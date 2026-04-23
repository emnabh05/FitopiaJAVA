package tn.esprit.Pidev3A49.Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Repas {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private Integer userId;
    private String userEmail;
    private LocalDateTime dateRepas;
    private String typeRepas;
    private String nomRepas;
    private Integer calories;
    private Integer proteines;
    private Integer glucides;
    private Integer lipides;
    private String commentaire;
    private Integer regimeId;
    private String regimeDisplay;

    public Repas() {
    }

    public Repas(int id, Integer userId, String userEmail, LocalDateTime dateRepas, String typeRepas, String nomRepas,
                 Integer calories, Integer proteines, Integer glucides, Integer lipides, String commentaire,
                 Integer regimeId, String regimeDisplay) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.dateRepas = dateRepas;
        this.typeRepas = typeRepas;
        this.nomRepas = nomRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.commentaire = commentaire;
        this.regimeId = regimeId;
        this.regimeDisplay = regimeDisplay;
    }

    public Repas(Integer userId, LocalDateTime dateRepas, String typeRepas, String nomRepas, Integer calories,
                 Integer proteines, Integer glucides, Integer lipides, String commentaire, Integer regimeId) {
        this.userId = userId;
        this.dateRepas = dateRepas;
        this.typeRepas = typeRepas;
        this.nomRepas = nomRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.commentaire = commentaire;
        this.regimeId = regimeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getDateRepas() {
        return dateRepas;
    }

    public void setDateRepas(LocalDateTime dateRepas) {
        this.dateRepas = dateRepas;
    }

    public String getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(String typeRepas) {
        this.typeRepas = typeRepas;
    }

    public String getNomRepas() {
        return nomRepas;
    }

    public void setNomRepas(String nomRepas) {
        this.nomRepas = nomRepas;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getProteines() {
        return proteines;
    }

    public void setProteines(Integer proteines) {
        this.proteines = proteines;
    }

    public Integer getGlucides() {
        return glucides;
    }

    public void setGlucides(Integer glucides) {
        this.glucides = glucides;
    }

    public Integer getLipides() {
        return lipides;
    }

    public void setLipides(Integer lipides) {
        this.lipides = lipides;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getRegimeId() {
        return regimeId;
    }

    public void setRegimeId(Integer regimeId) {
        this.regimeId = regimeId;
    }

    public String getRegimeDisplay() {
        return regimeDisplay;
    }

    public void setRegimeDisplay(String regimeDisplay) {
        this.regimeDisplay = regimeDisplay;
    }

    public String getDateDisplay() {
        return dateRepas == null ? "" : dateRepas.format(DISPLAY_FORMATTER);
    }
}
