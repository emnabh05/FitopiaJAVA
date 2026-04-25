package tn.esprit.Pidev3A49.models;

import java.time.LocalDateTime;

public class LoyaltyStatus {

    private static final int VIP_THRESHOLD = 5;

    private final String emailParticipant;
    private final String nomParticipant;
    private final int validReservations;
    private final double totalSpent;
    private final LocalDateTime lastPurchase;

    public LoyaltyStatus(String emailParticipant, String nomParticipant, int validReservations,
                         double totalSpent, LocalDateTime lastPurchase) {
        this.emailParticipant = emailParticipant;
        this.nomParticipant = nomParticipant;
        this.validReservations = validReservations;
        this.totalSpent = totalSpent;
        this.lastPurchase = lastPurchase;
    }

    public String getEmailParticipant() {
        return emailParticipant;
    }

    public String getNomParticipant() {
        return nomParticipant;
    }

    public int getValidReservations() {
        return validReservations;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public LocalDateTime getLastPurchase() {
        return lastPurchase;
    }

    public String getTier() {
        if (validReservations >= VIP_THRESHOLD) {
            return "VIP";
        }
        if (validReservations >= 3) {
            return "Silver";
        }
        return "Bronze";
    }

    public int getScore() {
        return validReservations * 10 + (int) Math.round(totalSpent / 10.0);
    }

    public boolean hasVipAccess() {
        return validReservations >= VIP_THRESHOLD;
    }

    public double getProgressRatio() {
        return Math.min(1.0, validReservations / (double) VIP_THRESHOLD);
    }

    public int getReservationsToVip() {
        return Math.max(0, VIP_THRESHOLD - validReservations);
    }

    public String getProgressText() {
        if (hasVipAccess()) {
            return "Statut VIP actif";
        }
        return validReservations + "/" + VIP_THRESHOLD + " reservations valides vers VIP";
    }
}
