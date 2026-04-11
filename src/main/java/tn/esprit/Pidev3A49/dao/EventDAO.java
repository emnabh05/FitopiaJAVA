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

    private final Connection connection;

    public EventDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Event event) {
        String sql = "INSERT INTO events (titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatementForWrite(statement, event);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Aucun evenement insere.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setIdEvent(generatedKeys.getInt(1));
                    System.out.println("Event ajoute avec succes. id_event=" + event.getIdEvent());
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout de l'evenement.", e);
        }
    }

    public void update(Event event) {
        String sql = "UPDATE events SET titre=?, description=?, date_event=?, lieu=?, capacite=?, type_event=?, image_event=?, prix_event=?, created_at=?, is_premium=? " +
                "WHERE id_event=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatementForWrite(statement, event);
            statement.setInt(11, event.getIdEvent());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun evenement modifie pour id_event=" + event.getIdEvent());
            }

            System.out.println("Event mis a jour avec succes. id_event=" + event.getIdEvent());
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la modification de l'evenement.", e);
        }
    }

    public void delete(int idEvent) {
        String sql = "DELETE FROM events WHERE id_event=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun evenement supprime pour id_event=" + idEvent);
            }

            System.out.println("Event supprime avec succes. id_event=" + idEvent);
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la suppression de l'evenement.", e);
        }
    }

    public Event findById(int idEvent) {
        String sql = "SELECT id_event, titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium " +
                "FROM events WHERE id_event=?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapEvent(resultSet);
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la recherche de l'evenement.", e);
        }

        return null;
    }

    public List<Event> getAll() {
        String sql = "SELECT id_event, titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium " +
                "FROM events ORDER BY id_event DESC";
        List<Event> events = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                events.add(mapEvent(resultSet));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des evenements.", e);
        }

        return events;
    }

    public List<Event> findAll() {
        return getAll();
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

    public void deleteById(int idEvent) {
        delete(idEvent);
    }

    public List<Event> getAllEvents() {
        return getAll();
    }

    private void fillStatementForWrite(PreparedStatement statement, Event event) throws SQLException {
        statement.setString(1, event.getTitre());
        statement.setString(2, event.getDescription());

        LocalDate dateEvent = event.getDateEvent();
        if (dateEvent != null) {
            statement.setDate(3, Date.valueOf(dateEvent));
        } else {
            statement.setDate(3, null);
        }

        statement.setString(4, event.getLieu());
        statement.setInt(5, event.getCapacite());
        statement.setString(6, event.getTypeEvent());
        statement.setString(7, event.getImageEvent());
        statement.setDouble(8, event.getPrixEvent());

        LocalDateTime createdAt = event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now();
        statement.setTimestamp(9, Timestamp.valueOf(createdAt));

        statement.setBoolean(10, event.isPremium());
    }

    private Event mapEvent(ResultSet resultSet) throws SQLException {
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

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL sur events.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}