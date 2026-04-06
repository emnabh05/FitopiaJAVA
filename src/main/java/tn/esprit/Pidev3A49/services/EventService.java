package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.models.Event;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EventService {
    private final EventDAO eventDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
    }

    public void add(Event event) throws SQLException {
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        eventDAO.add(event);
    }

    public void update(Event event) throws SQLException {
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        eventDAO.update(event);
    }

    public void deleteById(int id) throws SQLException {
        eventDAO.deleteById(id);
    }

    public Optional<Event> findById(int id) throws SQLException {
        return eventDAO.findById(id);
    }

    public List<Event> findAll() throws SQLException {
        return eventDAO.findAll();
    }
}
