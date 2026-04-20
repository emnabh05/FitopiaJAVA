package tn.esprit.Pidev3A49.api;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;
import tn.esprit.Pidev3A49.services.UserAiInsightService;

import java.util.List;

public class UserArchiveController {
    private final ServiceUser serviceUser;
    private final UserAiInsightService userAiInsightService;

    public UserArchiveController() {
        this(new ServiceUser(), new UserAiInsightService());
    }

    public UserArchiveController(ServiceUser serviceUser, UserAiInsightService userAiInsightService) {
        this.serviceUser = serviceUser;
        this.userAiInsightService = userAiInsightService;
    }

    public void archiveUser(int userId) {
        serviceUser.archiveUser(userId);
    }

    public void restoreUser(int userId) {
        serviceUser.restoreUser(userId);
    }

    public List<FitopiaUser> listUsers(Boolean archived) {
        return serviceUser.getUsersByArchiveState(archived);
    }

    public UserAiInsightService.ArchiveSuggestion suggestArchive(int userId, int inactiveDaysThreshold) {
        FitopiaUser user = serviceUser.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));
        return userAiInsightService.suggestArchiveCandidate(user, inactiveDaysThreshold);
    }
}
