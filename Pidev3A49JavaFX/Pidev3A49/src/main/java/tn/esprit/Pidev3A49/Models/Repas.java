package tn.esprit.Pidev3A49.Models;

import java.time.LocalDate;

public class Repas {

    private int id;
    private String nom;
    private String description;
    private int calories;
    private TypeRepas typeRepas;
    private LocalDate dateRepas;
    private Integer regimeId;

    public Repas() {
    }

    public Repas(int id, String nom, String description, int calories, TypeRepas typeRepas,
                 LocalDate dateRepas, Integer regimeId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.calories = calories;
        this.typeRepas = typeRepas;
        this.dateRepas = dateRepas;
        this.regimeId = regimeId;
    }

    public Repas(String nom, String description, int calories, TypeRepas typeRepas,
                 LocalDate dateRepas, Integer regimeId) {
        this.nom = nom;
        this.description = description;
        this.calories = calories;
        this.typeRepas = typeRepas;
        this.dateRepas = dateRepas;
        this.regimeId = regimeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public TypeRepas getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(TypeRepas typeRepas) {
        this.typeRepas = typeRepas;
    }

    public LocalDate getDateRepas() {
        return dateRepas;
    }

    public void setDateRepas(LocalDate dateRepas) {
        this.dateRepas = dateRepas;
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
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", calories=" + calories +
                ", typeRepas=" + typeRepas +
                ", dateRepas=" + dateRepas +
                ", regimeId=" + regimeId +
                '}';
    }
}
