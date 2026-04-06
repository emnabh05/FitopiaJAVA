package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.interfaces.CrudRepository;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO implements CrudRepository<Event, Integer> {
    private static final String INSERT_SQL = """
            INSERT INTO events (titre, description, date_event, lieu, capacite, type_event, image_event, prix_event, created_at, is_premium)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE events
            SET titre = ?, description = ?, date_event = ?, lieu = ?, capacite = ?, type_event = ?, image_event = ?, prix_event = ?, created_at = ?, is_premium = ?
            WHERE id_event = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM events WHERE id_event = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM events WHERE id_event = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM events ORDER BY id_event DESC";

    private final Connection connection;

    public EventDAO() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Event event) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindCommonFields(statement, event);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setIdEvent(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Event event) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bindCommonFields(statement, event);
            statement.setInt(11, event.getIdEvent());
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteById(Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Event> findById(Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Event> findAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                events.add(mapRow(resultSet));
            }
        }
        return events;
    }

    private void bindCommonFields(PreparedStatement statement, Event event) throws SQLException {
        statement.setString(1, event.getTitre());
        statement.setString(2, event.getDescription());
        statement.setDate(3, java.sql.Date.valueOf(event.getDateEvent()));
        statement.setString(4, event.getLieu());
        statement.setInt(5, event.getCapacite());
        statement.setString(6, event.getTypeEvent());
        if (event.getImageEvent() == null || event.getImageEvent().isBlank()) {
            statement.setNull(7, Types.VARCHAR);
        } else {
            statement.setString(7, event.getImageEvent());
        }
        statement.setBigDecimal(8, event.getPrixEvent());
        statement.setTimestamp(9, Timestamp.valueOf(event.getCreatedAt()));
        statement.setBoolean(10, event.isPremium());
    }

    private Event mapRow(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setIdEvent(resultSet.getInt("id_event"));
        event.setTitre(resultSet.getString("titre"));
        event.setDescription(resultSet.getString("description"));
        event.setDateEvent(resultSet.getDate("date_event").toLocalDate());
        event.setLieu(resultSet.getString("lieu"));
        event.setCapacite(resultSet.getInt("capacite"));
        event.setTypeEvent(resultSet.getString("type_event"));
        event.setImageEvent(resultSet.getString("image_event"));
        event.setPrixEvent(resultSet.getBigDecimal("prix_event"));
        event.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        event.setPremium(resultSet.getBoolean("is_premium"));
        return event;
    }
}
