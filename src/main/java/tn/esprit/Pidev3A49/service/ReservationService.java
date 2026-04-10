package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.models.Reservation;

import java.time.LocalDateTime;

public class ReservationService {

    private final ReservationDAO reservationDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
    }

    public void add(Reservation reservation) {
        validate(reservation);
        if (reservation.getDateReservation() == null) {
            reservation.setDateReservation(LocalDateTime.now());
        }
        if (reservation.getStatut() == null || reservation.getStatut().isBlank()) {
            reservation.setStatut("confirmee");
        }
        reservationDAO.add(reservation);
    }

    private void validate(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reservation est obligatoire.");
        }
        if (reservation.getIdEvent() <= 0) {
            throw new IllegalArgumentException("L'evenement selectionne est invalide.");
        }
        if (isBlank(reservation.getNomParticipant())) {
            throw new IllegalArgumentException("Le nom complet est obligatoire.");
        }
        if (isBlank(reservation.getEmailParticipant())) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        if (!reservation.getEmailParticipant().contains("@")) {
            throw new IllegalArgumentException("L'email saisi est invalide.");
        }
        if (reservation.getMontant() < 0) {
            throw new IllegalArgumentException("Le montant de reservation est invalide.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
