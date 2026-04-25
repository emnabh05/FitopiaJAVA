package tn.esprit.Pidev3A49.dto;

/**
 * DTO (Data Transfer Object) utilisé par le moteur de recommandation basé
 * sur la similarité nutritionnelle.
 *
 * <p>Contient les données d'un repas enrichies du score de similarité calculé
 * par rapport aux objectifs nutritionnels d'un régime (ou d'un autre repas).</p>
 *
 * <p>Le score de similarité représente la distance nutritionnelle absolue :
 * plus il est faible, plus le repas est proche de la cible nutritionnelle.</p>
 */
public class RepasSimilariteDTO {

    /** Identifiant du repas en base de données. */
    private int id;

    /** Nom du repas (ex : "Salade César", "Omelette protéinée"). */
    private String nom;

    /** Type du repas (ex : "Petit-déjeuner", "Déjeuner", "Dîner"). */
    private String typeRepas;

    /** Valeur calorique réelle du repas en kcal. */
    private double calories;

    /** Teneur en protéines du repas en grammes. */
    private double proteines;

    /** Teneur en glucides du repas en grammes. */
    private double glucides;

    /** Teneur en lipides du repas en grammes. */
    private double lipides;

    /**
     * Score de similarité nutritionnelle (distance de Manhattan).
     *
     * <p>Calculé par la formule :</p>
     * <pre>
     *   score = |calories_repas - objectif_calories|
     *         + |proteines_repas - objectif_proteines|
     *         + |glucides_repas - objectif_glucides|
     *         + |lipides_repas - objectif_lipides|
     * </pre>
     *
     * <p>Un score de 0 signifie une correspondance nutritionnelle parfaite.
     * Les repas sont triés par score croissant (les plus proches en premier).</p>
     */
    private double scoreSimilarite;

    // ─── Constructeurs ────────────────────────────────────────────────────────

    /** Constructeur vide requis pour les frameworks et usages génériques. */
    public RepasSimilariteDTO() {
    }

    /**
     * Constructeur complet.
     *
     * @param id             identifiant du repas
     * @param nom            nom du repas
     * @param typeRepas      type du repas
     * @param calories       calories du repas
     * @param proteines      protéines du repas
     * @param glucides       glucides du repas
     * @param lipides        lipides du repas
     * @param scoreSimilarite score de distance nutritionnelle (plus bas = meilleur)
     */
    public RepasSimilariteDTO(int id, String nom, String typeRepas,
                              double calories, double proteines, double glucides,
                              double lipides, double scoreSimilarite) {
        this.id = id;
        this.nom = nom;
        this.typeRepas = typeRepas;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.scoreSimilarite = scoreSimilarite;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

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

    public String getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(String typeRepas) {
        this.typeRepas = typeRepas;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteines() {
        return proteines;
    }

    public void setProteines(double proteines) {
        this.proteines = proteines;
    }

    public double getGlucides() {
        return glucides;
    }

    public void setGlucides(double glucides) {
        this.glucides = glucides;
    }

    public double getLipides() {
        return lipides;
    }

    public void setLipides(double lipides) {
        this.lipides = lipides;
    }

    public double getScoreSimilarite() {
        return scoreSimilarite;
    }

    public void setScoreSimilarite(double scoreSimilarite) {
        this.scoreSimilarite = scoreSimilarite;
    }

    // ─── Méthodes utilitaires ─────────────────────────────────────────────────

    /**
     * Retourne une représentation textuelle du niveau de compatibilité
     * basée sur le score de similarité.
     *
     * @return label de compatibilité (Excellent / Bon / Acceptable / Éloigné)
     */
    public String getLabelCompatibilite() {
        if (scoreSimilarite <= 50)  return "★★★ Excellent";
        if (scoreSimilarite <= 150) return "★★☆ Bon";
        if (scoreSimilarite <= 300) return "★☆☆ Acceptable";
        return "☆☆☆ Éloigné";
    }

    @Override
    public String toString() {
        return "RepasSimilariteDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", typeRepas='" + typeRepas + '\'' +
                ", calories=" + calories +
                ", scoreSimilarite=" + scoreSimilarite +
                ", compatibilite='" + getLabelCompatibilite() + '\'' +
                '}';
    }
}
