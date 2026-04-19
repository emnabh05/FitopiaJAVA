package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import java.util.ArrayList;
import java.util.List;

public class UserAiInsightService {

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
}
