package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Review;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDAO {

    private final Connection connection;

    public ReviewDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Review review) {
        String sql = "INSERT INTO reviews (id_event, email_participant, note, commentaire, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, review.getIdEvent());
            statement.setString(2, review.getEmailParticipant());
            statement.setInt(3, review.getNote());
            statement.setString(4, review.getCommentaire());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun avis insere.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout de l'avis.", e);
        }
    }

    public List<Review> findByEventId(int idEvent) {
        String sql = "SELECT id, id_event, email_participant, note, commentaire, created_at, updated_at "
                + "FROM reviews WHERE id_event = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des avis.", e);
        }

        return reviews;
    }

    public double getAverageNoteByEvent(int idEvent) {
        String sql = "SELECT AVG(note) AS avg_note FROM reviews WHERE id_event = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("avg_note");
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du calcul de la moyenne des avis.", e);
        }

        return 0.0;
    }

    public Map<Integer, Double> getAverageNotesByEvent() {
        String sql = "SELECT id_event, AVG(note) AS avg_note FROM reviews GROUP BY id_event";
        Map<Integer, Double> averages = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                averages.put(resultSet.getInt("id_event"), resultSet.getDouble("avg_note"));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des moyennes d'avis.", e);
        }

        return averages;
    }

    public Map<Integer, Integer> getReviewCountsByEvent() {
        String sql = "SELECT id_event, COUNT(*) AS total FROM reviews GROUP BY id_event";
        Map<Integer, Integer> counts = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counts.put(resultSet.getInt("id_event"), resultSet.getInt("total"));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement du nombre d'avis.", e);
        }

        return counts;
    }

    private Review mapReview(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        return new Review(
                resultSet.getInt("id"),
                resultSet.getInt("id_event"),
                resultSet.getString("email_participant"),
                resultSet.getInt("note"),
                resultSet.getString("commentaire"),
                createdAt,
                updatedAt
        );
    }

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL sur reviews.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
