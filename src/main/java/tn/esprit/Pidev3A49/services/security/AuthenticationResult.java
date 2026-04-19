package tn.esprit.Pidev3A49.services.security;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

public record AuthenticationResult(Status status, FitopiaUser user, String message) {
    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,
        LOCKED,
        SERVICE_UNAVAILABLE
    }
}
