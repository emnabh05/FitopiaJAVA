package tn.esprit.Pidev3A49.Models;

public class FitnessExercise {
    private static int nextId = 1;

    private int id;
    private final String name;
    private final String description;
    private final String muscleGroup;
    private final String difficulty;
    private final int sets;
    private final int repetitions;
    private final int duration;
    private final String videoUrl;
    private final String imageUrl;

    public FitnessExercise(String name, String description, String muscleGroup, String difficulty, int sets, int repetitions, int duration, String videoUrl, String imageUrl) {
        this.id = nextId++;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.difficulty = difficulty;
        this.sets = sets;
        this.repetitions = repetitions;
        this.duration = duration;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getSets() {
        return sets;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public int getDuration() {
        return duration;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
