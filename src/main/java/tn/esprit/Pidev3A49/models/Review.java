package tn.esprit.Pidev3A49.models;

import java.sql.Timestamp;

public class Review {

    private int id;
    private int idEvent;
    private String emailParticipant;
    private int note;
    private String commentaire;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Review() {
    }

    public Review(int idEvent, String emailParticipant, int note, String commentaire, Timestamp createdAt, Timestamp updatedAt) {
        this.idEvent = idEvent;
        this.emailParticipant = emailParticipant;
        this.note = note;
        this.commentaire = commentaire;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Review(int id, int idEvent, String emailParticipant, int note, String commentaire, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.idEvent = idEvent;
        this.emailParticipant = emailParticipant;
        this.note = note;
        this.commentaire = commentaire;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getEmailParticipant() {
        return emailParticipant;
    }

    public void setEmailParticipant(String emailParticipant) {
        this.emailParticipant = emailParticipant;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
