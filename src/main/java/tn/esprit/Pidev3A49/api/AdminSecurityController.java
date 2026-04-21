package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.AdminSecurityAlert;
import tn.esprit.Pidev3A49.services.security.ApiAuthorizationService;

import java.util.List;

public class AdminSecurityController {
    private final ServiceUser serviceUser;
    private final ApiAuthorizationService apiAuthorizationService;

    public AdminSecurityController() {
        this(new ServiceUser(), new ApiAuthorizationService());
    }

    public AdminSecurityController(ServiceUser serviceUser) {
        this(serviceUser, new ApiAuthorizationService());
    }

    public AdminSecurityController(ServiceUser serviceUser, ApiAuthorizationService apiAuthorizationService) {
        this.serviceUser = serviceUser;
        this.apiAuthorizationService = apiAuthorizationService;
    }

    public List<AdminSecurityAlert> listSecurityAlerts(String authorizationHeader, Integer minRiskScore, String status, boolean compromisedOnly) {
        apiAuthorizationService.requireAdmin(authorizationHeader);
        return serviceUser.listSecurityAlerts(minRiskScore, status, compromisedOnly);
    }
}
