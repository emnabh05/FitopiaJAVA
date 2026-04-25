package tn.esprit.Pidev3A49.models;

import java.time.LocalDateTime;

public class WaitlistEntry {

    private int id;
    private int idEvent;
    private String nomParticipant;
    private String emailParticipant;
    private String status;
    private int position;
    private LocalDateTime createdAt;
    private String token;
    private LocalDateTime invitedAt;
    private LocalDateTime expiresAt;

    public WaitlistEntry() {
    }

    public WaitlistEntry(int idEvent, String nomParticipant, String emailParticipant, String status, int position,
                         LocalDateTime createdAt, String token, LocalDateTime invitedAt, LocalDateTime expiresAt) {
        this.idEvent = idEvent;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.status = status;
        this.position = position;
        this.createdAt = createdAt;
        this.token = token;
        this.invitedAt = invitedAt;
        this.expiresAt = expiresAt;
    }

    public WaitlistEntry(int id, int idEvent, String nomParticipant, String emailParticipant, String status, int position,
                         LocalDateTime createdAt, String token, LocalDateTime invitedAt, LocalDateTime expiresAt) {
        this(idEvent, nomParticipant, emailParticipant, status, position, createdAt, token, invitedAt, expiresAt);
        this.id = id;
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

    public String getNomParticipant() {
        return nomParticipant;
    }

    public void setNomParticipant(String nomParticipant) {
        this.nomParticipant = nomParticipant;
    }

    public String getEmailParticipant() {
        return emailParticipant;
    }

    public void setEmailParticipant(String emailParticipant) {
        this.emailParticipant = emailParticipant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
