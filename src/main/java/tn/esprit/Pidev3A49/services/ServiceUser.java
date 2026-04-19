package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;
import tn.esprit.Pidev3A49.services.security.AdminSecurityAlert;
import tn.esprit.Pidev3A49.services.security.BCryptPasswordHasher;
import tn.esprit.Pidev3A49.services.security.PasswordChangeResult;
import tn.esprit.Pidev3A49.services.security.PasswordHasher;
import tn.esprit.Pidev3A49.services.security.PasswordPolicyReport;
import tn.esprit.Pidev3A49.services.security.PasswordSecurityService;
import tn.esprit.Pidev3A49.services.security.PwnedPasswordClient;
import tn.esprit.Pidev3A49.services.security.UserSecuritySnapshot;
import tn.esprit.Pidev3A49.services.security.EmailOtpService;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class ServiceUser {
    private static final String TABLE_NAME = "fitopia_users";
    private static final String PASSWORD_HISTORY_TABLE = "fitopia_user_password_history";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration RESET_OTP_DURATION = Duration.ofMinutes(10);
    private static final int PASSWORD_HISTORY_LIMIT = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String DEFAULT_RESET_PHONE = "58860916";

    private final Connection cnx;
    private final PasswordHasher passwordHasher = new BCryptPasswordHasher();
    private final PasswordSecurityService passwordSecurityService = new PasswordSecurityService(new PwnedPasswordClient());
    private final EmailOtpService emailOtpService = new EmailOtpService();
    private final SecureRandom secureRandom = new SecureRandom();

    public ServiceUser() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx != null) {
            initializeSchema();
        }
    }

    public boolean isAvailable() {
        return cnx != null;
    }

    public List<FitopiaUser> getAll() {
        ensureConnection();
        List<FitopiaUser> users = new ArrayList<>();
        String query = "SELECT * FROM `" + TABLE_NAME + "` ORDER BY id DESC";

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                FitopiaUser user = mapUser(rs);
                user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des utilisateurs : " + e.getMessage(), e);
        }

        return users;
    }

    public PasswordPolicyReport registerUser(FitopiaUser user, String rawPassword) {
        ensureConnection();
        validateRegistrationPayload(user, rawPassword);
        validateUniqueness(user, 0);

        PasswordPolicyReport report = passwordSecurityService.evaluate(user, rawPassword);
        if (!report.accepted()) {
            throw new RuntimeException(formatPasswordPolicyMessage(report));
        }

        String now = now();
        user.setPassword(passwordHasher.hash(rawPassword));
        user.setPasswordScore(report.score());
        user.setPasswordStrength(report.strengthLabel());
        user.setCompromisedPassword(report.compromised());
        user.setCompromisedOccurrences(report.compromisedOccurrences());
        user.setFailedLoginAttempts(0);
        user.setRiskScore(report.compromised() ? 50 : Math.max(0, 100 - report.score()));
        user.setAccountStatus("ACTIVE");
        user.setPasswordLastChangedAt(now);
        user.setLockedUntil("");
        user.setLastLoginAt("");
        user.setLastFailedLoginAt("");

        String query = "INSERT INTO `" + TABLE_NAME + "` (first_name,last_name,username,email,password,phone,birth_date,gender,role,avatar_path,"
                + "professional_title,specialization,qualification,years_experience,bio,license_number,height,weight,target_weight,"
                + "fitness_level,health_conditions,dietary_preferences,fitness_goals,face_id_enabled,face_image_path,password_score,"
                + "password_strength,compromised_password,compromised_occurrences,failed_login_attempts,risk_score,account_status,password_last_changed_at,"
                + "locked_until,last_login_at,last_failed_login_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstm = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pstm, user, false);
            pstm.executeUpdate();
            try (ResultSet keys = pstm.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            insertPasswordHistory(user.getId(), user.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage(), e);
        }

        user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
        return report;
    }

    public AuthenticationResult authenticateSecure(String identifier, String rawPassword) {
        ensureConnection();
        Optional<FitopiaUser> optionalUser = findByIdentifier(identifier);
        if (optionalUser.isEmpty()) {
            return new AuthenticationResult(AuthenticationResult.Status.INVALID_CREDENTIALS, null, "Email/username ou mot de passe incorrect.");
        }

        FitopiaUser user = optionalUser.get();
        if (isLocked(user)) {
            increaseRiskScore(user.getId(), 5);
            return new AuthenticationResult(
                    AuthenticationResult.Status.LOCKED,
                    refreshUser(user.getId()).orElse(user),
                    "Compte temporairement bloque jusqu'au " + safe(user.getLockedUntil()) + "."
            );
        }

        boolean passwordMatches = passwordHasher.matches(rawPassword, user.getPassword());
        if (!passwordMatches && !passwordHasher.isHashFormat(user.getPassword())) {
            passwordMatches = rawPassword.equals(user.getPassword());
            if (passwordMatches) {
                migrateLegacyPassword(user, rawPassword);
            }
        }

        if (!passwordMatches) {
            return handleFailedAuthentication(user);
        }

        handleSuccessfulAuthentication(user.getId());
        FitopiaUser refreshed = refreshUser(user.getId()).orElse(user);
        return new AuthenticationResult(AuthenticationResult.Status.SUCCESS, refreshed, "Authentification reussie.");
    }

    public Optional<FitopiaUser> authenticate(String identifier, String password) {
        AuthenticationResult result = authenticateSecure(identifier, password);
        return result.status() == AuthenticationResult.Status.SUCCESS ? Optional.of(result.user()) : Optional.empty();
    }

    public Optional<FitopiaUser> findByIdentifier(String identifier) {
        ensureConnection();
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?) LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, identifier);
            pstm.setString(2, identifier);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    FitopiaUser user = mapUser(rs);
                    user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche utilisateur : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<FitopiaUser> authenticateWithFaceId(String identifier) {
        ensureConnection();
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE (LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?)) "
                + "AND face_id_enabled=1 AND face_image_path IS NOT NULL AND face_image_path <> '' LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, identifier);
            pstm.setString(2, identifier);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    FitopiaUser user = mapUser(rs);
                    user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'authentification Face ID : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void enableFaceId(int userId, String faceImagePath) {
        ensureConnection();
        String query = "UPDATE `" + TABLE_NAME + "` SET face_id_enabled=?, face_image_path=? WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setBoolean(1, true);
            pstm.setString(2, faceImagePath);
            pstm.setInt(3, userId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'activation Face ID : " + e.getMessage(), e);
        }
    }

    public void requestPasswordReset(String email) {
        ensureConnection();
        FitopiaUser user = findByEmail(email).orElseThrow(() -> new RuntimeException("Aucun compte n'est associe a cet email."));
        String code = emailOtpService.generateOtpCode();
        String expiresAt = LocalDateTime.now().plus(RESET_OTP_DURATION).truncatedTo(ChronoUnit.SECONDS).format(DATE_FORMATTER);
        String query = "UPDATE `" + TABLE_NAME + "` SET reset_password_code=?, reset_password_expires_at=? WHERE id=?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, code);
            pstm.setString(2, expiresAt);
            pstm.setInt(3, user.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de preparer la reinitialisation du mot de passe : " + e.getMessage(), e);
        }

        try {
            emailOtpService.sendPasswordResetOtp(user.getEmail(), user.getEmail(), code, LocalDateTime.parse(expiresAt, DATE_FORMATTER));
        } catch (RuntimeException e) {
            clearPasswordResetChallenge(user.getId());
            throw e;
        }
    }

    public PasswordResetOtpInfo requestPasswordResetBySmsDemo(String identifier) {
        ensureConnection();
        FitopiaUser user = findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("Aucun compte n'est associe a cet identifiant."));

        String phone = safe(user.getPhone()).isBlank() ? DEFAULT_RESET_PHONE : normalizePhoneForSms(user.getPhone());
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        String expiresAt = LocalDateTime.now().plus(RESET_OTP_DURATION).truncatedTo(ChronoUnit.SECONDS).format(DATE_FORMATTER);
        String query = "UPDATE `" + TABLE_NAME + "` SET reset_password_code=?, reset_password_expires_at=? WHERE id=?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, code);
            pstm.setString(2, expiresAt);
            pstm.setInt(3, user.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de preparer la reinitialisation du mot de passe : " + e.getMessage(), e);
        }

        return new PasswordResetOtpInfo(user.getEmail(), phone, code, expiresAt);
    }

    public PasswordPolicyReport resetPasswordWithOtp(String email, String otpCode, String newPassword) {
        ensureConnection();
        FitopiaUser user = findByEmail(email).orElseThrow(() -> new RuntimeException("Aucun compte n'est associe a cet email."));
        ResetChallenge challenge = getResetChallenge(user.getId())
                .orElseThrow(() -> new RuntimeException("Aucune demande de reinitialisation en attente pour cet email."));

        if (challenge.isExpired()) {
            clearPasswordResetChallenge(user.getId());
            throw new RuntimeException("Le code de reinitialisation a expire.");
        }
        if (!challenge.code().equals(safe(otpCode).trim())) {
            throw new RuntimeException("Code de reinitialisation invalide.");
        }

        PasswordPolicyReport report = changePassword(user.getId(), newPassword, user);
        clearPasswordResetChallenge(user.getId());
        return report;
    }

    public PasswordPolicyReport changePassword(int userId, String rawPassword, FitopiaUser contextUser) {
        return changePasswordSecure(userId, rawPassword, contextUser).report();
    }

    public PasswordChangeResult changePasswordSecure(int userId, String rawPassword, FitopiaUser contextUser) {
        ensureConnection();
        FitopiaUser persisted = refreshUser(userId).orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        FitopiaUser evaluationUser = contextUser == null ? persisted : contextUser;
        evaluationUser.setId(userId);
        validatePasswordChangePayload(rawPassword);
        PasswordPolicyReport report = passwordSecurityService.evaluate(evaluationUser, rawPassword);
        if (!report.accepted()) {
            throw new RuntimeException(formatPasswordPolicyMessage(report));
        }
        if (isPasswordReused(userId, rawPassword, persisted.getPassword())) {
            throw new RuntimeException("Le mot de passe ne peut pas reutiliser les 3 derniers mots de passe.");
        }

        String hashedPassword = passwordHasher.hash(rawPassword);
        String now = now();
        String query = "UPDATE `" + TABLE_NAME + "` SET password=?, password_score=?, password_strength=?, compromised_password=?, "
                + "compromised_occurrences=?, password_last_changed_at=?, risk_score=?, account_status=? WHERE id=?";

        int recalculatedRisk = Math.max(0, persisted.getRiskScore() - 15);
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, hashedPassword);
            pstm.setInt(2, report.score());
            pstm.setString(3, report.strengthLabel());
            pstm.setBoolean(4, report.compromised());
            pstm.setInt(5, report.compromisedOccurrences());
            pstm.setString(6, now);
            pstm.setInt(7, recalculatedRisk);
            pstm.setString(8, "ACTIVE");
            pstm.setInt(9, userId);
            pstm.executeUpdate();
            insertPasswordHistory(userId, hashedPassword);
            trimPasswordHistory(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de mot de passe : " + e.getMessage(), e);
        }
        UserSecuritySnapshot snapshot = getSecuritySnapshot(userId);
        return new PasswordChangeResult(report, snapshot, "Password updated");
    }

    public void updateProfile(FitopiaUser user) {
        ensureConnection();
        validateUniqueness(user, user.getId());
        FitopiaUser persisted = refreshUser(user.getId()).orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        user.setPassword(persisted.getPassword());
        user.setPasswordScore(persisted.getPasswordScore());
        user.setPasswordStrength(persisted.getPasswordStrength());
        user.setCompromisedPassword(persisted.isCompromisedPassword());
        user.setCompromisedOccurrences(persisted.getCompromisedOccurrences());
        user.setFailedLoginAttempts(persisted.getFailedLoginAttempts());
        user.setRiskScore(persisted.getRiskScore());
        user.setAccountStatus(persisted.getAccountStatus());
        user.setPasswordLastChangedAt(persisted.getPasswordLastChangedAt());
        user.setLockedUntil(persisted.getLockedUntil());
        user.setLastLoginAt(persisted.getLastLoginAt());
        user.setLastFailedLoginAt(persisted.getLastFailedLoginAt());

        String query = "UPDATE `" + TABLE_NAME + "` SET first_name=?,last_name=?,username=?,email=?,password=?,phone=?,birth_date=?,gender=?,role=?,avatar_path=?,"
                + "professional_title=?,specialization=?,qualification=?,years_experience=?,bio=?,license_number=?,height=?,weight=?,target_weight=?,"
                + "fitness_level=?,health_conditions=?,dietary_preferences=?,fitness_goals=?,face_id_enabled=?,face_image_path=?,password_score=?,password_strength=?,"
                + "compromised_password=?,compromised_occurrences=?,failed_login_attempts=?,risk_score=?,account_status=?,password_last_changed_at=?,locked_until=?,last_login_at=?,last_failed_login_at=? "
                + "WHERE id=?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            fillStatement(pstm, user, true);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'utilisateur : " + e.getMessage(), e);
        }
    }

    public boolean emailExists(String email) {
        return existsByColumn("email", email);
    }

    public boolean usernameExists(String username) {
        return existsByColumn("username", username);
    }

    public boolean emailExistsForOtherUser(String email, int userId) {
        return existsByColumnForOtherUser("email", email, userId);
    }

    public boolean usernameExistsForOtherUser(String username, int userId) {
        return existsByColumnForOtherUser("username", username, userId);
    }

    public void add(FitopiaUser user) {
        registerUser(user, user.getPassword());
    }

    public void update(FitopiaUser user) {
        updateProfile(user);
    }

    public void delete(int id) {
        ensureConnection();
        try (PreparedStatement history = cnx.prepareStatement("DELETE FROM `" + PASSWORD_HISTORY_TABLE + "` WHERE user_id=?");
             PreparedStatement userDelete = cnx.prepareStatement("DELETE FROM `" + TABLE_NAME + "` WHERE id = ?")) {
            history.setInt(1, id);
            history.executeUpdate();
            userDelete.setInt(1, id);
            userDelete.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur : " + e.getMessage(), e);
        }
    }

    public List<String> buildSecurityAlerts(FitopiaUser user) {
        List<String> alerts = new ArrayList<>();
        if (user == null) {
            alerts.add("Utilisateur introuvable.");
            return alerts;
        }
        if (user.isCompromisedPassword()) {
            alerts.add("Mot de passe compromis detecte.");
        }
        if (user.getPasswordScore() < 70) {
            alerts.add("Mot de passe trop faible (" + user.getPasswordScore() + "/100).");
        }
        if (user.getFailedLoginAttempts() > 0) {
            alerts.add(user.getFailedLoginAttempts() + " tentative(s) de connexion echouee(s).");
        }
        if (isLocked(user)) {
            alerts.add("Compte bloque jusqu'au " + user.getLockedUntil() + ".");
        }
        if (isPasswordExpired(user)) {
            alerts.add("Mot de passe a renouveler.");
        }
        if (user.getRiskScore() >= 70) {
            alerts.add("Score de risque eleve.");
        }
        if (alerts.isEmpty()) {
            alerts.add("Aucune alerte critique.");
        }
        return alerts;
    }

    public String buildSecurityAlertSummary(FitopiaUser user) {
        return String.join(" | ", buildSecurityAlerts(user));
    }

    public String formatPasswordPolicyMessage(PasswordPolicyReport report) {
        return "Mot de passe refuse (" + report.score() + "/100 - " + report.strengthLabel() + ") : "
                + String.join(" ", report.feedback());
    }

    public UserSecuritySnapshot getSecuritySnapshot(int userId) {
        FitopiaUser user = refreshUser(userId).orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        return toSecuritySnapshot(user);
    }

    public List<AdminSecurityAlert> listSecurityAlerts(Integer minRiskScore, String status, boolean compromisedOnly) {
        return getAll().stream()
                .filter(user -> minRiskScore == null || user.getRiskScore() >= minRiskScore)
                .filter(user -> safe(status).isBlank() || safe(user.getAccountStatus()).equalsIgnoreCase(status))
                .filter(user -> !compromisedOnly || user.isCompromisedPassword())
                .filter(user -> !"Aucune alerte critique.".equals(user.getSecurityAlertSummary()) || user.getRiskScore() > 0)
                .map(user -> new AdminSecurityAlert(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRiskScore(),
                        user.getAccountStatus(),
                        user.getSecurityAlertSummary()
                ))
                .collect(Collectors.toList());
    }

    private AuthenticationResult handleFailedAuthentication(FitopiaUser user) {
        int nextAttempts = user.getFailedLoginAttempts() + 1;
        int riskIncrease = nextAttempts >= MAX_FAILED_ATTEMPTS ? 25 : 10;
        String lockedUntil = "";
        String status = "ACTIVE";
        if (nextAttempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil = LocalDateTime.now().plus(LOCK_DURATION).truncatedTo(ChronoUnit.SECONDS).format(DATE_FORMATTER);
            status = "TEMP_LOCKED";
            nextAttempts = 0;
        }

        String query = "UPDATE `" + TABLE_NAME + "` SET failed_login_attempts=?, risk_score=?, account_status=?, locked_until=?, last_failed_login_at=? WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, nextAttempts);
            pstm.setInt(2, Math.min(100, user.getRiskScore() + riskIncrease));
            pstm.setString(3, status);
            pstm.setString(4, lockedUntil);
            pstm.setString(5, now());
            pstm.setInt(6, user.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour des echecs de connexion : " + e.getMessage(), e);
        }

        if (!lockedUntil.isBlank()) {
            return new AuthenticationResult(AuthenticationResult.Status.LOCKED, refreshUser(user.getId()).orElse(user),
                    "Compte temporairement bloque apres plusieurs echecs. Reessayez a " + lockedUntil + ".");
        }
        return new AuthenticationResult(AuthenticationResult.Status.INVALID_CREDENTIALS, null,
                "Email/username ou mot de passe incorrect. Tentatives: " + nextAttempts + "/" + MAX_FAILED_ATTEMPTS + ".");
    }

    private void handleSuccessfulAuthentication(int userId) {
        String query = "UPDATE `" + TABLE_NAME + "` SET failed_login_attempts=0, locked_until='', account_status='ACTIVE', "
                + "risk_score=GREATEST(risk_score - 5, 0), last_login_at=? WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, now());
            pstm.setInt(2, userId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour de la connexion : " + e.getMessage(), e);
        }
    }

    private void migrateLegacyPassword(FitopiaUser user, String rawPassword) {
        String hashedPassword = passwordHasher.hash(rawPassword);
        String query = "UPDATE `" + TABLE_NAME + "` SET password=?, password_last_changed_at=?, password_strength=?, password_score=? WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, hashedPassword);
            pstm.setString(2, now());
            pstm.setString(3, "MIGRATED");
            pstm.setInt(4, 65);
            pstm.setInt(5, user.getId());
            pstm.executeUpdate();
            insertPasswordHistory(user.getId(), hashedPassword);
            trimPasswordHistory(user.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la migration du mot de passe : " + e.getMessage(), e);
        }
    }

    private void insertPasswordHistory(int userId, String hashedPassword) throws SQLException {
        String query = "INSERT INTO `" + PASSWORD_HISTORY_TABLE + "` (user_id,password_hash,created_at) VALUES (?,?,?)";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            pstm.setString(2, hashedPassword);
            pstm.setString(3, now());
            pstm.executeUpdate();
        }
    }

    private void trimPasswordHistory(int userId) throws SQLException {
        String query = "DELETE FROM `" + PASSWORD_HISTORY_TABLE + "` WHERE user_id=? AND id NOT IN ("
                + "SELECT id FROM (SELECT id FROM `" + PASSWORD_HISTORY_TABLE + "` WHERE user_id=? ORDER BY created_at DESC LIMIT ?) keep_ids)";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            pstm.setInt(2, userId);
            pstm.setInt(3, PASSWORD_HISTORY_LIMIT);
            pstm.executeUpdate();
        }
    }

    private boolean isPasswordReused(int userId, String rawPassword, String currentHash) {
        if (passwordHasher.matches(rawPassword, currentHash)) {
            return true;
        }
        String query = "SELECT password_hash FROM `" + PASSWORD_HISTORY_TABLE + "` WHERE user_id=? ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            pstm.setInt(2, PASSWORD_HISTORY_LIMIT);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    if (passwordHasher.matches(rawPassword, rs.getString("password_hash"))) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification de l'historique du mot de passe : " + e.getMessage(), e);
        }
        return false;
    }

    private void validateUniqueness(FitopiaUser user, int userId) {
        if (userId == 0) {
            if (emailExists(user.getEmail())) {
                throw new RuntimeException("Cet email est deja utilise.");
            }
            if (usernameExists(user.getUsername())) {
                throw new RuntimeException("Ce username est deja utilise.");
            }
            return;
        }
        if (emailExistsForOtherUser(user.getEmail(), userId)) {
            throw new RuntimeException("Cet email est deja utilise par un autre compte.");
        }
        if (usernameExistsForOtherUser(user.getUsername(), userId)) {
            throw new RuntimeException("Ce username est deja utilise par un autre compte.");
        }
    }

    private boolean existsByColumn(String column, String value) {
        ensureConnection();
        String query = "SELECT 1 FROM `" + TABLE_NAME + "` WHERE LOWER(" + column + ")=LOWER(?) LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, value);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification " + column + " : " + e.getMessage(), e);
        }
    }

    private boolean existsByColumnForOtherUser(String column, String value, int userId) {
        ensureConnection();
        String query = "SELECT 1 FROM `" + TABLE_NAME + "` WHERE LOWER(" + column + ")=LOWER(?) AND id<>? LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, value);
            pstm.setInt(2, userId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification " + column + " : " + e.getMessage(), e);
        }
    }

    private void initializeSchema() {
        String query = "CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "first_name VARCHAR(100) NOT NULL,"
                + "last_name VARCHAR(100) NOT NULL,"
                + "username VARCHAR(100) NOT NULL UNIQUE,"
                + "email VARCHAR(150) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "phone VARCHAR(50),"
                + "birth_date VARCHAR(50),"
                + "gender VARCHAR(20),"
                + "role VARCHAR(50) NOT NULL,"
                + "avatar_path VARCHAR(255),"
                + "professional_title VARCHAR(150),"
                + "specialization VARCHAR(255),"
                + "qualification VARCHAR(255),"
                + "years_experience VARCHAR(50),"
                + "bio TEXT,"
                + "license_number VARCHAR(120),"
                + "height VARCHAR(50),"
                + "weight VARCHAR(50),"
                + "target_weight VARCHAR(50),"
                + "fitness_level VARCHAR(50),"
                + "health_conditions TEXT,"
                + "dietary_preferences TEXT,"
                + "fitness_goals TEXT,"
                + "face_id_enabled BOOLEAN NOT NULL DEFAULT FALSE,"
                + "face_image_path VARCHAR(255),"
                + "password_score INT NOT NULL DEFAULT 0,"
                + "password_strength VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',"
                + "compromised_password BOOLEAN NOT NULL DEFAULT FALSE,"
                + "compromised_occurrences INT NOT NULL DEFAULT 0,"
                + "failed_login_attempts INT NOT NULL DEFAULT 0,"
                + "risk_score INT NOT NULL DEFAULT 0,"
                + "account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',"
                + "password_last_changed_at VARCHAR(50),"
                + "reset_password_code VARCHAR(20),"
                + "reset_password_expires_at VARCHAR(50),"
                + "locked_until VARCHAR(50),"
                + "last_login_at VARCHAR(50),"
                + "last_failed_login_at VARCHAR(50)"
                + ")";

        String historyQuery = "CREATE TABLE IF NOT EXISTS `" + PASSWORD_HISTORY_TABLE + "` ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id INT NOT NULL,"
                + "password_hash VARCHAR(255) NOT NULL,"
                + "created_at VARCHAR(50) NOT NULL,"
                + "INDEX idx_password_history_user (user_id)"
                + ")";

        try (Statement statement = cnx.createStatement()) {
            statement.executeUpdate(query);
            statement.executeUpdate(historyQuery);
            ensureColumn("face_id_enabled", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN face_id_enabled BOOLEAN NOT NULL DEFAULT FALSE");
            ensureColumn("face_image_path", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN face_image_path VARCHAR(255)");
            ensureColumn("password_score", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN password_score INT NOT NULL DEFAULT 0");
            ensureColumn("password_strength", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN password_strength VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN'");
            ensureColumn("compromised_password", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN compromised_password BOOLEAN NOT NULL DEFAULT FALSE");
            ensureColumn("compromised_occurrences", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN compromised_occurrences INT NOT NULL DEFAULT 0");
            ensureColumn("failed_login_attempts", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0");
            ensureColumn("risk_score", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN risk_score INT NOT NULL DEFAULT 0");
            ensureColumn("account_status", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'");
            ensureColumn("password_last_changed_at", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN password_last_changed_at VARCHAR(50)");
            ensureColumn("reset_password_code", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN reset_password_code VARCHAR(20)");
            ensureColumn("reset_password_expires_at", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN reset_password_expires_at VARCHAR(50)");
            ensureColumn("locked_until", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN locked_until VARCHAR(50)");
            ensureColumn("last_login_at", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN last_login_at VARCHAR(50)");
            ensureColumn("last_failed_login_at", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN last_failed_login_at VARCHAR(50)");
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de preparer la table utilisateur : " + e.getMessage(), e);
        }
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException("Connexion MySQL indisponible. Verifiez la base fitopiabd.");
        }
    }

    private void fillStatement(PreparedStatement pstm, FitopiaUser user, boolean includeId) throws SQLException {
        pstm.setString(1, user.getFirstName());
        pstm.setString(2, user.getLastName());
        pstm.setString(3, user.getUsername());
        pstm.setString(4, user.getEmail());
        pstm.setString(5, user.getPassword());
        pstm.setString(6, user.getPhone());
        pstm.setString(7, user.getBirthDate());
        pstm.setString(8, user.getGender());
        pstm.setString(9, user.getRole());
        pstm.setString(10, user.getAvatarPath());
        pstm.setString(11, user.getProfessionalTitle());
        pstm.setString(12, user.getSpecialization());
        pstm.setString(13, user.getQualification());
        pstm.setString(14, user.getYearsExperience());
        pstm.setString(15, user.getBio());
        pstm.setString(16, user.getLicenseNumber());
        pstm.setString(17, user.getHeight());
        pstm.setString(18, user.getWeight());
        pstm.setString(19, user.getTargetWeight());
        pstm.setString(20, user.getFitnessLevel());
        pstm.setString(21, user.getHealthConditions());
        pstm.setString(22, user.getDietaryPreferences());
        pstm.setString(23, user.getFitnessGoals());
        pstm.setBoolean(24, user.isFaceIdEnabled());
        pstm.setString(25, user.getFaceImagePath());
        pstm.setInt(26, user.getPasswordScore());
        pstm.setString(27, user.getPasswordStrength());
        pstm.setBoolean(28, user.isCompromisedPassword());
        pstm.setInt(29, user.getCompromisedOccurrences());
        pstm.setInt(30, user.getFailedLoginAttempts());
        pstm.setInt(31, user.getRiskScore());
        pstm.setString(32, user.getAccountStatus());
        pstm.setString(33, user.getPasswordLastChangedAt());
        pstm.setString(34, user.getLockedUntil());
        pstm.setString(35, user.getLastLoginAt());
        pstm.setString(36, user.getLastFailedLoginAt());
        if (includeId) {
            pstm.setInt(37, user.getId());
        }
    }

    private FitopiaUser mapUser(ResultSet rs) throws SQLException {
        FitopiaUser user = new FitopiaUser();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setBirthDate(rs.getString("birth_date"));
        user.setGender(rs.getString("gender"));
        user.setRole(rs.getString("role"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setProfessionalTitle(rs.getString("professional_title"));
        user.setSpecialization(rs.getString("specialization"));
        user.setQualification(rs.getString("qualification"));
        user.setYearsExperience(rs.getString("years_experience"));
        user.setBio(rs.getString("bio"));
        user.setLicenseNumber(rs.getString("license_number"));
        user.setHeight(rs.getString("height"));
        user.setWeight(rs.getString("weight"));
        user.setTargetWeight(rs.getString("target_weight"));
        user.setFitnessLevel(rs.getString("fitness_level"));
        user.setHealthConditions(rs.getString("health_conditions"));
        user.setDietaryPreferences(rs.getString("dietary_preferences"));
        user.setFitnessGoals(rs.getString("fitness_goals"));
        user.setFaceIdEnabled(rs.getBoolean("face_id_enabled"));
        user.setFaceImagePath(rs.getString("face_image_path"));
        user.setPasswordScore(rs.getInt("password_score"));
        user.setPasswordStrength(rs.getString("password_strength"));
        user.setCompromisedPassword(rs.getBoolean("compromised_password"));
        user.setCompromisedOccurrences(rs.getInt("compromised_occurrences"));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setRiskScore(rs.getInt("risk_score"));
        user.setAccountStatus(rs.getString("account_status"));
        user.setPasswordLastChangedAt(rs.getString("password_last_changed_at"));
        user.setLockedUntil(rs.getString("locked_until"));
        user.setLastLoginAt(rs.getString("last_login_at"));
        user.setLastFailedLoginAt(rs.getString("last_failed_login_at"));
        return user;
    }

    private void ensureColumn(String columnName, String alterQuery) throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!rs.next()) {
                try (Statement statement = cnx.createStatement()) {
                    statement.executeUpdate(alterQuery);
                }
            }
        }
    }

    private Optional<FitopiaUser> refreshUser(int userId) {
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE id=? LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    FitopiaUser user = mapUser(rs);
                    user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du rechargement utilisateur : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<FitopiaUser> findByEmail(String email) {
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE LOWER(email)=LOWER(?) LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, email);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    FitopiaUser user = mapUser(rs);
                    user.setSecurityAlertSummary(buildSecurityAlertSummary(user));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche par email : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<ResetChallenge> getResetChallenge(int userId) {
        String query = "SELECT reset_password_code, reset_password_expires_at FROM `" + TABLE_NAME + "` WHERE id=? LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    String code = safe(rs.getString("reset_password_code"));
                    String expiresAt = safe(rs.getString("reset_password_expires_at"));
                    if (code.isBlank() || expiresAt.isBlank()) {
                        return Optional.empty();
                    }
                    return Optional.of(new ResetChallenge(code, LocalDateTime.parse(expiresAt, DATE_FORMATTER)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du code de reinitialisation : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private void clearPasswordResetChallenge(int userId) {
        String query = "UPDATE `" + TABLE_NAME + "` SET reset_password_code=NULL, reset_password_expires_at=NULL WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du nettoyage du code de reinitialisation : " + e.getMessage(), e);
        }
    }

    private void increaseRiskScore(int userId, int amount) {
        String query = "UPDATE `" + TABLE_NAME + "` SET risk_score=LEAST(risk_score + ?, 100) WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, amount);
            pstm.setInt(2, userId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour du score de risque : " + e.getMessage(), e);
        }
    }

    private boolean isLocked(FitopiaUser user) {
        if (user == null || safe(user.getLockedUntil()).isBlank()) {
            return false;
        }
        try {
            LocalDateTime lockedUntil = LocalDateTime.parse(user.getLockedUntil(), DATE_FORMATTER);
            return lockedUntil.isAfter(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPasswordExpired(FitopiaUser user) {
        if (user == null || safe(user.getPasswordLastChangedAt()).isBlank()) {
            return true;
        }
        try {
            LocalDateTime changedAt = LocalDateTime.parse(user.getPasswordLastChangedAt(), DATE_FORMATTER);
            return changedAt.plusDays(90).isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return true;
        }
    }

    private String now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DATE_FORMATTER);
    }

    private UserSecuritySnapshot toSecuritySnapshot(FitopiaUser user) {
        return new UserSecuritySnapshot(
                user.getId(),
                user.getPasswordScore(),
                user.getPasswordStrength(),
                user.isCompromisedPassword(),
                user.getCompromisedOccurrences(),
                user.getFailedLoginAttempts(),
                user.getRiskScore(),
                user.getAccountStatus(),
                safe(user.getPasswordLastChangedAt()),
                safe(user.getLockedUntil()),
                safe(user.getLastLoginAt()),
                safe(user.getLastFailedLoginAt()),
                buildSecurityAlerts(user)
        );
    }

    private void validateRegistrationPayload(FitopiaUser user, String rawPassword) {
        if (user == null) {
            throw new RuntimeException("Le payload utilisateur est obligatoire.");
        }
        if (safe(user.getFirstName()).isBlank() || safe(user.getLastName()).isBlank()
                || safe(user.getUsername()).isBlank() || safe(user.getEmail()).isBlank()) {
            throw new RuntimeException("Nom, prenom, username et email sont obligatoires.");
        }
        if (!safe(user.getEmail()).matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Format email invalide.");
        }
        if (safe(user.getUsername()).length() < 3) {
            throw new RuntimeException("Le username doit contenir au moins 3 caracteres.");
        }
        validatePasswordChangePayload(rawPassword);
    }

    private void validatePasswordChangePayload(String rawPassword) {
        if (safe(rawPassword).isBlank()) {
            throw new RuntimeException("Le mot de passe est obligatoire.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizePhoneForSms(String phone) {
        String digits = safe(phone).replaceAll("\\D", "");
        if (digits.isBlank()) {
            return DEFAULT_RESET_PHONE;
        }
        if (digits.startsWith("216") && digits.length() >= 11) {
            return digits.substring(3);
        }
        return digits;
    }

    private record ResetChallenge(String code, LocalDateTime expiresAt) {
        private boolean isExpired() {
            return expiresAt.isBefore(LocalDateTime.now());
        }
    }

    public record PasswordResetOtpInfo(String email, String phone, String code, String expiresAt) {
    }
}
