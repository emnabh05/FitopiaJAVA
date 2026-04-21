package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

public final class UserSession {
    private static FitopiaUser currentUser;
    private static boolean verifyFaceIdOnly;
    private static String accessToken;
    private static long accessTokenExpiresInSeconds;

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
        verifyFaceIdOnly = false;
        accessToken = null;
        accessTokenExpiresInSeconds = 0L;
    }

    public static boolean isVerifyFaceIdOnly() {
        return verifyFaceIdOnly;
    }

    public static void setVerifyFaceIdOnly(boolean verifyFaceIdOnly) {
        UserSession.verifyFaceIdOnly = verifyFaceIdOnly;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        UserSession.accessToken = accessToken;
    }

    public static long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiresInSeconds;
    }

    public static void setAccessTokenExpiresInSeconds(long accessTokenExpiresInSeconds) {
        UserSession.accessTokenExpiresInSeconds = accessTokenExpiresInSeconds;
    }

    public static String getAuthorizationHeader() {
        String token = accessToken == null ? "" : accessToken.trim();
        if (token.isBlank()) {
            return "";
        }
        return "Bearer " + token;
    }
}
