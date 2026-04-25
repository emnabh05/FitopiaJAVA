package tn.esprit.Pidev3A49.models;

public class LoyaltyCustomerSummary {

    private final LoyaltyStatus status;

    public LoyaltyCustomerSummary(LoyaltyStatus status) {
        this.status = status;
    }

    public LoyaltyStatus getStatus() {
        return status;
    }

    public String getEmailParticipant() {
        return status.getEmailParticipant();
    }

    public String getNomParticipant() {
        return status.getNomParticipant();
    }

    public int getValidReservations() {
        return status.getValidReservations();
    }

    public double getTotalSpent() {
        return status.getTotalSpent();
    }

    public java.time.LocalDateTime getLastPurchase() {
        return status.getLastPurchase();
    }

    public String getTier() {
        return status.getTier();
    }

    public int getScore() {
        return status.getScore();
    }

    public boolean isVipAccess() {
        return status.hasVipAccess();
    }
}
