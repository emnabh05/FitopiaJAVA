package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.dto.LoginRequest;
import tn.esprit.Pidev3A49.api.dto.LoginResponse;
import tn.esprit.Pidev3A49.api.dto.RegisterRequest;
import tn.esprit.Pidev3A49.api.dto.RegisterResponse;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.AuthenticationResult;
import tn.esprit.Pidev3A49.services.security.PasswordPolicyReport;

public class AuthController {
    private final ServiceUser serviceUser;

    public AuthController() {
        this(new ServiceUser());
    }

    public AuthController(ServiceUser serviceUser) {
        this.serviceUser = serviceUser;
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
        user.setBio("");
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

        PasswordPolicyReport report = serviceUser.registerUser(user, safe(request.password()));
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
        AuthenticationResult result = serviceUser.authenticateSecure(safe(request.identifier()), safe(request.password()));
        FitopiaUser user = result.user();
        return new LoginResponse(
                result.status(),
                result.message(),
                user == null ? null : user.getId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                user == null ? null : user.getRiskScore(),
                user == null ? null : user.getAccountStatus()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
