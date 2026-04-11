package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitnessExercise;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceFitnessExercise implements IServices<FitnessExercise> {

    private static final String TABLE_NAME = SchemaInitializer.EXERCISE_TABLE;

    private final Connection cnx;

    public ServiceFitnessExercise() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(FitnessExercise exercise) {
        validate(exercise);
        LocalDateTime now = LocalDateTime.now();
        exercise.setCreatedAt(now);
        exercise.setUpdatedAt(now);
        String qry = """
                INSERT INTO %s (name, description, muscle_group, difficulty, sets_count, repetitions, duration, video_url, image_url, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            bindStatement(exercise, pstm, false);
            pstm.executeUpdate();

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exercise.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter l'exercice.", exception);
        }
    }

    @Override
    public List<FitnessExercise> getAll() {
        List<FitnessExercise> exercises = new ArrayList<>();
        String qry = "SELECT * FROM " + TABLE_NAME + " ORDER BY updated_at DESC, id DESC";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                exercises.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les exercices.", exception);
        }

        return exercises;
    }

    public FitnessExercise getById(int id) {
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer l'exercice avec l'id " + id, exception);
        }

        return null;
    }

    @Override
    public void update(FitnessExercise exercise) {
        validate(exercise);
        exercise.setUpdatedAt(LocalDateTime.now());
        String qry = """
                UPDATE %s
                SET name = ?, description = ?, muscle_group = ?, difficulty = ?, sets_count = ?,
                    repetitions = ?, duration = ?, video_url = ?, image_url = ?, updated_at = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            bindStatement(exercise, pstm, true);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier l'exercice.", exception);
        }
    }

    @Override
    public void delete(FitnessExercise exercise) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, exercise.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer l'exercice.", exception);
        }
    }

    private void bindStatement(FitnessExercise exercise, PreparedStatement pstm, boolean includeId) throws SQLException {
        pstm.setString(1, exercise.getName());
        pstm.setString(2, exercise.getDescription());
        pstm.setString(3, exercise.getMuscleGroup());
        pstm.setString(4, exercise.getDifficulty());
        pstm.setInt(5, exercise.getSets());
        pstm.setInt(6, exercise.getRepetitions());
        pstm.setInt(7, exercise.getDuration());
        pstm.setString(8, normalizeUrl(exercise.getVideoUrl()));
        pstm.setString(9, normalizeUrl(exercise.getImageUrl()));
        if (includeId) {
            pstm.setTimestamp(10, Timestamp.valueOf(exercise.getUpdatedAt()));
            pstm.setInt(11, exercise.getId());
            return;
        }
        pstm.setTimestamp(10, Timestamp.valueOf(exercise.getCreatedAt()));
        pstm.setTimestamp(11, Timestamp.valueOf(exercise.getUpdatedAt()));
    }

    private FitnessExercise mapResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new FitnessExercise(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("muscle_group"),
                rs.getString("difficulty"),
                rs.getInt("sets_count"),
                rs.getInt("repetitions"),
                rs.getInt("duration"),
                rs.getString("video_url"),
                rs.getString("image_url"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );
    }

    private void validate(FitnessExercise exercise) {
        if (exercise == null) {
            throw new IllegalArgumentException("L'exercice est obligatoire.");
        }

        if (exercise.getName() == null || exercise.getName().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'exercice est obligatoire.");
        }

        if (exercise.getName().length() > 255) {
            throw new IllegalArgumentException("Le nom de l'exercice ne doit pas depasser 255 caracteres.");
        }

        if (exercise.getMuscleGroup() == null || exercise.getMuscleGroup().isBlank()) {
            throw new IllegalArgumentException("Le groupe musculaire est obligatoire.");
        }

        if (exercise.getMuscleGroup().length() > 100) {
            throw new IllegalArgumentException("Le groupe musculaire ne doit pas depasser 100 caracteres.");
        }

        if (exercise.getDifficulty() == null || exercise.getDifficulty().isBlank()) {
            throw new IllegalArgumentException("Le niveau de difficulte est obligatoire.");
        }

        if (exercise.getDifficulty().length() > 50) {
            throw new IllegalArgumentException("Le niveau de difficulte ne doit pas depasser 50 caracteres.");
        }

        if (exercise.getSets() <= 0 || exercise.getRepetitions() <= 0 || exercise.getDuration() <= 0) {
            throw new IllegalArgumentException("Les series, repetitions et duree doivent etre superieures a 0.");
        }

        if (exercise.getVideoUrl() != null && exercise.getVideoUrl().length() > 500) {
            throw new IllegalArgumentException("L'URL video ne doit pas depasser 500 caracteres.");
        }

        if (exercise.getImageUrl() != null && exercise.getImageUrl().length() > 500) {
            throw new IllegalArgumentException("L'URL image ne doit pas depasser 500 caracteres.");
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }
}
