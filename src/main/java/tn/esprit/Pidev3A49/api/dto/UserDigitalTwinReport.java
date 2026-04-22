package tn.esprit.Pidev3A49.api.dto;

public class UserDigitalTwinReport {
    private int userId;
    private String fullName;
    private String email;
    private String goal;
    private String regimeName;
    private Double targetCalories;
    private int mealsCount;
    private Double avgRealCalories;
    private Double totalRealCalories;
    private String lastMealDate;
    private Double calorieGap;
    private String alignmentStatus;
    private int twinScore;
    private String alertLevel;

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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getRegimeName() {
        return regimeName;
    }

    public void setRegimeName(String regimeName) {
        this.regimeName = regimeName;
    }

    public Double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(Double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public int getMealsCount() {
        return mealsCount;
    }

    public void setMealsCount(int mealsCount) {
        this.mealsCount = mealsCount;
    }

    public Double getAvgRealCalories() {
        return avgRealCalories;
    }

    public void setAvgRealCalories(Double avgRealCalories) {
        this.avgRealCalories = avgRealCalories;
    }

    public Double getTotalRealCalories() {
        return totalRealCalories;
    }

    public void setTotalRealCalories(Double totalRealCalories) {
        this.totalRealCalories = totalRealCalories;
    }

    public String getLastMealDate() {
        return lastMealDate;
    }

    public void setLastMealDate(String lastMealDate) {
        this.lastMealDate = lastMealDate;
    }

    public Double getCalorieGap() {
        return calorieGap;
    }

    public void setCalorieGap(Double calorieGap) {
        this.calorieGap = calorieGap;
    }

    public String getAlignmentStatus() {
        return alignmentStatus;
    }

    public void setAlignmentStatus(String alignmentStatus) {
        this.alignmentStatus = alignmentStatus;
    }

    public int getTwinScore() {
        return twinScore;
    }

    public void setTwinScore(int twinScore) {
        this.twinScore = twinScore;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    @Override
    public String toString() {
        return "UserDigitalTwinReport{"
                + "userId=" + userId
                + ", fullName='" + fullName + '\''
                + ", email='" + email + '\''
                + ", goal='" + goal + '\''
                + ", regimeName='" + regimeName + '\''
                + ", targetCalories=" + targetCalories
                + ", mealsCount=" + mealsCount
                + ", avgRealCalories=" + avgRealCalories
                + ", totalRealCalories=" + totalRealCalories
                + ", lastMealDate='" + lastMealDate + '\''
                + ", calorieGap=" + calorieGap
                + ", alignmentStatus='" + alignmentStatus + '\''
                + ", twinScore=" + twinScore
                + ", alertLevel='" + alertLevel + '\''
                + '}';
    }
}
