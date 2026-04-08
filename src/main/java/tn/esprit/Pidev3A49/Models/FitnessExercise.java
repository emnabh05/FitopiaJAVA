package tn.esprit.Pidev3A49.Models;

import java.time.LocalDateTime;

public class FitnessExercise {

    private int id;
    private String name;
    private String description;
    private String muscleGroup;
    private String difficulty;
    private int sets;
    private int repetitions;
    private int duration;
    private String videoUrl;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FitnessExercise() {
    }

    public FitnessExercise(int id, String name, String description, String muscleGroup, String difficulty, int sets,
                           int repetitions, int duration, String videoUrl, String imageUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.difficulty = difficulty;
        this.sets = sets;
        this.repetitions = repetitions;
        this.duration = duration;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public FitnessExercise(String name, String description, String muscleGroup, String difficulty, int sets,
                           int repetitions, int duration, String videoUrl, String imageUrl) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FitnessExercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", muscleGroup='" + muscleGroup + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", sets=" + sets +
                ", repetitions=" + repetitions +
                ", duration=" + duration +
                '}';
    }
}
