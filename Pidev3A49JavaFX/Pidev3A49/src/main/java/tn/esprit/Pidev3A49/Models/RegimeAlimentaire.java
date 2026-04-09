package tn.esprit.Pidev3A49.Models;

public class RegimeAlimentaire {

    private int id;
    private String nom;
    private String description;
    private int objectifCalorique;
    private boolean actif;

    public RegimeAlimentaire() {
    }

    public RegimeAlimentaire(int id, String nom, String description, int objectifCalorique, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.objectifCalorique = objectifCalorique;
        this.actif = actif;
    }

    public RegimeAlimentaire(String nom, String description, int objectifCalorique, boolean actif) {
        this.nom = nom;
        this.description = description;
        this.objectifCalorique = objectifCalorique;
        this.actif = actif;
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

    public int getObjectifCalorique() {
        return objectifCalorique;
    }

    public void setObjectifCalorique(int objectifCalorique) {
        this.objectifCalorique = objectifCalorique;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "RegimeAlimentaire{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", objectifCalorique=" + objectifCalorique +
                ", actif=" + actif +
                '}';
    }
}
