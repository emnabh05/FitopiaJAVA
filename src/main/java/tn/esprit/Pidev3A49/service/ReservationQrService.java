package tn.esprit.Pidev3A49.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.models.Reservation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class ReservationQrService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int MAX_TOKEN_ATTEMPTS = 10;

    private final ReservationDAO reservationDAO;

    public ReservationQrService() {
        this.reservationDAO = new ReservationDAO();
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
        return "fitopia://reservation/check-in?token=" + reservation.getQrToken();
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
}
