package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ReviewDAO;
import tn.esprit.Pidev3A49.models.Review;

import java.util.List;
import java.util.Map;

public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    public void addReview(Review review) {
        validate(review);
        reviewDAO.add(review);
    }

    public List<Review> getReviewsByEvent(int idEvent) {
        if (idEvent <= 0) {
            throw new IllegalArgumentException("L'evenement est invalide.");
        }
        return reviewDAO.findByEventId(idEvent);
    }

    public double getAverageNoteByEvent(int idEvent) {
        if (idEvent <= 0) {
            throw new IllegalArgumentException("L'evenement est invalide.");
        }
        return reviewDAO.getAverageNoteByEvent(idEvent);
    }

    public Map<Integer, Double> getAverageNotesByEvent() {
        return reviewDAO.getAverageNotesByEvent();
    }

    public Map<Integer, Integer> getReviewCountsByEvent() {
        return reviewDAO.getReviewCountsByEvent();
    }

    private void validate(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("L'avis est obligatoire.");
        }
        if (review.getIdEvent() <= 0) {
            throw new IllegalArgumentException("L'evenement est invalide.");
        }
        if (review.getEmailParticipant() == null || review.getEmailParticipant().trim().isEmpty() || !review.getEmailParticipant().contains("@")) {
            throw new IllegalArgumentException("Un email utilisateur valide est obligatoire.");
        }
        if (review.getNote() < 1 || review.getNote() > 5) {
            throw new IllegalArgumentException("La note doit etre comprise entre 1 et 5.");
        }
        if (review.getCommentaire() == null || review.getCommentaire().trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire est obligatoire.");
        }
    }
}
