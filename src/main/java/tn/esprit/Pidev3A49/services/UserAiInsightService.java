package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UserAiInsightService {
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    public UserInsight buildInsight(FitopiaUser user) {
        if (user == null) {
            return new UserInsight(
                    "Aucune session active.",
                    "Niveau de confiance: faible",
                    "API avancees conseillees: OAuth2/JWT, audit login, webhook de notification.",
                    "IA conseillee: scoring de profil, detection d'anomalie sur connexion, recommandations personnalisees."
            );
        }

        int completeness = computeCompleteness(user);
        String security = computeSecurity(user);
        String apiIdeas = buildApiIdeas(user);
        String aiIdeas = buildAiIdeas(user, completeness);

        return new UserInsight(
                "Profil " + completeness + "% complet pour " + safe(user.getRole()) + ". "
                        + buildMissingFieldsSummary(user),
                security,
                apiIdeas,
                aiIdeas
        );
    }

    public ProfileCompletionSuggestion buildProfileCompletionSuggestion(FitopiaUser user) {
        if (user == null) {
            return new ProfileCompletionSuggestion("", "", "", "", "", "", List.of("Aucun profil a analyser."));
        }

        String normalizedFirstName = normalizeName(user.getFirstName());
        String normalizedLastName = normalizeName(user.getLastName());
        String normalizedPhone = normalizePhone(user.getPhone());
        String suggestedUsername = suggestUsername(normalizedFirstName, normalizedLastName, user.getUsername(), user.getRole());
        String improvedBio = improveBio(user, normalizedFirstName, normalizedLastName);
        String profileSummary = buildProfileSummary(user, normalizedFirstName, normalizedLastName, normalizedPhone, suggestedUsername, improvedBio);

        List<String> improvements = new ArrayList<>();
        if (!safe(user.getFirstName()).equals(normalizedFirstName)) {
            improvements.add("Prenom reformate en ecriture propre.");
        }
        if (!safe(user.getLastName()).equals(normalizedLastName)) {
            improvements.add("Nom reformate pour un affichage plus professionnel.");
        }
        if (!safe(user.getPhone()).equals(normalizedPhone) && !normalizedPhone.isBlank()) {
            improvements.add("Telephone normalise au format international.");
        }
        if (!safe(user.getUsername()).equals(suggestedUsername) && !suggestedUsername.isBlank()) {
            improvements.add("Username suggere a partir du nom et du role.");
        }
        if (!safe(user.getBio()).equals(improvedBio) && !improvedBio.isBlank()) {
            improvements.add("Bio reformulee avec un ton plus professionnel.");
        }
        if (improvements.isEmpty()) {
            improvements.add("Le profil est deja bien structure.");
        }

        return new ProfileCompletionSuggestion(
                normalizedFirstName,
                normalizedLastName,
                normalizedPhone,
                suggestedUsername,
                improvedBio,
                profileSummary,
                improvements
        );
    }

    private int computeCompleteness(FitopiaUser user) {
        int total = 10;
        int score = 0;
        score += present(user.getFirstName());
        score += present(user.getLastName());
        score += present(user.getUsername());
        score += present(user.getEmail());
        score += present(user.getPhone());
        score += present(user.getBirthDate());
        score += present(user.getAvatarPath());
        score += present(user.getBio());
        score += user.isFaceIdEnabled() ? 1 : 0;
        score += present(user.getRole());
        return Math.round((score * 100f) / total);
    }

    private String computeSecurity(FitopiaUser user) {
        List<String> flags = new ArrayList<>();
        if (!user.isFaceIdEnabled()) {
            flags.add("Face ID non active");
        }
        if (user.getPasswordScore() < 70) {
            flags.add("mot de passe faible " + user.getPasswordScore() + "/100");
        }
        if (user.isCompromisedPassword()) {
            flags.add("mot de passe compromis");
        }
        if (user.getFailedLoginAttempts() > 0) {
            flags.add("echecs de connexion=" + user.getFailedLoginAttempts());
        }
        if ("TEMP_LOCKED".equalsIgnoreCase(safe(user.getAccountStatus()))) {
            flags.add("compte temporairement bloque");
        }
        if (safe(user.getPhone()).isBlank()) {
            flags.add("telephone absent");
        }
        if (safe(user.getEmail()).endsWith("@gmail.com") || safe(user.getEmail()).endsWith("@yahoo.com")) {
            flags.add("email public");
        }

        if (flags.isEmpty()) {
            return "Niveau de confiance: eleve. Score mot de passe " + user.getPasswordScore()
                    + "/100. Compte pret pour MFA, audit et verification contextuelle.";
        }
        return "Niveau de confiance: moyen. Points a corriger: " + String.join(", ", flags) + ".";
    }

    private String buildApiIdeas(FitopiaUser user) {
        return "APIs avancees a integrer: `POST /api/users/{id}/faceid/enroll`, `POST /api/auth/faceid/login`, "
                + "`GET /api/users/{id}/risk-score`, `POST /api/users/{id}/notifications/webhook` pour "
                + safe(user.getRole()).toLowerCase() + ".";
    }

    private String buildAiIdeas(FitopiaUser user, int completeness) {
        StringBuilder builder = new StringBuilder("IA activee: ");
        builder.append("score de completion=").append(completeness).append("%, ");
        builder.append("suggestion automatique du parcours utilisateur, ");
        builder.append("detection de compte incomplet, ");
        builder.append("priorite de securite basee sur Face ID et profil.");

        if ("Patient".equalsIgnoreCase(user.getRole())) {
            builder.append(" Extension recommandee: moteur de recommandations nutrition/fitness personnalise.");
        } else if ("Coach".equalsIgnoreCase(user.getRole()) || "Nutritionist".equalsIgnoreCase(user.getRole())) {
            builder.append(" Extension recommandee: classement intelligent des clients a risque ou inactifs.");
        } else if ("Admin".equalsIgnoreCase(user.getRole())) {
            builder.append(" Extension recommandee: detection d'anomalie sur creation massive de comptes et connexions suspectes.");
        }

        return builder.toString();
    }

    private String normalizeName(String raw) {
        String value = MULTI_SPACE.matcher(safe(raw).trim()).replaceAll(" ");
        if (value.isBlank()) {
            return "";
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private String normalizePhone(String raw) {
        String digits = safe(raw).replaceAll("\\D", "");
        if (digits.isBlank()) {
            return "";
        }
        if (digits.startsWith("216") && digits.length() == 11) {
            return "+216 " + digits.substring(3, 5) + " " + digits.substring(5, 8) + " " + digits.substring(8);
        }
        if (digits.length() == 8) {
            return "+216 " + digits.substring(0, 2) + " " + digits.substring(2, 5) + " " + digits.substring(5);
        }
        if (digits.length() == 10 && digits.startsWith("00")) {
            return "+" + digits.substring(2);
        }
        return "+" + digits;
    }

    private String suggestUsername(String firstName, String lastName, String currentUsername, String role) {
        if (!safe(currentUsername).isBlank() && safe(currentUsername).length() >= 4) {
            return safe(currentUsername).trim().toLowerCase(Locale.ROOT);
        }
        String first = slug(firstName);
        String last = slug(lastName);
        String roleSuffix = roleSuffix(role);
        if (!first.isBlank() && !last.isBlank()) {
            return first + "." + last + roleSuffix;
        }
        if (!first.isBlank()) {
            return first + roleSuffix;
        }
        return last + roleSuffix;
    }

    private String improveBio(FitopiaUser user, String firstName, String lastName) {
        if (safe(user.getBio()).trim().length() >= 40) {
            return refineSentence(safe(user.getBio()).trim());
        }

        String role = safe(user.getRole());
        if ("Coach".equalsIgnoreCase(role) || "Nutritionist".equalsIgnoreCase(role)) {
            String title = safe(user.getProfessionalTitle()).isBlank() ? role : user.getProfessionalTitle();
            String specialization = safe(user.getSpecialization());
            String qualification = safe(user.getQualification());
            String experience = safe(user.getYearsExperience());

            StringBuilder builder = new StringBuilder();
            builder.append(firstName.isBlank() ? "Professionnel" : firstName + " " + lastName);
            builder.append(" est ");
            builder.append(title.isBlank() ? "un expert" : articleFor(title) + " " + title.toLowerCase(Locale.ROOT));
            if (!specialization.isBlank()) {
                builder.append(" specialise en ").append(specialization.toLowerCase(Locale.ROOT));
            }
            if (!qualification.isBlank()) {
                builder.append(", avec une qualification en ").append(qualification.toLowerCase(Locale.ROOT));
            }
            if (!experience.isBlank()) {
                builder.append(" et ").append(experience).append(" an(s) d'experience");
            }
            builder.append(". Son objectif est d'accompagner chaque utilisateur avec une approche claire, humaine et personnalisee.");
            return refineSentence(builder.toString());
        }

        StringBuilder builder = new StringBuilder();
        builder.append(firstName.isBlank() ? "Utilisateur" : firstName + " " + lastName);
        builder.append(" dispose d'un profil ");
        builder.append(role.isBlank() ? "Fitopia" : role.toLowerCase(Locale.ROOT));
        builder.append(" centre sur des objectifs de progression, de bien-etre et de suivi personnalise.");
        if (!safe(user.getFitnessGoals()).isBlank()) {
            builder.append(" Objectifs principaux: ").append(safe(user.getFitnessGoals()).trim()).append(".");
        }
        return refineSentence(builder.toString());
    }

    private String buildProfileSummary(FitopiaUser user, String firstName, String lastName, String phone, String username, String bio) {
        List<String> parts = new ArrayList<>();
        parts.add((firstName + " " + lastName).trim());
        if (!safe(user.getRole()).isBlank()) {
            parts.add("role " + safe(user.getRole()));
        }
        if (!username.isBlank()) {
            parts.add("username suggere " + username);
        }
        if (!safe(user.getEmail()).isBlank()) {
            parts.add("contact " + safe(user.getEmail()));
        }
        if (!phone.isBlank()) {
            parts.add("telephone " + phone);
        }
        if (!safe(user.getSpecialization()).isBlank()) {
            parts.add("specialisation " + safe(user.getSpecialization()));
        } else if (!safe(user.getFitnessGoals()).isBlank()) {
            parts.add("objectif principal " + safe(user.getFitnessGoals()));
        }
        return String.join(" | ", parts) + "\n\nBio recommandee:\n" + bio;
    }

    private String refineSentence(String text) {
        String value = MULTI_SPACE.matcher(safe(text).trim()).replaceAll(" ");
        if (value.isBlank()) {
            return "";
        }
        String normalized = Character.toUpperCase(value.charAt(0)) + value.substring(1);
        return normalized.endsWith(".") ? normalized : normalized + ".";
    }

    private String articleFor(String title) {
        String lower = safe(title).toLowerCase(Locale.ROOT);
        if (lower.startsWith("nutrition")) {
            return "un";
        }
        if (lower.startsWith("admin")) {
            return "un";
        }
        return "un";
    }

    private String slug(String value) {
        return safe(value).trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    private String roleSuffix(String role) {
        if ("Coach".equalsIgnoreCase(role)) {
            return ".coach";
        }
        if ("Nutritionist".equalsIgnoreCase(role)) {
            return ".nutri";
        }
        if ("Admin".equalsIgnoreCase(role)) {
            return ".admin";
        }
        return "";
    }

    private String buildMissingFieldsSummary(FitopiaUser user) {
        List<String> missing = new ArrayList<>();
        if (safe(user.getPhone()).isBlank()) {
            missing.add("telephone");
        }
        if (safe(user.getAvatarPath()).isBlank()) {
            missing.add("avatar");
        }
        if (safe(user.getBio()).isBlank()) {
            missing.add("bio");
        }
        if (!user.isFaceIdEnabled()) {
            missing.add("Face ID");
        }
        if (missing.isEmpty()) {
            return "Aucun champ critique manquant.";
        }
        return "Champs a completer: " + String.join(", ", missing) + ".";
    }

    private int present(String value) {
        return safe(value).isBlank() ? 0 : 1;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record UserInsight(String profileSummary, String securitySummary, String apiIdeas, String aiIdeas) {
    }

    public record ProfileCompletionSuggestion(
            String suggestedFirstName,
            String suggestedLastName,
            String normalizedPhone,
            String suggestedUsername,
            String improvedBio,
            String profileSummary,
            List<String> improvements
    ) {
    }
}
