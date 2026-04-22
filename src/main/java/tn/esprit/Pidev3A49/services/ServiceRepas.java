package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepas implements IServices<Repas> {

    private static final String TABLE_NAME = SchemaInitializer.REPAS_TABLE;
    private final Connection cnx;

    public ServiceRepas() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Repas repas) {
        validate(repas);
        String qry = """
                INSERT INTO %s (user_id, date_repas, type_repas, nom_repas, calories, proteines, glucides, lipides, commentaire, regime_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            remplirPreparedStatementRepas(pstm, repas, false);
            pstm.executeUpdate();
            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    repas.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le repas. Cause SQL: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Repas> getAll() {
        List<Repas> repasList = new ArrayList<>();
        String qry = """
                SELECT r.id_repas, r.user_id, u.email, r.date_repas, r.type_repas, r.nom_repas, r.calories,
                       r.proteines, r.glucides, r.lipides, r.commentaire, r.regime_id,
                       rg.type_sante, rg.calories_cibles
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                LEFT JOIN %s rg ON rg.id = r.regime_id
                ORDER BY r.date_repas DESC, r.id_repas DESC
                """.formatted(TABLE_NAME, SchemaInitializer.REGIME_TABLE);

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                repasList.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les repas. Cause SQL: " + exception.getMessage(), exception);
        }

        return repasList;
    }

    @Override
    public Repas getById(int id) {
        String qry = """
                SELECT r.id_repas, r.user_id, u.email, r.date_repas, r.type_repas, r.nom_repas, r.calories,
                       r.proteines, r.glucides, r.lipides, r.commentaire, r.regime_id,
                       rg.type_sante, rg.calories_cibles
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                LEFT JOIN %s rg ON rg.id = r.regime_id
                WHERE r.id_repas = ?
                """.formatted(TABLE_NAME, SchemaInitializer.REGIME_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le repas avec l'id " + id, exception);
        }

        return null;
    }

    @Override
    public void update(Repas repas) {
        validate(repas);
        String qry = """
                UPDATE %s
                SET user_id = ?, date_repas = ?, type_repas = ?, nom_repas = ?, calories = ?, proteines = ?,
                    glucides = ?, lipides = ?, commentaire = ?, regime_id = ?
                WHERE id_repas = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            remplirPreparedStatementRepas(pstm, repas, true);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le repas. Cause SQL: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void delete(Repas repas) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id_repas = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, repas.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le repas.", exception);
        }
    }

    private Repas mapResultSet(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("date_repas");
        Integer regimeId = readNullableInt(rs, "regime_id");
        String regimeDisplay = null;
        if (regimeId != null) {
            regimeDisplay = "#" + regimeId + " - " + nullable(rs.getString("type_sante")) + " - "
                    + (readNullableInt(rs, "calories_cibles") == null ? 0 : readNullableInt(rs, "calories_cibles")) + " kcal";
        }

        return new Repas(
                rs.getInt("id_repas"),
                readNullableInt(rs, "user_id"),
                rs.getString("email"),
                timestamp == null ? null : timestamp.toLocalDateTime(),
                rs.getString("type_repas"),
                rs.getString("nom_repas"),
                readNullableInt(rs, "calories"),
                readNullableInt(rs, "proteines"),
                readNullableInt(rs, "glucides"),
                readNullableInt(rs, "lipides"),
                rs.getString("commentaire"),
                regimeId,
                regimeDisplay
        );
    }

    private void remplirPreparedStatementRepas(PreparedStatement pstm, Repas repas, boolean withId) throws SQLException {
        pstm.setInt(1, repas.getUserId());
        pstm.setTimestamp(2, Timestamp.valueOf(repas.getDateRepas()));
        pstm.setString(3, repas.getTypeRepas());
        pstm.setString(4, repas.getNomRepas());
        setNullableInteger(pstm, 5, repas.getCalories());
        setNullableInteger(pstm, 6, repas.getProteines());
        setNullableInteger(pstm, 7, repas.getGlucides());
        setNullableInteger(pstm, 8, repas.getLipides());
        pstm.setString(9, repas.getCommentaire());
        setNullableInteger(pstm, 10, repas.getRegimeId());
        if (withId) {
            pstm.setInt(11, repas.getId());
        }
    }

    private void validate(Repas repas) {
        if (repas == null) {
            throw new IllegalArgumentException("Le repas est obligatoire.");
        }
        if (repas.getUserId() == null) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire.");
        }
        if (repas.getDateRepas() == null) {
            throw new IllegalArgumentException("La date du repas est obligatoire.");
        }
        if (repas.getTypeRepas() == null || repas.getTypeRepas().isBlank()) {
            throw new IllegalArgumentException("Le type de repas est obligatoire.");
        }
        if (repas.getNomRepas() == null || repas.getNomRepas().isBlank()) {
            throw new IllegalArgumentException("Le nom du repas est obligatoire.");
        }
        if (repas.getCalories() != null && repas.getCalories() < 0) {
            throw new IllegalArgumentException("Les calories doivent etre positives.");
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private Integer readNullableInt(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
