package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegimeAlimentaire implements IServices<RegimeAlimentaire> {

    private static final String TABLE_NAME = SchemaInitializer.REGIME_TABLE;
    private final Connection cnx;

    public ServiceRegimeAlimentaire() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(RegimeAlimentaire regime) {
        validate(regime);
        String qry = """
                INSERT INTO %s (user_id, taille, poids, age, bmi, type_sante, calories_cibles, repas_adequats)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            remplirPreparedStatementRegime(pstm, regime, false);
            pstm.executeUpdate();
            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    regime.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le regime alimentaire.", exception);
        }
    }

    @Override
    public List<RegimeAlimentaire> getAll() {
        List<RegimeAlimentaire> regimes = new ArrayList<>();
        String qry = """
                SELECT r.id, r.user_id, u.email, r.taille, r.poids, r.age, r.bmi, r.type_sante, r.calories_cibles, r.repas_adequats
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                ORDER BY r.id DESC
                """.formatted(TABLE_NAME);

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                regimes.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les regimes alimentaires.", exception);
        }
        return regimes;
    }

    @Override
    public RegimeAlimentaire getById(int id) {
        String qry = """
                SELECT r.id, r.user_id, u.email, r.taille, r.poids, r.age, r.bmi, r.type_sante, r.calories_cibles, r.repas_adequats
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                WHERE r.id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le regime alimentaire avec l'id " + id, exception);
        }
        return null;
    }

    @Override
    public void update(RegimeAlimentaire regime) {
        validate(regime);
        String qry = """
                UPDATE %s
                SET user_id = ?, taille = ?, poids = ?, age = ?, bmi = ?, type_sante = ?, calories_cibles = ?, repas_adequats = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            remplirPreparedStatementRegime(pstm, regime, true);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le regime alimentaire.", exception);
        }
    }

    @Override
    public void delete(RegimeAlimentaire regime) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, regime.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le regime alimentaire.", exception);
        }
    }

    private RegimeAlimentaire mapResultSet(ResultSet rs) throws SQLException {
        return new RegimeAlimentaire(
                rs.getInt("id"),
                readNullableInt(rs, "user_id"),
                rs.getString("email"),
                readNullableDouble(rs, "taille"),
                readNullableDouble(rs, "poids"),
                readNullableInt(rs, "age"),
                readNullableDouble(rs, "bmi"),
                rs.getString("type_sante"),
                readNullableInt(rs, "calories_cibles"),
                rs.getString("repas_adequats")
        );
    }

    private void remplirPreparedStatementRegime(PreparedStatement pstm, RegimeAlimentaire regime, boolean withId) throws SQLException {
        pstm.setInt(1, regime.getUserId());
        setNullableDouble(pstm, 2, regime.getTaille());
        setNullableDouble(pstm, 3, regime.getPoids());
        setNullableInteger(pstm, 4, regime.getAge());
        setNullableDouble(pstm, 5, regime.getBmi());
        pstm.setString(6, regime.getTypeSante());
        setNullableInteger(pstm, 7, regime.getCaloriesCibles());
        pstm.setString(8, regime.getRepasAdequats());
        if (withId) {
            pstm.setInt(9, regime.getId());
        }
    }

    private void validate(RegimeAlimentaire regime) {
        if (regime == null) {
            throw new IllegalArgumentException("Le regime alimentaire est obligatoire.");
        }
        if (regime.getUserId() == null) {
            throw new IllegalArgumentException("L'utilisateur du regime est obligatoire.");
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }

    private Integer readNullableInt(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private Double readNullableDouble(ResultSet resultSet, String column) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }
}
