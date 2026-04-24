package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceSupplement implements IServices<Supplement> {

    private final Connection cnx;
    private final String supplementTable;
    private final boolean hasDurationColumn;
    private final boolean hasCreatedAtColumn;
    private final boolean hasUpdatedAtColumn;

    public ServiceSupplement() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }
        supplementTable = SchemaInitializer.SUPPLEMENT_TABLE;
        hasDurationColumn = hasColumn(supplementTable, "recommended_duration_days");
        hasCreatedAtColumn = hasColumn(supplementTable, "created_at");
        hasUpdatedAtColumn = hasColumn(supplementTable, "updated_at");
    }

    @Override
    public void add(Supplement supplement) {
        validate(supplement);
        StringBuilder columns = new StringBuilder("name, category, brand, price, stock, calories, description, image");
        StringBuilder values = new StringBuilder("?, ?, ?, ?, ?, ?, ?, ?");
        if (hasDurationColumn) {
            columns.append(", recommended_duration_days");
            values.append(", ?");
        }
        if (hasCreatedAtColumn) {
            columns.append(", created_at");
            values.append(", ?");
        }
        if (hasUpdatedAtColumn) {
            columns.append(", updated_at");
            values.append(", ?");
        }
        String query = "INSERT INTO " + supplementTable + " (" + columns + ") VALUES (" + values + ")";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int nextParamIndex = fillStatement(preparedStatement, supplement);
            LocalDateTime now = LocalDateTime.now();

            if (hasDurationColumn) {
                preparedStatement.setInt(nextParamIndex, 30);
                nextParamIndex++;
            }
            if (hasCreatedAtColumn) {
                preparedStatement.setTimestamp(nextParamIndex, Timestamp.valueOf(now));
                nextParamIndex++;
            }
            if (hasUpdatedAtColumn) {
                preparedStatement.setTimestamp(nextParamIndex, Timestamp.valueOf(now));
            }

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    supplement.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le supplement.", exception);
        }
    }

    @Override
    public List<Supplement> getAll() {
        List<Supplement> supplements = new ArrayList<>();
        String query = "SELECT * FROM " + supplementTable + " ORDER BY updated_at DESC, id DESC";

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                supplements.add(mapResultSet(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les supplements.", exception);
        }

        return supplements;
    }

    @Override
    public Supplement getById(int id) {
        String query = "SELECT * FROM " + supplementTable + " WHERE id = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le supplement " + id + ".", exception);
        }
        return null;
    }

    @Override
    public void update(Supplement supplement) {
        validate(supplement);
        String query = hasUpdatedAtColumn
                ? """
                UPDATE %s
                SET name = ?, category = ?, brand = ?, price = ?, stock = ?, calories = ?, description = ?, image = ?, updated_at = ?
                WHERE id = ?
                """.formatted(supplementTable)
                : """
                UPDATE %s
                SET name = ?, category = ?, brand = ?, price = ?, stock = ?, calories = ?, description = ?, image = ?
                WHERE id = ?
                """.formatted(supplementTable);

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            int nextParamIndex = fillStatement(preparedStatement, supplement);
            if (hasUpdatedAtColumn) {
                preparedStatement.setTimestamp(nextParamIndex, Timestamp.valueOf(LocalDateTime.now()));
                nextParamIndex++;
            }
            preparedStatement.setInt(nextParamIndex, supplement.getId());
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows != 1) {
                throw new IllegalStateException("Supplement introuvable pour l'identifiant " + supplement.getId() + ".");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le supplement.", exception);
        }
    }

    @Override
    public void delete(Supplement supplement) {
        if (supplement == null || supplement.getId() <= 0) {
            throw new IllegalArgumentException("Le supplement selectionne est invalide.");
        }

        String query = "DELETE FROM " + supplementTable + " WHERE id = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, supplement.getId());
            int deletedRows = preparedStatement.executeUpdate();
            if (deletedRows != 1) {
                throw new IllegalStateException("Supplement introuvable pour l'identifiant " + supplement.getId() + ".");
            }
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalStateException("Impossible de supprimer ce supplement car il est deja lie a d'autres donnees.", exception);
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le supplement.", exception);
        }
    }

    private int fillStatement(PreparedStatement preparedStatement, Supplement supplement) throws SQLException {
        preparedStatement.setString(1, supplement.getName());
        preparedStatement.setString(2, supplement.getCategory());
        preparedStatement.setString(3, supplement.getBrand());
        preparedStatement.setBigDecimal(4, supplement.getPrice());
        preparedStatement.setInt(5, supplement.getStock());

        if (supplement.getCalories() == null) {
            preparedStatement.setNull(6, Types.INTEGER);
        } else {
            preparedStatement.setInt(6, supplement.getCalories());
        }

        preparedStatement.setString(7, supplement.getDescription());

        if (supplement.getImage() == null || supplement.getImage().isBlank()) {
            preparedStatement.setNull(8, Types.VARCHAR);
        } else {
            preparedStatement.setString(8, supplement.getImage());
        }
        return 9;
    }

    private Supplement mapResultSet(ResultSet resultSet) throws SQLException {
        return new Supplement(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getString("brand"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock"),
                readNullableInteger(resultSet, "calories"),
                resultSet.getString("description"),
                resultSet.getString("image"),
                readTimestamp(resultSet.getTimestamp("created_at")),
                readTimestamp(resultSet.getTimestamp("updated_at"))
        );
    }

    private Integer readNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDateTime readTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void validate(Supplement supplement) {
        if (supplement == null) {
            throw new IllegalArgumentException("Le supplement est obligatoire.");
        }
        if (supplement.getName() == null || supplement.getName().isBlank()) {
            throw new IllegalArgumentException("Le nom du supplement est obligatoire.");
        }
        if (supplement.getCategory() == null || supplement.getCategory().isBlank()) {
            throw new IllegalArgumentException("La categorie du supplement est obligatoire.");
        }
        if (supplement.getBrand() == null || supplement.getBrand().isBlank()) {
            throw new IllegalArgumentException("La marque du supplement est obligatoire.");
        }

        BigDecimal price = supplement.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit etre superieur a 0.");
        }
        if (supplement.getStock() < 0) {
            throw new IllegalArgumentException("Le stock doit etre positif ou nul.");
        }
        if (supplement.getCalories() != null && supplement.getCalories() < 0) {
            throw new IllegalArgumentException("Les calories doivent etre positives.");
        }
        if (supplement.getDescription() == null || supplement.getDescription().isBlank()) {
            throw new IllegalArgumentException("La description du supplement est obligatoire.");
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        try {
            try (ResultSet resultSet = cnx.getMetaData().getColumns(cnx.getCatalog(), null, tableName, columnName)) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            return false;
        }
    }
}
