package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ServiceSupplementFavorite {

    private final Connection cnx;

    public ServiceSupplementFavorite() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }
    }

    public Set<Integer> getFavoriteSupplementIdsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Set.of();
        }

        String query = """
                SELECT supplement_id
                FROM %s
                WHERE LOWER(user_email) = LOWER(?)
                """.formatted(SchemaInitializer.SUPPLEMENT_FAVORITE_TABLE);

        Set<Integer> favoriteIds = new HashSet<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    favoriteIds.add(resultSet.getInt("supplement_id"));
                }
            }
            return favoriteIds;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les supplements favoris.", exception);
        }
    }

    public void addFavorite(String email, int supplementId) {
        String safeEmail = normalizeEmail(email);
        if (supplementId <= 0) {
            throw new IllegalArgumentException("Supplement invalide.");
        }

        String query = """
                INSERT INTO %s (user_email, supplement_id)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE created_at = created_at
                """.formatted(SchemaInitializer.SUPPLEMENT_FAVORITE_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, safeEmail);
            statement.setInt(2, supplementId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter ce supplement aux favoris.", exception);
        }
    }

    public void removeFavorite(String email, int supplementId) {
        String safeEmail = normalizeEmail(email);
        if (supplementId <= 0) {
            throw new IllegalArgumentException("Supplement invalide.");
        }

        String query = """
                DELETE FROM %s
                WHERE LOWER(user_email) = LOWER(?) AND supplement_id = ?
                """.formatted(SchemaInitializer.SUPPLEMENT_FAVORITE_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, safeEmail);
            statement.setInt(2, supplementId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de retirer ce supplement des favoris.", exception);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email utilisateur invalide pour les favoris.");
        }
        return email.trim();
    }
}
