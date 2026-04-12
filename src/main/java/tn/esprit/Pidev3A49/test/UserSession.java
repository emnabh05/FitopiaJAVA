package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

public final class UserSession {
    private static FitopiaUser currentUser;

    private UserSession() {
    }

    public static FitopiaUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(FitopiaUser user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
