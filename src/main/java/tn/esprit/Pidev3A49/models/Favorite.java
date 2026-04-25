package tn.esprit.Pidev3A49.models;

import java.time.LocalDateTime;

public class Favorite {

    private int id;
    private int idEvent;
    private String emailParticipant;
    private LocalDateTime createdAt;

    public Favorite() {
    }

    public Favorite(int idEvent, String emailParticipant, LocalDateTime createdAt) {
        this.idEvent = idEvent;
        this.emailParticipant = emailParticipant;
        this.createdAt = createdAt;
    }

    public Favorite(int id, int idEvent, String emailParticipant, LocalDateTime createdAt) {
        this.id = id;
        this.idEvent = idEvent;
        this.emailParticipant = emailParticipant;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
