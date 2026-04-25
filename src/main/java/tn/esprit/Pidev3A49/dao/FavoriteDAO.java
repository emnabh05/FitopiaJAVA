package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Favorite;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FavoriteDAO {

    private final Connection connection;

    public FavoriteDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Favorite favorite) {
        String sql = "INSERT INTO favorites (id_event, email_participant, created_at) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, favorite.getIdEvent());
            statement.setString(2, favorite.getEmailParticipant());
            statement.setTimestamp(3, Timestamp.valueOf(favorite.getCreatedAt()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun favori insere.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    favorite.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout du favori.", e);
        }
    }

    public void remove(int idEvent, String emailParticipant) {
        String sql = "DELETE FROM favorites WHERE id_event = ? AND email_participant = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);
            statement.setString(2, emailParticipant);
            statement.executeUpdate();
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la suppression du favori.", e);
        }
    }

    public boolean exists(int idEvent, String emailParticipant) {
        String sql = "SELECT 1 FROM favorites WHERE id_event = ? AND email_participant = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);
            statement.setString(2, emailParticipant);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la verification du favori.", e);
        }
    }

    public Set<Integer> findFavoriteEventIdsByEmail(String emailParticipant) {
        String sql = "SELECT id_event FROM favorites WHERE email_participant = ?";
        Set<Integer> eventIds = new HashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    eventIds.add(resultSet.getInt("id_event"));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des favoris.", e);
        }

        return eventIds;
    }

    public Map<Integer, Integer> countByEvent() {
        String sql = "SELECT id_event, COUNT(*) AS total FROM favorites GROUP BY id_event";
        Map<Integer, Integer> counts = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                counts.put(resultSet.getInt("id_event"), resultSet.getInt("total"));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du comptage des favoris.", e);
        }

        return counts;
    }

    public int countByEmail(String emailParticipant) {
        String sql = "SELECT COUNT(*) AS total FROM favorites WHERE email_participant = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du comptage des favoris utilisateur.", e);
        }

        return 0;
    }

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL sur favorites.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
