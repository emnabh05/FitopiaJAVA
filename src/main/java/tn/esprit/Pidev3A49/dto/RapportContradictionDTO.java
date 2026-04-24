package tn.esprit.Pidev3A49.dto;

import java.util.List;

public class RapportContradictionDTO {
    private String nomRegime;
    private String objectif;
    private int nombreRepas;
    private int totalCalories;
    private double moyenneCalories;
    private double moyenneProteines;
    private double moyenneGlucides;
    private double moyenneLipides;
    private int nombreContradictions;
    private double tauxContradiction;
    private String diagnosticGlobal;
    private String conseilAutomatique;
    private List<RepasProblematiqueDTO> repasProblematiques;

    public RapportContradictionDTO() {}

    // Getters and Setters
    public String getNomRegime() { return nomRegime; }
    public void setNomRegime(String nomRegime) { this.nomRegime = nomRegime; }
    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }
    public int getNombreRepas() { return nombreRepas; }
    public void setNombreRepas(int nombreRepas) { this.nombreRepas = nombreRepas; }
    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
    public double getMoyenneCalories() { return moyenneCalories; }
    public void setMoyenneCalories(double moyenneCalories) { this.moyenneCalories = moyenneCalories; }
    public double getMoyenneProteines() { return moyenneProteines; }
    public void setMoyenneProteines(double moyenneProteines) { this.moyenneProteines = moyenneProteines; }
    public double getMoyenneGlucides() { return moyenneGlucides; }
    public void setMoyenneGlucides(double moyenneGlucides) { this.moyenneGlucides = moyenneGlucides; }
    public double getMoyenneLipides() { return moyenneLipides; }
    public void setMoyenneLipides(double moyenneLipides) { this.moyenneLipides = moyenneLipides; }
    public int getNombreContradictions() { return nombreContradictions; }
    public void setNombreContradictions(int nombreContradictions) { this.nombreContradictions = nombreContradictions; }
    public double getTauxContradiction() { return tauxContradiction; }
    public void setTauxContradiction(double tauxContradiction) { this.tauxContradiction = tauxContradiction; }
    public String getDiagnosticGlobal() { return diagnosticGlobal; }
    public void setDiagnosticGlobal(String diagnosticGlobal) { this.diagnosticGlobal = diagnosticGlobal; }
    public String getConseilAutomatique() { return conseilAutomatique; }
    public void setConseilAutomatique(String conseilAutomatique) { this.conseilAutomatique = conseilAutomatique; }
    public List<RepasProblematiqueDTO> getRepasProblematiques() { return repasProblematiques; }
    public void setRepasProblematiques(List<RepasProblematiqueDTO> repasProblematiques) { this.repasProblematiques = repasProblematiques; }
}
