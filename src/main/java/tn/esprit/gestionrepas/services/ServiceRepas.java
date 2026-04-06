package tn.esprit.gestionrepas.services;

import tn.esprit.gestionrepas.Models.Repas;
import tn.esprit.gestionrepas.interfaces.IServices;
import tn.esprit.gestionrepas.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepas implements IServices<Repas> {
    private static final String INSERT_SQL = "INSERT INTO repas " +
            "(user_id, date_repas, type_repas, nom_repas, calories, proteines, glucides, lipides, commentaire, regime_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM repas ORDER BY date_repas DESC, id_repas DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM repas WHERE id_repas = ?";
    private static final String UPDATE_SQL = "UPDATE repas SET user_id = ?, date_repas = ?, type_repas = ?, nom_repas = ?, " +
            "calories = ?, proteines = ?, glucides = ?, lipides = ?, commentaire = ?, regime_id = ? WHERE id_repas = ?";
    private static final String DELETE_SQL = "DELETE FROM repas WHERE id_repas = ?";
    private static final String CHECK_REGIME_SQL = "SELECT 1 FROM regime_alimentaire WHERE id = ?";

    private final Connection cnx;

    public ServiceRepas() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Repas repas) {
        validateRepas(repas);

        try (PreparedStatement statement = cnx.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, repas);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    repas.setIdRepas(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout du repas : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Repas> getAll() {
        List<Repas> repasList = new ArrayList<>();

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {

            while (resultSet.next()) {
                repasList.add(mapResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la lecture des repas : " + e.getMessage(), e);
        }

        return repasList;
    }

    public Repas getById(int id) {
        try (PreparedStatement statement = cnx.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la recherche du repas : " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void update(Repas repas) {
        validateRepas(repas);

        try (PreparedStatement statement = cnx.prepareStatement(UPDATE_SQL)) {
            fillStatement(statement, repas);
            statement.setInt(11, repas.getIdRepas());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la modification du repas : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Repas repas) {
        if (repas != null) {
            deleteById(repas.getIdRepas());
        }
    }

    public boolean deleteById(int id) {
        try (PreparedStatement statement = cnx.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la suppression du repas : " + e.getMessage(), e);
        }
    }

    private void validateRepas(Repas repas) {
        if (repas.getDateRepas() == null) {
            throw new IllegalArgumentException("La date du repas est obligatoire.");
        }
        if (repas.getTypeRepas() == null || repas.getTypeRepas().isBlank()) {
            throw new IllegalArgumentException("Le type du repas est obligatoire.");
        }
        if (repas.getNomRepas() == null || repas.getNomRepas().isBlank()) {
            throw new IllegalArgumentException("Le nom du repas est obligatoire.");
        }
        if (repas.getRegimeId() != null && !regimeExists(repas.getRegimeId())) {
            throw new IllegalArgumentException("Le regime saisi n'existe pas dans la base.");
        }
    }

    private void fillStatement(PreparedStatement statement, Repas repas) throws SQLException {
        statement.setInt(1, repas.getUserId());
        statement.setTimestamp(2, Timestamp.valueOf(repas.getDateRepas()));
        statement.setString(3, repas.getTypeRepas());
        statement.setString(4, repas.getNomRepas());
        setNullableInteger(statement, 5, repas.getCalories());
        setNullableInteger(statement, 6, repas.getProteines());
        setNullableInteger(statement, 7, repas.getGlucides());
        setNullableInteger(statement, 8, repas.getLipides());
        setNullableString(statement, 9, repas.getCommentaire());
        setNullableInteger(statement, 10, repas.getRegimeId());
    }

    private Repas mapResultSet(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("date_repas");

        return new Repas(
                resultSet.getInt("id_repas"),
                resultSet.getInt("user_id"),
                timestamp == null ? null : timestamp.toLocalDateTime(),
                resultSet.getString("type_repas"),
                resultSet.getString("nom_repas"),
                resultSet.getObject("calories", Integer.class),
                resultSet.getObject("proteines", Integer.class),
                resultSet.getObject("glucides", Integer.class),
                resultSet.getObject("lipides", Integer.class),
                resultSet.getString("commentaire"),
                resultSet.getObject("regime_id", Integer.class)
        );
    }

    private boolean regimeExists(Integer regimeId) {
        if (regimeId == null) {
            return true;
        }

        try (PreparedStatement statement = cnx.prepareStatement(CHECK_REGIME_SQL)) {
            statement.setInt(1, regimeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la verification du regime : " + e.getMessage(), e);
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
            return;
        }
        statement.setInt(index, value);
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
            return;
        }
        statement.setString(index, value);
    }
}
