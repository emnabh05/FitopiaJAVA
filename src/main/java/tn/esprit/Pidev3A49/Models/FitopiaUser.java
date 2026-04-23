package tn.esprit.Pidev3A49.Models;

public class FitopiaUser {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String birthDate;
    private String gender;
    private String role;
    private String avatarPath;
    private String professionalTitle;
    private String specialization;
    private String qualification;
    private String yearsExperience;
    private String bio;
    private String licenseNumber;
    private String height;
    private String weight;
    private String targetWeight;
    private String fitnessLevel;
    private String healthConditions;
    private String dietaryPreferences;
    private String fitnessGoals;
    private boolean faceIdEnabled;
    private String faceImagePath;
    private int passwordScore;
    private String passwordStrength;
    private boolean compromisedPassword;
    private int compromisedOccurrences;
    private int failedLoginAttempts;
    private int riskScore;
    private String accountStatus;
    private String passwordLastChangedAt;
    private String lockedUntil;
    private String lastLoginAt;
    private String lastFailedLoginAt;
    private boolean archived;
    private String archivedAt;
    private String securityAlertSummary;
    private int trustScore;
    private String trustRiskLevel;
    private String trustDecision;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(String yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getTargetWeight() { return targetWeight; }
    public void setTargetWeight(String targetWeight) { this.targetWeight = targetWeight; }
    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }
    public String getHealthConditions() { return healthConditions; }
    public void setHealthConditions(String healthConditions) { this.healthConditions = healthConditions; }
    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }
    public String getFitnessGoals() { return fitnessGoals; }
    public void setFitnessGoals(String fitnessGoals) { this.fitnessGoals = fitnessGoals; }
    public boolean isFaceIdEnabled() { return faceIdEnabled; }
    public void setFaceIdEnabled(boolean faceIdEnabled) { this.faceIdEnabled = faceIdEnabled; }
    public String getFaceImagePath() { return faceImagePath; }
    public void setFaceImagePath(String faceImagePath) { this.faceImagePath = faceImagePath; }
    public int getPasswordScore() { return passwordScore; }
    public void setPasswordScore(int passwordScore) { this.passwordScore = passwordScore; }
    public String getPasswordStrength() { return passwordStrength; }
    public void setPasswordStrength(String passwordStrength) { this.passwordStrength = passwordStrength; }
    public boolean isCompromisedPassword() { return compromisedPassword; }
    public void setCompromisedPassword(boolean compromisedPassword) { this.compromisedPassword = compromisedPassword; }
    public int getCompromisedOccurrences() { return compromisedOccurrences; }
    public void setCompromisedOccurrences(int compromisedOccurrences) { this.compromisedOccurrences = compromisedOccurrences; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getPasswordLastChangedAt() { return passwordLastChangedAt; }
    public void setPasswordLastChangedAt(String passwordLastChangedAt) { this.passwordLastChangedAt = passwordLastChangedAt; }
    public String getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(String lockedUntil) { this.lockedUntil = lockedUntil; }
    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public String getLastFailedLoginAt() { return lastFailedLoginAt; }
    public void setLastFailedLoginAt(String lastFailedLoginAt) { this.lastFailedLoginAt = lastFailedLoginAt; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public String getArchivedAt() { return archivedAt; }
    public void setArchivedAt(String archivedAt) { this.archivedAt = archivedAt; }
    public String getSecurityAlertSummary() { return securityAlertSummary; }
    public void setSecurityAlertSummary(String securityAlertSummary) { this.securityAlertSummary = securityAlertSummary; }
    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }
    public String getTrustRiskLevel() { return trustRiskLevel; }
    public void setTrustRiskLevel(String trustRiskLevel) { this.trustRiskLevel = trustRiskLevel; }
    public String getTrustDecision() { return trustDecision; }
    public void setTrustDecision(String trustDecision) { this.trustDecision = trustDecision; }
}
