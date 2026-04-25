package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.models.PaymentResult;
import tn.esprit.Pidev3A49.models.Reservation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class PaymentService {

    private static final double SANDBOX_SUCCESS_RATE = 0.90;
    private static final DateTimeFormatter TRANSACTION_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);

    private final SecureRandom random = new SecureRandom();

    public void validatePaymentForm(String cardHolderName, String cardNumber, String expirationDate,
                                    String cvv, String paymentMethod) {
        if (isBlank(paymentMethod)) {
            throw new IllegalArgumentException("Choisissez un moyen de paiement.");
        }
        if (isBlank(cardHolderName)) {
            throw new IllegalArgumentException("Le nom sur la carte est obligatoire.");
        }

        String normalizedCardNumber = normalizeDigits(cardNumber);
        if (normalizedCardNumber.length() != 16) {
            throw new IllegalArgumentException("Le numero de carte doit contenir exactement 16 chiffres.");
        }
        if (!normalizedCardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Le numero de carte ne doit contenir que des chiffres.");
        }

        if (isBlank(expirationDate)) {
            throw new IllegalArgumentException("La date d'expiration est obligatoire.");
        }
        validateExpirationDate(expirationDate.trim());

        String normalizedCvv = normalizeDigits(cvv);
        if (!normalizedCvv.matches("\\d{3}")) {
            throw new IllegalArgumentException("Le CVV doit contenir exactement 3 chiffres.");
        }
    }

    public PaymentResult processPayment(Reservation reservation) {
        return processPayment(reservation, "Carte bancaire");
    }

    public PaymentResult processPayment(Reservation reservation, String paymentMethod) {
        validate(reservation);

        boolean success = simulatePaymentResult();
        if (!success) {
            return new PaymentResult(false, null, "Paiement refuse. Veuillez reessayer.");
        }

        return new PaymentResult(true, generateTransactionId(paymentMethod), "Paiement sandbox valide.");
    }

    public PaymentResult processSuccessfulTestPayment(Reservation reservation) {
        validate(reservation);
        return new PaymentResult(true, generateTransactionId("TEST"), "Paiement de test valide.");
    }

    public PaymentResult processFailedTestPayment(Reservation reservation) {
        validate(reservation);
        return new PaymentResult(false, null, "Paiement refuse. Veuillez reessayer.");
    }

    public String generateTransactionId() {
        return generateTransactionId("SBX");
    }

    public String generateTransactionId(String paymentMethod) {
        String methodCode = normalizePaymentMethod(paymentMethod);
        return "FITOPIA-" + methodCode + "-"
                + TRANSACTION_FORMATTER.format(LocalDateTime.now())
                + "-"
                + Math.abs(random.nextInt(900_000) + 100_000);
    }

    public boolean simulatePaymentResult() {
        return random.nextDouble() < SANDBOX_SUCCESS_RATE;
    }

    private void validate(Reservation reservation) {
        if (reservation == null || reservation.getId() <= 0) {
            throw new IllegalArgumentException("La reservation a payer est invalide.");
        }
        if (reservation.getMontant() <= 0) {
            throw new IllegalArgumentException("Cette reservation ne necessite pas de paiement.");
        }
    }

    private void validateExpirationDate(String expirationDate) {
        if (!expirationDate.matches("\\d{2}/\\d{2}")) {
            throw new IllegalArgumentException("La date d'expiration doit etre au format MM/AA.");
        }

        try {
            int month = Integer.parseInt(expirationDate.substring(0, 2));
            int year = Integer.parseInt("20" + expirationDate.substring(3, 5));
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Le mois d'expiration doit etre entre 01 et 12.");
            }
            YearMonth cardExpiration = YearMonth.of(year, month);
            if (cardExpiration.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException("La carte est expiree.");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("La date d'expiration est invalide.");
        }
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (isBlank(paymentMethod)) {
            return "SBX";
        }
        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("PAYPAL")) {
            return "PAYPAL";
        }
        if (normalized.contains("WALLET")) {
            return "WALLET";
        }
        if (normalized.contains("TEST")) {
            return "TEST";
        }
        return "CARD";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
