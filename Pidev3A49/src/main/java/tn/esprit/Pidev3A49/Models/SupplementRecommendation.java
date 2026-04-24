package tn.esprit.Pidev3A49.Models;

public class SupplementRecommendation {

    private int supplementId;
    private double score;
    private String reason;
    private String source;

    public SupplementRecommendation() {
    }

    public SupplementRecommendation(int supplementId, double score, String reason, String source) {
        this.supplementId = supplementId;
        this.score = score;
        this.reason = reason;
        this.source = source;
    }

    public int getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(int supplementId) {
        this.supplementId = supplementId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
