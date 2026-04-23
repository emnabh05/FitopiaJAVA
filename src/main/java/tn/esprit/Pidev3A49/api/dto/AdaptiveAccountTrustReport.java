package tn.esprit.Pidev3A49.api.dto;

public class AdaptiveAccountTrustReport {
    private int userId;
    private String fullName;
    private String email;
    private String role;
    private String actionContext;
    private String jwtStatus;
    private int trustScore;
    private String riskLevel;
    private String decision;
    private int profileCompletionPercent;
    private int failedLoginAttempts;
    private int resetRequests24h;
    private int resetFailures24h;
    private int resetAttempts24h;
    private int accountSeniorityDays;
    private int inactivityDays;
    private String lastActivityAt;
    private String trustSummary;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getActionContext() {
        return actionContext;
    }

    public void setActionContext(String actionContext) {
        this.actionContext = actionContext;
    }

    public String getJwtStatus() {
        return jwtStatus;
    }

    public void setJwtStatus(String jwtStatus) {
        this.jwtStatus = jwtStatus;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public int getProfileCompletionPercent() {
        return profileCompletionPercent;
    }

    public void setProfileCompletionPercent(int profileCompletionPercent) {
        this.profileCompletionPercent = profileCompletionPercent;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public int getResetRequests24h() {
        return resetRequests24h;
    }

    public void setResetRequests24h(int resetRequests24h) {
        this.resetRequests24h = resetRequests24h;
    }

    public int getResetFailures24h() {
        return resetFailures24h;
    }

    public void setResetFailures24h(int resetFailures24h) {
        this.resetFailures24h = resetFailures24h;
    }

    public int getResetAttempts24h() {
        return resetAttempts24h;
    }

    public void setResetAttempts24h(int resetAttempts24h) {
        this.resetAttempts24h = resetAttempts24h;
    }

    public int getAccountSeniorityDays() {
        return accountSeniorityDays;
    }

    public void setAccountSeniorityDays(int accountSeniorityDays) {
        this.accountSeniorityDays = accountSeniorityDays;
    }

    public int getInactivityDays() {
        return inactivityDays;
    }

    public void setInactivityDays(int inactivityDays) {
        this.inactivityDays = inactivityDays;
    }

    public String getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(String lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getTrustSummary() {
        return trustSummary;
    }

    public void setTrustSummary(String trustSummary) {
        this.trustSummary = trustSummary;
    }
}
