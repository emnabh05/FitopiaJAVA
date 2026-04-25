package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ReservationDAO;
import tn.esprit.Pidev3A49.models.LoyaltyCustomerSummary;
import tn.esprit.Pidev3A49.models.LoyaltyStatus;

import java.util.List;
import java.util.stream.Collectors;

public class LoyaltyService {

    private final ReservationDAO reservationDAO;

    public LoyaltyService() {
        this.reservationDAO = new ReservationDAO();
    }

    public LoyaltyStatus getStatusByEmail(String emailParticipant) {
        if (isBlank(emailParticipant) || !emailParticipant.contains("@")) {
            throw new IllegalArgumentException("L'email du client est obligatoire.");
        }
        return reservationDAO.findLoyaltyStatusByEmail(emailParticipant.trim());
    }

    public List<LoyaltyCustomerSummary> getAllCustomerSummaries() {
        return reservationDAO.findAllLoyaltyStatuses().stream()
                .map(LoyaltyCustomerSummary::new)
                .collect(Collectors.toList());
    }

    public boolean isVip(String emailParticipant) {
        return getStatusByEmail(emailParticipant).hasVipAccess();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
