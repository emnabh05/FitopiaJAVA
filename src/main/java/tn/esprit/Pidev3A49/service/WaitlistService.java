package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.dao.WaitlistDAO;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.WaitlistEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaitlistService {

    private final WaitlistDAO waitlistDAO;
    private final ReservationDAO reservationDAO;
    private final EventService eventService;
    private final ParticipationService participationService;
    private final ReservationQrService reservationQrService;

    public WaitlistService() {
        this.waitlistDAO = new WaitlistDAO();
        this.reservationDAO = new ReservationDAO();
        this.eventService = new EventService();
        this.participationService = new ParticipationService();
        this.reservationQrService = new ReservationQrService();
    }

    public WaitlistEntry addToWaitlist(Event event, String nomParticipant, String emailParticipant) {
        validateEvent(event);
        String email = requireEmail(emailParticipant);
        String name = normalizeName(nomParticipant, email);
        int idEvent = event.getIdEvent();
        int validReservations = reservationDAO.countValidReservationsByEvent(idEvent);
        boolean eventFull = validReservations >= event.getCapacite();
        boolean hasReservation = reservationDAO.existsValidReservationByEventAndEmail(idEvent, email);
        boolean alreadyInWaitlist = isAlreadyInWaitlist(idEvent, email);

        System.out.println("[WaitlistService] addToWaitlist START");
        System.out.println("[WaitlistService] id_event=" + idEvent);
        System.out.println("[WaitlistService] email=" + email);
        System.out.println("[WaitlistService] nom=" + name);
        System.out.println("[WaitlistService] validReservations=" + validReservations + "/" + event.getCapacite());
        System.out.println("[WaitlistService] isEventFull=" + eventFull);
        System.out.println("[WaitlistService] hasExistingReservation=" + hasReservation);
        System.out.println("[WaitlistService] isAlreadyInWaitlist=" + alreadyInWaitlist);

        if (!eventFull) {
            System.out.println("[WaitlistService] REFUS: evenement non complet");
            throw new IllegalArgumentException("Cet evenement a encore des places disponibles. Tu peux reserver directement.");
        }
        if (hasReservation) {
            System.out.println("[WaitlistService] REFUS: reservation existante");
            throw new IllegalArgumentException("Vous avez deja une reservation valide pour cet evenement.");
        }
        if (alreadyInWaitlist) {
            System.out.println("[WaitlistService] REFUS: deja en waitlist");
            throw new IllegalArgumentException("Vous etes deja inscrit a la liste d'attente.");
        }

        int position = waitlistDAO.nextPositionForEvent(idEvent);
        WaitlistEntry entry = new WaitlistEntry(
                idEvent,
                name,
                email,
                "EN_ATTENTE",
                position,
                LocalDateTime.now(),
                UUID.randomUUID().toString(),
                null,
                null
        );
        waitlistDAO.add(entry);
        System.out.println("[WaitlistService] SUCCESS: waitlist id=" + entry.getId() + ", position=" + entry.getPosition());
        return entry;
    }

    public boolean isAlreadyInWaitlist(int idEvent, String emailParticipant) {
        if (emailParticipant == null || emailParticipant.isBlank()) {
            return false;
        }
        return waitlistDAO.existsActiveByEventAndEmail(idEvent, emailParticipant.trim());
    }

    public List<WaitlistEntry> getWaitlistForEvent(int idEvent) {
        return waitlistDAO.getWaitlistForEvent(idEvent);
    }

    public WaitlistEntry getFirstWaitingForEvent(int idEvent) {
        return waitlistDAO.getFirstWaitingForEvent(idEvent);
    }

    public List<Integer> getActiveWaitlistEventIdsByEmail(String emailParticipant) {
        return waitlistDAO.findActiveEventIdsByEmail(requireEmail(emailParticipant));
    }

    public Map<Integer, Integer> countActiveByEvent() {
        return waitlistDAO.countActiveByEvent();
    }

    public boolean isEventFull(Event event) {
        validateEvent(event);
        int validReservations = reservationDAO.countValidReservationsByEvent(event.getIdEvent());
        boolean full = validReservations >= event.getCapacite();
        System.out.println("[WaitlistService] isEventFull id_event=" + event.getIdEvent()
                + ", validReservations=" + validReservations
                + ", capacite=" + event.getCapacite()
                + ", result=" + full);
        return full;
    }

    public WaitlistEntry promoteNextIfNeeded(int idEvent) {
        WaitlistEntry next = getFirstWaitingForEvent(idEvent);
        if (next == null) {
            return null;
        }

        Reservation promotedReservation = promoteNextFromWaitlist(idEvent);
        if (promotedReservation == null) {
            return null;
        }

        return next;
    }

    public void removeFromWaitlist(WaitlistEntry entry) {
        if (entry == null || entry.getId() <= 0) {
            throw new IllegalArgumentException("L'entree waitlist est invalide.");
        }
        waitlistDAO.removeFromWaitlist(entry.getId());
    }

    public Reservation promoteNextFromWaitlist(int idEvent) {
        Event event = eventService.getById(idEvent);
        if (event == null || isEventFull(event)) {
            return null;
        }

        WaitlistEntry next = getFirstWaitingForEvent(idEvent);
        if (next == null) {
            return null;
        }

        if (reservationDAO.existsValidReservationByEventAndEmail(idEvent, next.getEmailParticipant())) {
            waitlistDAO.markPromoted(next.getId());
            waitlistDAO.removeFromWaitlist(next.getId());
            return null;
        }

        Reservation reservation = new Reservation(
                idEvent,
                next.getNomParticipant(),
                next.getEmailParticipant(),
                LocalDateTime.now(),
                event.getPrixEvent(),
                "Confirmee"
        );
        reservationQrService.attachQrToReservation(reservation);
        reservationDAO.add(reservation);

        participationService.add(new Participation(
                idEvent,
                next.getNomParticipant(),
                next.getEmailParticipant(),
                LocalDateTime.now()
        ));

        waitlistDAO.markPromoted(next.getId());
        waitlistDAO.removeFromWaitlist(next.getId());
        System.out.println("[WaitlistService] promoteNextFromWaitlist SUCCESS reservation_id=" + reservation.getId()
                + ", waitlist_id=" + next.getId());
        return reservation;
    }

    public Reservation promoteNextFromWaitlist(Event event) {
        validateEvent(event);
        Reservation reservation = promoteNextFromWaitlist(event.getIdEvent());
        if (reservation == null) {
            throw new IllegalArgumentException("Aucun client en attente a promouvoir pour cet evenement.");
        }
        return reservation;
    }

    private void validateEvent(Event event) {
        if (event == null || event.getIdEvent() <= 0) {
            throw new IllegalArgumentException("L'evenement est invalide.");
        }
        if (event.getCapacite() <= 0) {
            throw new IllegalArgumentException("La capacite de l'evenement est invalide.");
        }
    }

    private String requireEmail(String value) {
        if (value == null || value.trim().isEmpty() || !value.contains("@")) {
            throw new IllegalArgumentException("Saisis un email client valide.");
        }
        return value.trim();
    }

    private String normalizeName(String name, String email) {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return email.substring(0, email.indexOf('@'));
    }
}
