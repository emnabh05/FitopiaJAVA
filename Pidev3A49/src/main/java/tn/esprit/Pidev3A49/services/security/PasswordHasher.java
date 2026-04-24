package tn.esprit.Pidev3A49.services.security;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);

    boolean isHashFormat(String value);
}
