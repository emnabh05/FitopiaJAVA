package tn.esprit.Pidev3A49.models;

public class RecommendationResult {

    private final Event event;
    private final int score;
    private final String explanation;

    public RecommendationResult(Event event, int score, String explanation) {
        this.event = event;
        this.score = score;
        this.explanation = explanation;
    }

    public Event getEvent() {
        return event;
    }

    public int getScore() {
        return score;
    }

    public String getExplanation() {
        return explanation;
    }
}
