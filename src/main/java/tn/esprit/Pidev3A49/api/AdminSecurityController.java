package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.security.AdminSecurityAlert;

import java.util.List;

public class AdminSecurityController {
    private final ServiceUser serviceUser;

    public AdminSecurityController() {
        this(new ServiceUser());
    }

    public AdminSecurityController(ServiceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    public List<AdminSecurityAlert> listSecurityAlerts(Integer minRiskScore, String status, boolean compromisedOnly) {
        return serviceUser.listSecurityAlerts(minRiskScore, status, compromisedOnly);
    }
}
