package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.ReservationHistoryItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationService {

    public static final class CancelReservationResult {
        private final Reservation canceledReservation;
        private final Reservation promotedReservation;

        public CancelReservationResult(Reservation canceledReservation, Reservation promotedReservation) {
            this.canceledReservation = canceledReservation;
            this.promotedReservation = promotedReservation;
        }

        public Reservation getCanceledReservation() {
            return canceledReservation;
        }

        public Reservation getPromotedReservation() {
            return promotedReservation;
        }

        public boolean hasPromotion() {
            return promotedReservation != null;
        }
    }

    private final ReservationDAO reservationDAO;
    private final ParticipationService participationService;
    private final ReservationQrService reservationQrService;
    private final WaitlistService waitlistService;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.participationService = new ParticipationService();
        this.reservationQrService = new ReservationQrService();
        this.waitlistService = new WaitlistService();
    }

    public void add(Reservation reservation) {
        validate(reservation);
        if (reservation.getDateReservation() == null) {
            reservation.setDateReservation(LocalDateTime.now());
        }
        if (reservation.getStatut() == null || reservation.getStatut().isBlank()) {
            reservation.setStatut("confirmee");
        }
        if (shouldAttachQr(reservation)) {
            reservationQrService.attachQrToReservation(reservation);
        }
        reservationDAO.add(reservation);
    }

    public void markPaymentSucceeded(Reservation reservation, String transactionId) {
        if (reservation == null || reservation.getId() <= 0) {
            throw new IllegalArgumentException("La reservation est invalide.");
        }
        if (isBlank(transactionId)) {
            throw new IllegalArgumentException("La transaction de paiement est invalide.");
        }

        reservation.setStatut("PAYEE");
        reservation.setTransactionId(transactionId.trim());
        reservationQrService.attachQrToReservation(reservation);
        int updatedRows = reservationDAO.updatePaymentStatus(reservation);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Reservation introuvable pour validation du paiement.");
        }
    }

    public void markPaymentFailed(Reservation reservation) {
        if (reservation == null || reservation.getId() <= 0) {
            throw new IllegalArgumentException("La reservation est invalide.");
        }

        reservation.setStatut("ECHEC_PAIEMENT");
        reservation.setTransactionId(null);
        int updatedRows = reservationDAO.updatePaymentStatus(reservation);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Reservation introuvable pour echec de paiement.");
        }
    }

    public List<Reservation> getByEmail(String emailParticipant) {
        if (isBlank(emailParticipant) || !emailParticipant.contains("@")) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        return reservationDAO.findByEmail(emailParticipant.trim());
    }

    public List<ReservationHistoryItem> getHistoryByEmail(String emailParticipant) {
        if (isBlank(emailParticipant) || !emailParticipant.contains("@")) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        return reservationDAO.findHistoryByEmail(emailParticipant.trim());
    }

    public Map<Integer, Long> countValidReservationsByEvent() {
        return reservationDAO.countValidReservationsByEvent();
    }

    public ReservationHistoryItem validateQrToken(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("QR invalide : saisissez ou scannez un token.");
        }

        ReservationHistoryItem item = reservationDAO.findByQrToken(token.trim());
        if (item == null || item.getReservation() == null) {
            throw new IllegalArgumentException("QR invalide.");
        }

        Reservation reservation = item.getReservation();
        String status = normalizeStatus(reservation.getStatut());
        if ("annulee".equals(status) || "cancelled".equals(status)) {
            throw new IllegalArgumentException("Reservation annulee : entree refusee.");
        }
        if ("utilisee".equals(status) || "used".equals(status) || reservation.getUsedAt() != null) {
            throw new IllegalArgumentException("QR deja utilise.");
        }
        if ("en_attente_paiement".equals(status) || "echec_paiement".equals(status)) {
            throw new IllegalArgumentException("Reservation non payee ou paiement refuse : entree refusee.");
        }
        if (!"confirmee".equals(status) && !"payee".equals(status)) {
            throw new IllegalArgumentException("Reservation non eligible au check-in.");
        }

        return item;
    }

    public void confirmCheckin(Reservation reservation) {
        if (reservation == null || reservation.getId() <= 0) {
            throw new IllegalArgumentException("Reservation invalide.");
        }
        if (reservation.getUsedAt() != null) {
            throw new IllegalArgumentException("QR deja utilise.");
        }

        int updatedRows = reservationDAO.markAsUsed(reservation.getId());
        if (updatedRows == 0) {
            throw new IllegalArgumentException("QR deja utilise ou reservation non eligible.");
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setStatut("Utilisee");
        reservation.setCheckedInAt(now);
        reservation.setUsedAt(now);
    }

    public void cancel(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reservation est invalide.");
        }
        cancelReservation(reservation.getId());
    }

    public CancelReservationResult cancelReservation(int reservationId) {
        if (reservationId <= 0) {
            throw new IllegalArgumentException("La reservation est invalide.");
        }

        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation introuvable.");
        }
        if (!isCancelable(reservation)) {
            throw new IllegalArgumentException("Seules les reservations Confirmee ou PAYEE sont annulables.");
        }

        int updatedRows = reservationDAO.cancelReservationIfCancelable(reservationId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Cette reservation est deja annulee ou non annulable.");
        }

        participationService.deleteByEventAndEmail(reservation.getIdEvent(), reservation.getEmailParticipant());
        Reservation promotedReservation = waitlistService.promoteNextFromWaitlist(reservation.getIdEvent());

        reservation.setStatut("Annulee");
        return new CancelReservationResult(reservation, promotedReservation);
    }

    public boolean isCancelable(Reservation reservation) {
        if (reservation == null) {
            return false;
        }
        String status = normalizeStatus(reservation.getStatut());
        return "confirmee".equals(status) || "payee".equals(status);
    }

    public boolean isValidForLoyalty(Reservation reservation) {
        String status = normalizeStatus(reservation == null ? null : reservation.getStatut());
        return "confirmee".equals(status) || "payee".equals(status) || "utilisee".equals(status);
    }

    private boolean shouldAttachQr(Reservation reservation) {
        String status = normalizeStatus(reservation.getStatut());
        return "confirmee".equals(status) || "payee".equals(status);
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

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("confirmed".equals(normalized)) {
            return "confirmee";
        }
        if ("used".equals(normalized)) {
            return "utilisee";
        }
        if ("paid".equals(normalized)) {
            return "payee";
        }
        if ("payee".equals(normalized) || "payée".equals(normalized)) {
            return "payee";
        }
        return normalized;
    }
}
