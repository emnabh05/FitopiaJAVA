package tn.esprit.Pidev3A49.Models;

public class RegimeAlimentaire {

    private int id;
    private Integer userId;
    private String userEmail;
    private Double taille;
    private Double poids;
    private Integer age;
    private Double bmi;
    private String typeSante;
    private Integer caloriesCibles;
    private String repasAdequats;

    public RegimeAlimentaire() {
    }

    public RegimeAlimentaire(int id, Integer userId, String userEmail, Double taille, Double poids, Integer age,
                             Double bmi, String typeSante, Integer caloriesCibles, String repasAdequats) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.taille = taille;
        this.poids = poids;
        this.age = age;
        this.bmi = bmi;
        this.typeSante = typeSante;
        this.caloriesCibles = caloriesCibles;
        this.repasAdequats = repasAdequats;
    }

    public RegimeAlimentaire(Integer userId, Double taille, Double poids, Integer age, Double bmi, String typeSante,
                             Integer caloriesCibles, String repasAdequats) {
        this.userId = userId;
        this.taille = taille;
        this.poids = poids;
        this.age = age;
        this.bmi = bmi;
        this.typeSante = typeSante;
        this.caloriesCibles = caloriesCibles;
        this.repasAdequats = repasAdequats;
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

    public String getDisplayLabel() {
        return "#" + id + " - " + (typeSante == null || typeSante.isBlank() ? "N/A" : typeSante) +
                " - " + (caloriesCibles == null ? 0 : caloriesCibles) + " kcal";
    }
}
