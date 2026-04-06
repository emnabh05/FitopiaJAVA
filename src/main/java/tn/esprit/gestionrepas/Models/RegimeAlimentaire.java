package tn.esprit.gestionrepas.Models;

public class RegimeAlimentaire {
    private int id;
    private int userId;
    private Double taille;
    private Double poids;
    private Integer age;
    private Double bmi;
    private String typeSante;
    private Integer caloriesCibles;
    private String repasAdequats;

    public RegimeAlimentaire() {
    }

    public RegimeAlimentaire(int id, int userId, Double taille, Double poids, Integer age, Double bmi,
                             String typeSante, Integer caloriesCibles, String repasAdequats) {
        this.id = id;
        this.userId = userId;
        this.taille = taille;
        this.poids = poids;
        this.age = age;
        this.bmi = bmi;
        this.typeSante = typeSante;
        this.caloriesCibles = caloriesCibles;
        this.repasAdequats = repasAdequats;
    }

    public RegimeAlimentaire(int userId, Double taille, Double poids, Integer age,
                             Integer caloriesCibles, String repasAdequats) {
        this.userId = userId;
        this.taille = taille;
        this.poids = poids;
        this.age = age;
        this.caloriesCibles = caloriesCibles;
        this.repasAdequats = repasAdequats;
    }

    public void calculerBmiEtTypeSante() {
        if (taille == null || poids == null || taille <= 0) {
            bmi = null;
            typeSante = null;
            return;
        }

        double tailleEnMetres = taille / 100.0;
        double bmiCalcule = poids / (tailleEnMetres * tailleEnMetres);
        bmi = Math.round(bmiCalcule * 100.0) / 100.0;
        typeSante = determinerTypeSante(bmi);
    }

    private String determinerTypeSante(double bmiValue) {
        if (bmiValue < 18.5) {
            return "sous_poids";
        }
        if (bmiValue < 25) {
            return "normal";
        }
        if (bmiValue < 30) {
            return "surpoids";
        }
        return "obesite";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Double getTaille() {
        return taille;
    }

    public void setTaille(Double taille) {
        this.taille = taille;
    }

    public Double getPoids() {
        return poids;
    }

    public void setPoids(Double poids) {
        this.poids = poids;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getTypeSante() {
        return typeSante;
    }

    public void setTypeSante(String typeSante) {
        this.typeSante = typeSante;
    }

    public Integer getCaloriesCibles() {
        return caloriesCibles;
    }

    public void setCaloriesCibles(Integer caloriesCibles) {
        this.caloriesCibles = caloriesCibles;
    }

    public String getRepasAdequats() {
        return repasAdequats;
    }

    public void setRepasAdequats(String repasAdequats) {
        this.repasAdequats = repasAdequats;
    }

    @Override
    public String toString() {
        return "RegimeAlimentaire{" +
                "id=" + id +
                ", userId=" + userId +
                ", taille=" + taille +
                ", poids=" + poids +
                ", age=" + age +
                ", bmi=" + bmi +
                ", typeSante='" + typeSante + '\'' +
                ", caloriesCibles=" + caloriesCibles +
                ", repasAdequats='" + repasAdequats + '\'' +
                '}';
    }
}
