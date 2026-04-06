package tn.esprit.Pidev3A49.models;

import java.time.LocalDateTime;

public class Participation {
    private int idParticipation;
    private int idEvent;
    private String nomParticipant;
    private String emailParticipant;
    private LocalDateTime dateInscription;

    public Participation() {
    }

    public Participation(int idParticipation, int idEvent, String nomParticipant, String emailParticipant,
                         LocalDateTime dateInscription) {
        this.idParticipation = idParticipation;
        this.idEvent = idEvent;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.dateInscription = dateInscription;
    }

    public Participation(int idEvent, String nomParticipant, String emailParticipant, LocalDateTime dateInscription) {
        this(0, idEvent, nomParticipant, emailParticipant, dateInscription);
    }

    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
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

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }
}
