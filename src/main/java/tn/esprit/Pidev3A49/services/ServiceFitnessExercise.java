package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitnessExercise;

import java.util.ArrayList;
import java.util.List;

public class ServiceFitnessExercise {

    private final List<FitnessExercise> exercises = new ArrayList<>();

    public List<FitnessExercise> getAll() {
        return new ArrayList<>(exercises);
    }

    public void add(FitnessExercise exercise) {
        exercises.add(exercise);
    }

    public void update(FitnessExercise exercise) {
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getId() == exercise.getId()) {
                exercises.set(i, exercise);
                return;
            }
        }
    }

    public void delete(FitnessExercise exercise) {
        exercises.removeIf(existing -> existing.getId() == exercise.getId());
    }
}
