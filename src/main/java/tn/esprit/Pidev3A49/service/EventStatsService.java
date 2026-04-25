package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.EventStatsDAO;
import tn.esprit.Pidev3A49.models.EventStats;

import java.util.List;

public class EventStatsService {

    private final EventStatsDAO eventStatsDAO = new EventStatsDAO();

    public List<EventStats> getTopProfitableEvents() {
        return eventStatsDAO.getTopProfitableEvents();
    }
}
