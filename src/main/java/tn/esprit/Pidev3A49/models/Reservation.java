package tn.esprit.Pidev3A49.models;

import java.time.LocalDateTime;

public class Reservation {

    private int id;
    private int idEvent;
    private String nomParticipant;
    private String emailParticipant;
    private String telephoneParticipant;
    private LocalDateTime dateReservation;
    private double montant;
    private String statut;
    private String transactionId;
    private String qrToken;
    private LocalDateTime checkedInAt;
    private LocalDateTime usedAt;
    private LocalDateTime qrGeneratedAt;

    public Reservation() {
    }

    public Reservation(int idEvent, String nomParticipant, String emailParticipant, LocalDateTime dateReservation,
                       double montant, String statut) {
        this(idEvent, nomParticipant, emailParticipant, null, dateReservation, montant, statut);
    }

    public Reservation(int idEvent, String nomParticipant, String emailParticipant, String telephoneParticipant,
                       LocalDateTime dateReservation, double montant, String statut) {
        this.idEvent = idEvent;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.telephoneParticipant = telephoneParticipant;
        this.dateReservation = dateReservation;
        this.montant = montant;
        this.statut = statut;
    }

    public Reservation(int id, int idEvent, String nomParticipant, String emailParticipant, LocalDateTime dateReservation,
                       double montant, String statut, String transactionId, String qrToken, LocalDateTime checkedInAt,
                       LocalDateTime usedAt, LocalDateTime qrGeneratedAt) {
        this(id, idEvent, nomParticipant, emailParticipant, null, dateReservation, montant, statut, transactionId,
                qrToken, checkedInAt, usedAt, qrGeneratedAt);
    }

    public Reservation(int id, int idEvent, String nomParticipant, String emailParticipant, String telephoneParticipant,
                       LocalDateTime dateReservation, double montant, String statut, String transactionId,
                       String qrToken, LocalDateTime checkedInAt, LocalDateTime usedAt, LocalDateTime qrGeneratedAt) {
        this.id = id;
        this.idEvent = idEvent;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.telephoneParticipant = telephoneParticipant;
        this.dateReservation = dateReservation;
        this.montant = montant;
        this.statut = statut;
        this.transactionId = transactionId;
        this.qrToken = qrToken;
        this.checkedInAt = checkedInAt;
        this.usedAt = usedAt;
        this.qrGeneratedAt = qrGeneratedAt;
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

    public String getTelephoneParticipant() {
        return telephoneParticipant;
    }

    public void setTelephoneParticipant(String telephoneParticipant) {
        this.telephoneParticipant = telephoneParticipant;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getQrGeneratedAt() {
        return qrGeneratedAt;
    }

    public void setQrGeneratedAt(LocalDateTime qrGeneratedAt) {
        this.qrGeneratedAt = qrGeneratedAt;
    }
}
