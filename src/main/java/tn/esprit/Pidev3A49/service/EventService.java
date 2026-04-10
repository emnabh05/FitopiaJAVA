package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.models.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventService {

    private final EventDAO eventDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
    }

    public void add(Event event) {
        validateForSave(event, false);
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        eventDAO.add(event);
    }

    public void update(Event event) {
        validateForSave(event, true);
        eventDAO.update(event);
    }

    public void delete(int idEvent) {
        if (idEvent <= 0) {
            throw new IllegalArgumentException("L'id de l'evenement est invalide.");
        }
        eventDAO.delete(idEvent);
    }

    public List<Event> getAll() {
        return eventDAO.getAll();
    }

    public void ajouterEvent(Event event) {
        add(event);
    }

    public void modifierEvent(Event event) {
        update(event);
    }

    public void supprimerEvent(int idEvent) {
        delete(idEvent);
    }

    public List<Event> afficherEvents() {
        return getAll();
    }

    private void validateForSave(Event event, boolean requireId) {
        if (event == null) {
            throw new IllegalArgumentException("L'evenement est obligatoire.");
        }
        if (requireId && event.getIdEvent() <= 0) {
            throw new IllegalArgumentException("L'id de l'evenement est obligatoire pour la mise a jour.");
        }
        if (isBlank(event.getTitre())) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        if (event.getDateEvent() == null) {
            throw new IllegalArgumentException("La date de l'evenement est obligatoire.");
        }
        if (isBlank(event.getLieu())) {
            throw new IllegalArgumentException("Le lieu est obligatoire.");
        }
        if (event.getCapacite() < 0) {
            throw new IllegalArgumentException("La capacite doit etre positive.");
        }
        if (event.getPrixEvent() < 0) {
            throw new IllegalArgumentException("Le prix doit etre positif.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
