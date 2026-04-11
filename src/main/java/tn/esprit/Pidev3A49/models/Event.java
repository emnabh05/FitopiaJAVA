package tn.esprit.Pidev3A49.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Event {

    private int idEvent;
    private String titre;
    private String description;
    private LocalDate dateEvent;
    private String lieu;
    private int capacite;
    private String typeEvent;
    private String imageEvent;
    private double prixEvent;
    private LocalDateTime createdAt;
    private boolean premium;

    public Event() {
    }

    public Event(String titre, String description, LocalDate dateEvent, String lieu, int capacite,
                 String typeEvent, String imageEvent, double prixEvent, boolean premium) {
        this.titre = titre;
        this.description = description;
        this.dateEvent = dateEvent;
        this.lieu = lieu;
        this.capacite = capacite;
        this.typeEvent = typeEvent;
        this.imageEvent = imageEvent;
        this.prixEvent = prixEvent;
        this.createdAt = LocalDateTime.now();
        this.premium = premium;
    }

    public Event(String titre, String description, LocalDate dateEvent, String lieu, int capacite,
                 String typeEvent, String imageEvent, double prixEvent, LocalDateTime createdAt, boolean premium) {
        this.titre = titre;
        this.description = description;
        this.dateEvent = dateEvent;
        this.lieu = lieu;
        this.capacite = capacite;
        this.typeEvent = typeEvent;
        this.imageEvent = imageEvent;
        this.prixEvent = prixEvent;
        this.createdAt = createdAt;
        this.premium = premium;
    }

    public Event(int idEvent, String titre, String description, LocalDate dateEvent, String lieu, int capacite,
                 String typeEvent, String imageEvent, double prixEvent, LocalDateTime createdAt, boolean premium) {
        this.idEvent = idEvent;
        this.titre = titre;
        this.description = description;
        this.dateEvent = dateEvent;
        this.lieu = lieu;
        this.capacite = capacite;
        this.typeEvent = typeEvent;
        this.imageEvent = imageEvent;
        this.prixEvent = prixEvent;
        this.createdAt = createdAt;
        this.premium = premium;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(int idEvent) {
        this.idEvent = idEvent;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDate dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        this.typeEvent = typeEvent;
    }

    public String getImageEvent() {
        return imageEvent;
    }

    public void setImageEvent(String imageEvent) {
        this.imageEvent = imageEvent;
    }

    public double getPrixEvent() {
        return prixEvent;
    }

    public void setPrixEvent(double prixEvent) {
        this.prixEvent = prixEvent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    @Override
    public String toString() {
        return "Event{" +
                "idEvent=" + idEvent +
                ", titre='" + titre + '\'' +
                ", dateEvent=" + dateEvent +
                ", lieu='" + lieu + '\'' +
                ", capacite=" + capacite +
                ", typeEvent='" + typeEvent + '\'' +
                ", prixEvent=" + prixEvent +
                ", createdAt=" + createdAt +
                ", premium=" + premium +
                '}';
    }
}