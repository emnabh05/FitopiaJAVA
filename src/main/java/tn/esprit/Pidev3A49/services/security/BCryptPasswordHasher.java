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
        return isHashFormat(hashedPassword) && BCrypt.checkpw(rawPassword, hashedPassword);
    }

    @Override
    public boolean isHashFormat(String value) {
        return value != null && value.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    }
}
