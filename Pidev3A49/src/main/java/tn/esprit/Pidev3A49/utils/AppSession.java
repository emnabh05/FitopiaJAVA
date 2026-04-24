package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.test.UserSession;

import java.util.Locale;

public final class AppSession {

    private static final AppSession INSTANCE = new AppSession();

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public boolean isAuthenticated() {
        return UserSession.getCurrentUser() != null;
    }

    public String getEmail() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            return "";
        }
        return currentUser.getEmail().trim();
    }

    public String getUsername() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.getUsername() == null) {
            return "";
        }
        return currentUser.getUsername().trim();
    }

    public String getDisplayName() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "";
        }

        String username = currentUser.getUsername() == null ? "" : currentUser.getUsername().trim();
        if (!username.isBlank()) {
            return username;
        }

        String firstName = currentUser.getFirstName() == null ? "" : currentUser.getFirstName().trim();
        String lastName = currentUser.getLastName() == null ? "" : currentUser.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }

        return getEmail();
    }

    public String getPrimaryRole() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole().isBlank()) {
            return "";
        }
        return normalizeRole(currentUser.getRole());
    }

    public boolean hasRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        return normalizeRole(role).equalsIgnoreCase(getPrimaryRole());
    }

    public boolean hasAnyRole(String... expectedRoles) {
        if (expectedRoles == null) {
            return false;
        }
        for (String expectedRole : expectedRoles) {
            if (hasRole(expectedRole)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }
        return "ROLE_" + normalized;
    }
}
