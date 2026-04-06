package tn.esprit.gestionrepas.Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Repas {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int idRepas;
    private int userId;
    private LocalDateTime dateRepas;
    private String typeRepas;
    private String nomRepas;
    private Integer calories;
    private Integer proteines;
    private Integer glucides;
    private Integer lipides;
    private String commentaire;
    private Integer regimeId;

    public Repas() {
    }

    public Repas(int idRepas, int userId, LocalDateTime dateRepas, String typeRepas, String nomRepas,
                 Integer calories, Integer proteines, Integer glucides, Integer lipides,
                 String commentaire, Integer regimeId) {
        this.idRepas = idRepas;
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

    public Repas(int userId, LocalDateTime dateRepas, String typeRepas, String nomRepas,
                 Integer calories, Integer proteines, Integer glucides, Integer lipides,
                 String commentaire, Integer regimeId) {
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

    public int getIdRepas() {
        return idRepas;
    }

    public void setIdRepas(int idRepas) {
        this.idRepas = idRepas;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "Repas{" +
                "idRepas=" + idRepas +
                ", userId=" + userId +
                ", dateRepas=" + (dateRepas == null ? null : dateRepas.format(FORMATTER)) +
                ", typeRepas='" + typeRepas + '\'' +
                ", nomRepas='" + nomRepas + '\'' +
                ", calories=" + calories +
                ", proteines=" + proteines +
                ", glucides=" + glucides +
                ", lipides=" + lipides +
                ", commentaire='" + commentaire + '\'' +
                ", regimeId=" + regimeId +
                '}';
    }
}
