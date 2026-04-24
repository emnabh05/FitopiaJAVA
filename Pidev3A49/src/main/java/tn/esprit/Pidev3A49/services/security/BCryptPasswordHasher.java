package tn.esprit.Pidev3A49.services.security;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHasher implements PasswordHasher {
    private static final int COST = 12;

    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(COST));
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        String normalizedHash = normalizeHash(hashedPassword);
        return isHashFormat(normalizedHash) && BCrypt.checkpw(rawPassword, normalizedHash);
    }

    @Override
    public boolean isHashFormat(String value) {
        return normalizeHash(value) != null && normalizeHash(value).matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    }

    private String normalizeHash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.startsWith("$2y$")) {
            return "$2a$" + value.substring(4);
        }
        return value;
    }
}
