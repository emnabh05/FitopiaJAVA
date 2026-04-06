package tn.esprit.gestionrepas.services;

import tn.esprit.gestionrepas.Models.RegimeAlimentaire;
import tn.esprit.gestionrepas.interfaces.IServices;
import tn.esprit.gestionrepas.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegimeAlimentaire implements IServices<RegimeAlimentaire> {
    private static final String INSERT_SQL = "INSERT INTO regime_alimentaire " +
            "(user_id, taille, poids, age, bmi, type_sante, calories_cibles, repas_adequats) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM regime_alimentaire ORDER BY id DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM regime_alimentaire WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE regime_alimentaire SET user_id = ?, taille = ?, poids = ?, age = ?, " +
            "bmi = ?, type_sante = ?, calories_cibles = ?, repas_adequats = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM regime_alimentaire WHERE id = ?";

    private final Connection cnx;

    public ServiceRegimeAlimentaire() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(RegimeAlimentaire regime) {
        prepareBusinessFields(regime);

        try (PreparedStatement statement = cnx.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, regime);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    regime.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de l'ajout du regime : " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegimeAlimentaire> getAll() {
        List<RegimeAlimentaire> regimes = new ArrayList<>();

        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {

            while (resultSet.next()) {
                regimes.add(mapResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la lecture des regimes : " + e.getMessage(), e);
        }

        return regimes;
    }

    public RegimeAlimentaire getById(int id) {
        try (PreparedStatement statement = cnx.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la recherche du regime : " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void update(RegimeAlimentaire regime) {
        prepareBusinessFields(regime);

        try (PreparedStatement statement = cnx.prepareStatement(UPDATE_SQL)) {
            fillStatement(statement, regime);
            statement.setInt(9, regime.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la modification du regime : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(RegimeAlimentaire regime) {
        if (regime != null) {
            deleteById(regime.getId());
        }
    }

    public boolean deleteById(int id) {
        try (PreparedStatement statement = cnx.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lors de la suppression du regime : " + e.getMessage(), e);
        }
    }

    private void prepareBusinessFields(RegimeAlimentaire regime) {
        regime.calculerBmiEtTypeSante();
    }

    private void fillStatement(PreparedStatement statement, RegimeAlimentaire regime) throws SQLException {
        statement.setInt(1, regime.getUserId());
        setNullableDouble(statement, 2, regime.getTaille());
        setNullableDouble(statement, 3, regime.getPoids());
        setNullableInteger(statement, 4, regime.getAge());
        setNullableDouble(statement, 5, regime.getBmi());
        setNullableString(statement, 6, regime.getTypeSante());
        setNullableInteger(statement, 7, regime.getCaloriesCibles());
        setNullableString(statement, 8, regime.getRepasAdequats());
    }

    private RegimeAlimentaire mapResultSet(ResultSet resultSet) throws SQLException {
        return new RegimeAlimentaire(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getObject("taille", Double.class),
                resultSet.getObject("poids", Double.class),
                resultSet.getObject("age", Integer.class),
                resultSet.getObject("bmi", Double.class),
                resultSet.getString("type_sante"),
                resultSet.getObject("calories_cibles", Integer.class),
                resultSet.getString("repas_adequats")
        );
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
            return;
        }
        statement.setInt(index, value);
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
            return;
        }
        statement.setDouble(index, value);
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
            return;
        }
        statement.setString(index, value);
    }
}
