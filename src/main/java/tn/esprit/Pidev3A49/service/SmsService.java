package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;

import java.util.Locale;

public class SmsService {

    private final EventDAO eventDAO;

    public SmsService() {
        this.eventDAO = new EventDAO();
    }

    public void sendWelcomeSms(String phone, Reservation reservation) {
        validatePhone(phone);
        if (reservation == null || reservation.getIdEvent() <= 0) {
            throw new IllegalArgumentException("La reservation est invalide pour l'envoi SMS.");
        }

        Event event = eventDAO.findById(reservation.getIdEvent());
        String eventTitle = event == null || event.getTitre() == null || event.getTitre().isBlank()
                ? "votre evenement"
                : event.getTitre().trim();
        String message = "Bienvenue chez Fitopia ! Votre reservation pour l'evenement "
                + eventTitle
                + " est confirmee. Montant paye : "
                + String.format(Locale.US, "%.2f", reservation.getMontant())
                + " DT. Merci pour votre confiance.";

        simulateSms(phone, message);
    }

    public void simulateSms(String phone, String message) {
        System.out.println("SMS simule envoye a " + phone + " : " + message);
    }

    public void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Le telephone est obligatoire.");
        }

        String normalized = phone.trim().replaceAll("\\s+", "");
        if (!normalized.matches("^\\+?[0-9]{8,15}$")) {
            throw new IllegalArgumentException("Le telephone doit contenir 8 a 15 chiffres, avec + optionnel.");
        }
    }
}
