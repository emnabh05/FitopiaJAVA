package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.dto.LoginRequest;
import tn.esprit.Pidev3A49.api.dto.LoginResponse;
import tn.esprit.Pidev3A49.api.dto.ForgotPasswordRequest;
import tn.esprit.Pidev3A49.api.dto.PasswordResetResponse;
import tn.esprit.Pidev3A49.api.dto.RegisterRequest;
import tn.esprit.Pidev3A49.api.dto.RegisterResponse;
import tn.esprit.Pidev3A49.api.dto.ResetPasswordRequest;
import tn.esprit.Pidev3A49.services.FitopiaUserService;
import tn.esprit.Pidev3A49.services.security.ApiAuthorizationService;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;
import tn.esprit.Pidev3A49.services.security.JwtService;
import tn.esprit.Pidev3A49.services.security.PasswordPolicyReport;

public class AuthController {
    private final FitopiaUserService fitopiaUserService;
    private final JwtService jwtService;
    private final ApiAuthorizationService apiAuthorizationService;

    public AuthController() {
        this(new FitopiaUserService(), new JwtService(), new ApiAuthorizationService());
    }

    public AuthController(FitopiaUserService fitopiaUserService) {
        this(fitopiaUserService, new JwtService(), new ApiAuthorizationService());
    }

    public AuthController(FitopiaUserService fitopiaUserService, JwtService jwtService, ApiAuthorizationService apiAuthorizationService) {
        this.fitopiaUserService = fitopiaUserService;
        this.jwtService = jwtService;
        this.apiAuthorizationService = apiAuthorizationService;
    }

    public RegisterResponse register(RegisterRequest request) {
        FitopiaUser user = new FitopiaUser();
        user.setFirstName(safe(request.firstName()));
        user.setLastName(safe(request.lastName()));
        user.setUsername(safe(request.username()));
        user.setEmail(safe(request.email()));
        user.setBirthDate(safe(request.birthDate()));
        user.setRole(safe(request.role()).isBlank() ? "Patient" : safe(request.role()));
        user.setPhone(safe(request.phone()));
        user.setGender(safe(request.gender()).isBlank() ? "Male" : safe(request.gender()));
        user.setAvatarPath("");
        user.setProfessionalTitle("");
        user.setSpecialization("");
        user.setQualification("");
        user.setYearsExperience("");
        user.setBio(safe(request.bio()));
        user.setLicenseNumber("");
        user.setHeight("");
        user.setWeight("");
        user.setTargetWeight("");
        user.setFitnessLevel("");
        user.setHealthConditions("");
        user.setDietaryPreferences("");
        user.setFitnessGoals("");
        user.setFaceIdEnabled(false);
        user.setFaceImagePath("");

        PasswordPolicyReport report = fitopiaUserService.registerUser(user, safe(request.password()));
        return new RegisterResponse(
                "User created",
                user.getId(),
                report.score(),
                report.strengthLabel(),
                report.compromised(),
                user.getRiskScore(),
                user.getAccountStatus()
        );
    }

    public LoginResponse login(LoginRequest request) {
        AuthenticationResult result = fitopiaUserService.authenticateSecure(safe(request.identifier()), safe(request.password()));
        FitopiaUser user = result.user();
        String token = null;
        if (result.status() == AuthenticationResult.Status.SUCCESS && user != null) {
            token = jwtService.generateAccessToken(user);
        }
        return new LoginResponse(
                result.status(),
                result.message(),
                user == null ? null : user.getId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                user == null ? null : user.getRole(),
                user == null ? null : user.getRiskScore(),
                user == null ? null : user.getAccountStatus(),
                token == null ? null : "Bearer",
                token,
                token == null ? null : apiAuthorizationService.getAccessTokenExpirySeconds()
        );
    }

    public PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request) {
        String email = safe(request.email());
        fitopiaUserService.requestPasswordReset(email);
        return new PasswordResetResponse("Password reset code sent", email);
    }

    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        String email = safe(request.email());
        fitopiaUserService.resetPasswordWithOtp(email, safe(request.otpCode()), safe(request.newPassword()));
        return new PasswordResetResponse("Password updated", email);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

