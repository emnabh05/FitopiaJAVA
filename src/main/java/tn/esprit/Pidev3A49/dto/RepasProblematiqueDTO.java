package tn.esprit.Pidev3A49.dto;

public class RepasProblematiqueDTO {
    private int id;
    private String nom;
    private String typeRepas;
    private int calories;
    private int proteines;
    private int glucides;
    private int lipides;
    private String diagnostic;
    private int scoreRisque;

    public RepasProblematiqueDTO() {}

    public RepasProblematiqueDTO(int id, String nom, String typeRepas, int calories, int proteines, int glucides, int lipides, String diagnostic, int scoreRisque) {
        this.id = id;
        this.nom = nom;
        this.typeRepas = typeRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.diagnostic = diagnostic;
        this.scoreRisque = scoreRisque;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTypeRepas() { return typeRepas; }
    public void setTypeRepas(String typeRepas) { this.typeRepas = typeRepas; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public int getProteines() { return proteines; }
    public void setProteines(int proteines) { this.proteines = proteines; }
    public int getGlucides() { return glucides; }
    public void setGlucides(int glucides) { this.glucides = glucides; }
    public int getLipides() { return lipides; }
    public void setLipides(int lipides) { this.lipides = lipides; }
    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }
    public int getScoreRisque() { return scoreRisque; }
    public void setScoreRisque(int scoreRisque) { this.scoreRisque = scoreRisque; }
}
