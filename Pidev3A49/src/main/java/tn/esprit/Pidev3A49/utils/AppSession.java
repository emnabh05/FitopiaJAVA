package tn.esprit.Pidev3A49.utils;

import java.util.Set;

public final class AppSession {

    private static final AppSession INSTANCE = new AppSession();

    private final String email = "ahmed@gmail.com";
    private final Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_COACH");

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public boolean isAuthenticated() {
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getPrimaryRole() {
        return roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN" : roles.iterator().next();
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... expectedRoles) {
        for (String expectedRole : expectedRoles) {
            if (roles.contains(expectedRole)) {
                return true;
            }
        }
        return false;
    }
}
