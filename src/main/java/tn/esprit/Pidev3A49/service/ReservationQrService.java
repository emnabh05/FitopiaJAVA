package tn.esprit.Pidev3A49.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import tn.esprit.Pidev3A49.dao.EventDAO;
import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Reservation;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

public class ReservationQrService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int MAX_TOKEN_ATTEMPTS = 10;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    private final ReservationDAO reservationDAO;
    private final EventDAO eventDAO;

    public ReservationQrService() {
        this.reservationDAO = new ReservationDAO();
        this.eventDAO = new EventDAO();
    }

    public void attachQrToReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reservation est obligatoire.");
        }
        if (reservation.getQrToken() == null || reservation.getQrToken().isBlank()) {
            reservation.setQrToken(generateUniqueToken());
        }
        if (reservation.getQrGeneratedAt() == null) {
            reservation.setQrGeneratedAt(LocalDateTime.now());
        }
    }

    public WritableImage generateQrImage(Reservation reservation, int size) {
        if (reservation == null || reservation.getQrToken() == null || reservation.getQrToken().isBlank()) {
            throw new IllegalArgumentException("Cette reservation ne possede pas de QR token.");
        }
        return generateQrImage(buildQrPayload(reservation), size);
    }

    public String buildQrPayload(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reservation est obligatoire.");
        }

        Event event = reservation.getIdEvent() > 0 ? eventDAO.findById(reservation.getIdEvent()) : null;
        return "FITOPIA - TICKET EVENEMENT\n\n"
                + "Evenement : " + nullSafe(event == null ? null : event.getTitre()) + "\n"
                + "Date : " + formatDate(event == null ? null : event.getDateEvent()) + "\n"
                + "Lieu : " + nullSafe(event == null ? null : event.getLieu()) + "\n"
                + "Prix : " + formatPrice(event == null ? reservation.getMontant() : event.getPrixEvent()) + "\n\n"
                + "Participant : " + nullSafe(reservation.getNomParticipant()) + "\n"
                + "Email : " + nullSafe(reservation.getEmailParticipant()) + "\n"
                + "Statut : " + nullSafe(reservation.getStatut()) + "\n\n"
                + "Token check-in : " + nullSafe(reservation.getQrToken());
    }

    private WritableImage generateQrImage(String payload, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
            WritableImage image = new WritableImage(size, size);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    image.getPixelWriter().setColor(x, y, matrix.get(x, y) ? Color.web("#173d32") : Color.WHITE);
                }
            }
            return image;
        } catch (WriterException e) {
            throw new IllegalStateException("Impossible de generer l'image QR.", e);
        }
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_ATTEMPTS; attempt++) {
            String token = generateToken();
            if (!reservationDAO.existsByQrToken(token)) {
                return token;
            }
        }
        throw new IllegalStateException("Impossible de generer un token QR unique.");
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f DT", price);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
