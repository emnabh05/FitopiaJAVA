package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.models.Event;

import java.util.List;

public class EventService {

    private final EventDAO eventDAO = new EventDAO();
    private final LoyaltyService loyaltyService = new LoyaltyService();

    public void add(Event event) {
        eventDAO.add(event);
    }

    public void update(Event event) {
        eventDAO.update(event);
    }

    public void delete(int idEvent) {
        eventDAO.delete(idEvent);
    }

    public Event getById(int idEvent) {
        return eventDAO.findById(idEvent);
    }

    public List<Event> getAll() {
        return eventDAO.findAll();
    }

    public boolean canReserve(Event event, String emailParticipant) {
        if (event == null) {
            return false;
        }
        if (!event.isPremium()) {
            return true;
        }
        return loyaltyService.isVip(emailParticipant);
    }

    public void ensureCanReserve(Event event, String emailParticipant) {
        if (event == null) {
            throw new IllegalArgumentException("L'evenement selectionne est invalide.");
        }
        if (event.isPremium() && !loyaltyService.isVip(emailParticipant)) {
            throw new IllegalArgumentException("Reserve aux clients VIP.");
        }
    }
}
