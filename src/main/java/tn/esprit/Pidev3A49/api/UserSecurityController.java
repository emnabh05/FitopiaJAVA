package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.api.dto.ChangePasswordRequest;
import tn.esprit.Pidev3A49.api.dto.ChangePasswordResponse;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.PasswordChangeResult;
import tn.esprit.Pidev3A49.services.security.UserSecuritySnapshot;

public class UserSecurityController {
    private final ServiceUser serviceUser;

    public UserSecurityController() {
        this(new ServiceUser());
    }

    public UserSecurityController(ServiceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    public ChangePasswordResponse changePassword(int userId, ChangePasswordRequest request) {
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

    public UserSecuritySnapshot getSecuritySnapshot(int userId) {
        return serviceUser.getSecuritySnapshot(userId);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
