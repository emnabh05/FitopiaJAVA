package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    private static final String BASE_SELECT =
            "SELECT id_event, titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium FROM events";

    private final Connection connection;

    public EventDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Event event) {
        String sql = "INSERT INTO events (titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatementForWrite(statement, event, true);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune ligne inseree dans la table events.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setIdEvent(generatedKeys.getInt(1));
                }
            }

            System.out.println("Event ajoute avec succes. id_event=" + event.getIdEvent());
        } catch (SQLException e) {
            logSqlError("ajout", sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout de l'evenement.", e);
        }
    }

    public void update(Event event) {
        String sql = "UPDATE events SET titre = ?, description = ?, date_event = ?, lieu = ?, capacite = ?, "
                + "type_event = ?, image_event = ?, prix_event = ?, is_premium = ? WHERE id_event = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatementForWrite(statement, event, false);
            statement.setInt(10, event.getIdEvent());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun evenement trouve avec id_event=" + event.getIdEvent() + ".");
            }

            System.out.println("Event mis a jour avec succes. id_event=" + event.getIdEvent());
        } catch (SQLException e) {
            logSqlError("mise a jour", sql, e);
            throw new RuntimeException("Echec SQL lors de la mise a jour de l'evenement.", e);
        }
    }

    public void delete(int idEvent) {
        String sql = "DELETE FROM events WHERE id_event = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun evenement supprime avec id_event=" + idEvent + ".");
            }

            System.out.println("Event supprime avec succes. id_event=" + idEvent);
        } catch (SQLException e) {
            logSqlError("suppression", sql, e);
            throw new RuntimeException("Echec SQL lors de la suppression de l'evenement.", e);
        }
    }

    public List<Event> getAll() {
        String sql = BASE_SELECT + " ORDER BY id_event DESC";
        List<Event> events = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                events.add(mapResultSetToEvent(resultSet));
            }
        } catch (SQLException e) {
            logSqlError("chargement", sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des evenements.", e);
        }

        return events;
    }

    public void addEvent(Event event) {
        add(event);
    }

    public void updateEvent(Event event) {
        update(event);
    }

    public void deleteEvent(int idEvent) {
        delete(idEvent);
    }

    public List<Event> getAllEvents() {
        return getAll();
    }

    private void fillStatementForWrite(PreparedStatement statement, Event event, boolean includeCreatedAt) throws SQLException {
        LocalDate dateEvent = event.getDateEvent();
        LocalDateTime createdAt = event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now();
        event.setCreatedAt(createdAt);

        statement.setString(1, event.getTitre());
        statement.setString(2, event.getDescription());
        statement.setDate(3, Date.valueOf(dateEvent));
        statement.setString(4, event.getLieu());
        statement.setInt(5, event.getCapacite());
        statement.setString(6, event.getTypeEvent());
        statement.setString(7, event.getImageEvent());
        statement.setDouble(8, event.getPrixEvent());

        if (includeCreatedAt) {
            statement.setTimestamp(9, Timestamp.valueOf(createdAt));
            statement.setBoolean(10, event.isPremium());
        } else {
            statement.setBoolean(9, event.isPremium());
        }
    }

    private Event mapResultSetToEvent(ResultSet resultSet) throws SQLException {
        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null;
        Date sqlDate = resultSet.getDate("date_event");
        LocalDate dateEvent = sqlDate != null ? sqlDate.toLocalDate() : null;

        return new Event(
                resultSet.getInt("id_event"),
                resultSet.getString("titre"),
                resultSet.getString("description"),
                dateEvent,
                resultSet.getString("lieu"),
                resultSet.getInt("capacite"),
                resultSet.getString("type_event"),
                resultSet.getString("image_event"),
                resultSet.getDouble("prix_event"),
                createdAt,
                resultSet.getBoolean("is_premium")
        );
    }

    private void logSqlError(String operation, String sql, SQLException e) {
        System.err.println("Erreur SQL lors de la " + operation + " d'un event.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
