package tn.esprit.Pidev3A49.Models;

public class RegimeInsight {
    private int regimeId;
    private String label;
    private int targetCalories;
    private int totalMeals;
    private double avgCaloriesPerMeal;
    private double totalProt;
    private double totalGluc;
    private double totalLip;
    private String favoriteMealType;
    private String macroDominant; // Computed in Java
    private double adherenceScore; // Computed in Java

    public RegimeInsight(int regimeId, String label, int targetCalories, int totalMeals, 
                         double avgCaloriesPerMeal, double totalProt, double totalGluc, 
                         double totalLip, String favoriteMealType) {
        this.regimeId = regimeId;
        this.label = label;
        this.targetCalories = targetCalories;
        this.totalMeals = totalMeals;
        this.avgCaloriesPerMeal = avgCaloriesPerMeal;
        this.totalProt = totalProt;
        this.totalGluc = totalGluc;
        this.totalLip = totalLip;
        this.favoriteMealType = favoriteMealType;
    }

    // Getters and Setters
    public int getRegimeId() { return regimeId; }
    public String getLabel() { return label; }
    public int getTargetCalories() { return targetCalories; }
    public int getTotalMeals() { return totalMeals; }
    public void setTotalMeals(int totalMeals) { this.totalMeals = totalMeals; }
    public double getAvgCaloriesPerMeal() { return avgCaloriesPerMeal; }
    public void setAvgCaloriesPerMeal(double avgCaloriesPerMeal) { this.avgCaloriesPerMeal = avgCaloriesPerMeal; }
    public double getTotalProt() { return totalProt; }
    public void setTotalProt(double totalProt) { this.totalProt = totalProt; }
    public double getTotalGluc() { return totalGluc; }
    public void setTotalGluc(double totalGluc) { this.totalGluc = totalGluc; }
    public double getTotalLip() { return totalLip; }
    public void setTotalLip(double totalLip) { this.totalLip = totalLip; }
    public String getFavoriteMealType() { return favoriteMealType; }
    public String getMacroDominant() { return macroDominant; }
    public void setMacroDominant(String macroDominant) { this.macroDominant = macroDominant; }
    public double getAdherenceScore() { return adherenceScore; }
    public void setAdherenceScore(double adherenceScore) { this.adherenceScore = adherenceScore; }

    @Override
    public String toString() {
        return String.format("Insight[%s]: Avg Cal=%.1f, Top Meal=%s, Main Macro=%s", 
                label, avgCaloriesPerMeal, favoriteMealType, macroDominant);
    }
}
