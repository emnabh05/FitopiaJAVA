package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.dto.ChangePasswordRequest;
import tn.esprit.Pidev3A49.api.dto.ChangePasswordResponse;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.ApiAuthorizationService;
import tn.esprit.Pidev3A49.services.security.PasswordChangeResult;
import tn.esprit.Pidev3A49.services.security.UserSecuritySnapshot;

public class UserSecurityController {
    private final ServiceUser serviceUser;
    private final ApiAuthorizationService apiAuthorizationService;

    public UserSecurityController() {
        this(new ServiceUser(), new ApiAuthorizationService());
    }

    public UserSecurityController(ServiceUser serviceUser) {
        this(serviceUser, new ApiAuthorizationService());
    }

    public UserSecurityController(ServiceUser serviceUser, ApiAuthorizationService apiAuthorizationService) {
        this.serviceUser = serviceUser;
        this.apiAuthorizationService = apiAuthorizationService;
    }

    public ChangePasswordResponse changePassword(int userId, ChangePasswordRequest request, String authorizationHeader) {
        apiAuthorizationService.requireSelfOrAdmin(authorizationHeader, userId);
        FitopiaUser contextUser = new FitopiaUser();
        contextUser.setFirstName(safe(request.firstName()));
        contextUser.setLastName(safe(request.lastName()));
        contextUser.setUsername(safe(request.username()));
        contextUser.setEmail(safe(request.email()));
        contextUser.setBirthDate(safe(request.birthDate()));

        PasswordChangeResult result = serviceUser.changePasswordSecure(userId, safe(request.newPassword()), contextUser);
        return new ChangePasswordResponse(
                result.message(),
                result.report().score(),
                result.report().strengthLabel(),
                result.report().compromised(),
                result.snapshot().passwordLastChangedAt(),
                result.snapshot().riskScore(),
                result.snapshot().accountStatus()
        );
    }

    public UserSecuritySnapshot getSecuritySnapshot(int userId, String authorizationHeader) {
        apiAuthorizationService.requireSelfOrAdmin(authorizationHeader, userId);
        return serviceUser.getSecuritySnapshot(userId);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
