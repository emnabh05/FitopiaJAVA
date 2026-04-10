package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParticipationDAO {

    private final Connection connection;

    public ParticipationDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public List<Participation> getAll() {
        String sql = "SELECT id_participation, id_event, nom_participant, email_participant, date_inscription "
                + "FROM participation ORDER BY id_participation DESC";
        List<Participation> participations = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                participations.add(mapParticipation(resultSet));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des participations.", e);
        }

        return participations;
    }

    private Participation mapParticipation(ResultSet resultSet) throws SQLException {
        Timestamp inscriptionTimestamp = resultSet.getTimestamp("date_inscription");
        LocalDateTime dateInscription = inscriptionTimestamp != null ? inscriptionTimestamp.toLocalDateTime() : null;

        return new Participation(
                resultSet.getInt("id_participation"),
                resultSet.getInt("id_event"),
                resultSet.getString("nom_participant"),
                resultSet.getString("email_participant"),
                dateInscription
        );
    }

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL lors du chargement des participations.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
