package tn.esprit.Pidev3A49.Models;

import java.time.LocalDateTime;

public class SupplementReview {

    private int id;
    private int supplementId;
    private int rating;
    private String comment;
    private String reviewerName;
    private String reviewerEmail;
    private LocalDateTime createdAt;

    public SupplementReview() {
    }

    public SupplementReview(int supplementId, int rating, String comment) {
        this.supplementId = supplementId;
        this.rating = rating;
        this.comment = comment;
    }

    public SupplementReview(int id, int supplementId, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.supplementId = supplementId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public SupplementReview(int id, int supplementId, int rating, String comment, String reviewerName, String reviewerEmail,
                            LocalDateTime createdAt) {
        this.id = id;
        this.supplementId = supplementId;
        this.rating = rating;
        this.comment = comment;
        this.reviewerName = reviewerName;
        this.reviewerEmail = reviewerEmail;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(int supplementId) {
        this.supplementId = supplementId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
