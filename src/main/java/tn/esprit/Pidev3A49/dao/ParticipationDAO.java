package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParticipationDAO {

    private final Connection connection;

    public ParticipationDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Participation participation) {
        String sql = "INSERT INTO participation (id_event, nom_participant, email_participant, date_inscription) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, participation.getIdEvent());
            statement.setString(2, participation.getNomParticipant());
            statement.setString(3, participation.getEmailParticipant());

            LocalDateTime dateInscription = participation.getDateInscription() != null
                    ? participation.getDateInscription()
                    : LocalDateTime.now();
            statement.setTimestamp(4, Timestamp.valueOf(dateInscription));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune participation inseree.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participation.setIdParticipation(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout de la participation.", e);
        }
    }

    public void update(Participation participation) {
        String sql = "UPDATE participation SET id_event=?, nom_participant=?, email_participant=?, date_inscription=? WHERE id_participation=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, participation.getIdEvent());
            statement.setString(2, participation.getNomParticipant());
            statement.setString(3, participation.getEmailParticipant());

            LocalDateTime dateInscription = participation.getDateInscription() != null
                    ? participation.getDateInscription()
                    : LocalDateTime.now();
            statement.setTimestamp(4, Timestamp.valueOf(dateInscription));
            statement.setInt(5, participation.getIdParticipation());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune participation modifiee.");
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la modification de la participation.", e);
        }
    }

    public void deleteById(int idParticipation) {
        String sql = "DELETE FROM participation WHERE id_participation=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idParticipation);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune participation supprimee.");
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la suppression de la participation.", e);
        }
    }

    public Participation findById(int idParticipation) {
        String sql = "SELECT id_participation, id_event, nom_participant, email_participant, date_inscription " +
                "FROM participation WHERE id_participation=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idParticipation);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapParticipation(resultSet);
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la recherche de la participation.", e);
        }

        return null;
    }

    public List<Participation> getAll() {
        String sql = "SELECT id_participation, id_event, nom_participant, email_participant, date_inscription " +
                "FROM participation ORDER BY id_participation DESC";
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

    public List<Participation> findAll() {
        return getAll();
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
        System.err.println("Erreur SQL lors des operations sur participation.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}