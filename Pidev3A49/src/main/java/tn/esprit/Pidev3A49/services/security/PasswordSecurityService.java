package tn.esprit.Pidev3A49.services.security;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PasswordSecurityService {
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");
    private static final List<String> COMMON_PASSWORDS = List.of(
            "password", "123456", "123456789", "qwerty", "azerty", "admin",
            "welcome", "fitopia", "abc123", "password123", "letmein", "iloveyou"
    );

    private final PwnedPasswordClient pwnedPasswordClient;

    public PasswordSecurityService(PwnedPasswordClient pwnedPasswordClient) {
        this.pwnedPasswordClient = pwnedPasswordClient;
    }

    public PasswordPolicyReport evaluate(FitopiaUser user, String rawPassword) {
        List<String> feedback = new ArrayList<>();
        int score = 0;

        if (rawPassword == null || rawPassword.isBlank()) {
            feedback.add("Le mot de passe est obligatoire.");
            return new PasswordPolicyReport(0, "EMPTY", false, false, 0, false, feedback);
        }

        if (rawPassword.length() >= 8) {
            score += 15;
        } else {
            feedback.add("Le mot de passe doit contenir au moins 8 caracteres.");
        }
        if (rawPassword.length() >= 12) {
            score += 15;
        }
        if (rawPassword.length() >= 16) {
            score += 10;
        }
        if (UPPER.matcher(rawPassword).find()) {
            score += 10;
        } else {
            feedback.add("Ajoutez au moins une lettre majuscule.");
        }
        if (LOWER.matcher(rawPassword).find()) {
            score += 10;
        } else {
            feedback.add("Ajoutez au moins une lettre minuscule.");
        }
        if (DIGIT.matcher(rawPassword).find()) {
            score += 10;
        } else {
            feedback.add("Ajoutez au moins un chiffre.");
        }
        if (SPECIAL.matcher(rawPassword).find()) {
            score += 10;
        } else {
            feedback.add("Ajoutez au moins un caractere special.");
        }
        if (!containsPersonalData(user, rawPassword)) {
            score += 10;
        } else {
            feedback.add("Le mot de passe contient des informations personnelles predictibles.");
        }
        if (!containsSimplePattern(rawPassword)) {
            score += 10;
        } else {
            feedback.add("Le mot de passe contient un motif trop simple ou trop connu.");
        }

        PwnedPasswordClient.BreachCheckResult breachCheck = pwnedPasswordClient.checkPassword(rawPassword);
        if (breachCheck.checkAvailable()) {
            if (breachCheck.compromised()) {
                feedback.add("Ce mot de passe apparait deja dans des fuites connues (" + breachCheck.occurrences() + " occurrence(s)).");
                score = Math.max(0, score - 40);
            } else {
                score += 10;
            }
        } else {
            feedback.add("Verification des fuites indisponible pour le moment.");
        }

        score = Math.max(0, Math.min(100, score));
        boolean accepted = score >= 70 && !breachCheck.compromised();

        if (accepted && feedback.isEmpty()) {
            feedback.add("Mot de passe robuste.");
        }

        return new PasswordPolicyReport(
                score,
                classify(score, breachCheck.compromised()),
                accepted,
                breachCheck.compromised(),
                breachCheck.occurrences(),
                breachCheck.checkAvailable(),
                feedback
        );
    }

    private boolean containsPersonalData(FitopiaUser user, String rawPassword) {
        String password = rawPassword.toLowerCase(Locale.ROOT);
        List<String> tokens = new ArrayList<>();
        tokens.add(normalize(user == null ? null : user.getFirstName()));
        tokens.add(normalize(user == null ? null : user.getLastName()));
        tokens.add(normalize(user == null ? null : user.getUsername()));
        tokens.add(normalize(extractEmailLocalPart(user == null ? null : user.getEmail())));
        tokens.add(normalizeDigits(user == null ? null : user.getBirthDate()));
        tokens.addAll(buildDateVariants(user == null ? null : user.getBirthDate()));

        for (String token : tokens) {
            if (token != null && token.length() >= 3 && password.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSimplePattern(String rawPassword) {
        String lower = rawPassword.toLowerCase(Locale.ROOT);
        for (String common : COMMON_PASSWORDS) {
            if (lower.contains(common)) {
                return true;
            }
        }
        return hasSequentialChars(lower) || hasRepeatedChars(lower) || hasKeyboardPattern(lower);
    }

    private boolean hasSequentialChars(String value) {
        for (int i = 0; i < value.length() - 2; i++) {
            char a = value.charAt(i);
            char b = value.charAt(i + 1);
            char c = value.charAt(i + 2);
            if ((b == a + 1 && c == b + 1) || (b == a - 1 && c == b - 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRepeatedChars(String value) {
        int streak = 1;
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1)) {
                streak++;
                if (streak >= 4) {
                    return true;
                }
            } else {
                streak = 1;
            }
        }
        return false;
    }

    private boolean hasKeyboardPattern(String value) {
        List<String> patterns = List.of("qwerty", "azerty", "asdf", "zxcv", "987654", "654321");
        for (String pattern : patterns) {
            if (value.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String classify(int score, boolean compromised) {
        if (compromised) {
            return "COMPROMISED";
        }
        if (score >= 90) {
            return "VERY_STRONG";
        }
        if (score >= 75) {
            return "STRONG";
        }
        if (score >= 60) {
            return "MEDIUM";
        }
        if (score >= 40) {
            return "WEAK";
        }
        return "VERY_WEAK";
    }

    private String extractEmailLocalPart(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDigits(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    private List<String> buildDateVariants(String birthDate) {
        List<String> variants = new ArrayList<>();
        String digits = normalizeDigits(birthDate);
        if (digits == null || digits.length() < 4) {
            return variants;
        }
        variants.add(digits);
        if (digits.length() >= 8) {
            variants.add(digits.substring(0, 4));
            variants.add(digits.substring(digits.length() - 4));
            variants.add(digits.substring(0, 2) + digits.substring(2, 4));
            variants.add(digits.substring(4, 8));
        }
        return variants;
    }
}
