package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.SupplementReview;
import tn.esprit.Pidev3A49.utils.AppSession;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ServiceSupplementReview {

    private final Connection cnx;
    private final String reviewTable;
    private final String supplementTable;
    private final boolean reviewHasCreatedAt;
    private final boolean reviewHasName;
    private final boolean reviewHasEmail;

    public ServiceSupplementReview() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }

        supplementTable = resolveSupplementTable();
        reviewTable = resolveReviewTable();
        ensureIdentityColumns();
        reviewHasCreatedAt = safeHasColumn(reviewTable, "created_at");
        reviewHasName = safeHasColumn(reviewTable, "name");
        reviewHasEmail = safeHasColumn(reviewTable, "email");
    }

    public void addReview(int supplementId, int rating, String comment) {
        validateReview(supplementId, rating, comment);
        ensureSupplementExists(supplementId);

        StringBuilder columns = new StringBuilder("`supplement_id`, `rating`, `comment`");
        StringBuilder values = new StringBuilder("?, ?, ?");
        if (reviewHasName) {
            columns.append(", `name`");
            values.append(", ?");
        }
        if (reviewHasEmail) {
            columns.append(", `email`");
            values.append(", ?");
        }
        if (reviewHasCreatedAt) {
            columns.append(", `created_at`");
            values.append(", ?");
        }

        String query = "INSERT INTO `%s` (%s) VALUES (%s)".formatted(reviewTable, columns, values);

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            int index = 1;
            preparedStatement.setInt(index++, supplementId);
            preparedStatement.setInt(index++, rating);
            preparedStatement.setString(index++, comment.trim());
            if (reviewHasName) {
                preparedStatement.setString(index++, resolveReviewerName());
            }
            if (reviewHasEmail) {
                preparedStatement.setString(index++, resolveReviewerEmail());
            }
            if (reviewHasCreatedAt) {
                preparedStatement.setTimestamp(index, Timestamp.valueOf(LocalDateTime.now()));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(buildSqlErrorMessage("Impossible d'ajouter l'avis sur le supplement.", exception), exception);
        }
    }

    public List<SupplementReview> getBySupplementId(int supplementId) {
        if (supplementId <= 0) {
            return List.of();
        }

        String createdAtSelection = reviewHasCreatedAt ? "`created_at`" : "NULL AS `created_at`";
        String nameSelection = reviewHasName ? "`name`" : "NULL AS `name`";
        String emailSelection = reviewHasEmail ? "`email`" : "NULL AS `email`";
        String orderBy = reviewHasCreatedAt ? "ORDER BY `created_at` DESC, `id` DESC" : "ORDER BY `id` DESC";

        String query = """
                SELECT `id`, `supplement_id`, `rating`, `comment` AS `review_comment`, %s, %s, %s
                FROM `%s`
                WHERE `supplement_id` = ?
                %s
                """.formatted(createdAtSelection, nameSelection, emailSelection, reviewTable, orderBy);

        List<SupplementReview> reviews = new ArrayList<>();
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, supplementId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(buildSqlErrorMessage("Impossible de recuperer les avis du supplement.", exception), exception);
        }

        return reviews;
    }

    private SupplementReview mapResultSet(ResultSet resultSet) throws SQLException {
        return new SupplementReview(
                resultSet.getInt("id"),
                resultSet.getInt("supplement_id"),
                resultSet.getInt("rating"),
                resultSet.getString("review_comment"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                readTimestamp(resultSet.getTimestamp("created_at"))
        );
    }

    private LocalDateTime readTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void validateReview(int supplementId, int rating, String comment) {
        if (supplementId <= 0) {
            throw new IllegalArgumentException("Le supplement selectionne est invalide.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit etre comprise entre 1 et 5.");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Le commentaire est obligatoire.");
        }
        if (comment.trim().length() > 1000) {
            throw new IllegalArgumentException("Le commentaire ne doit pas depasser 1000 caracteres.");
        }
    }

    private void ensureSupplementExists(int supplementId) {
        String query = "SELECT 1 FROM `" + supplementTable + "` WHERE `id` = ? LIMIT 1";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, supplementId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Le supplement selectionne est introuvable.");
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(buildSqlErrorMessage("Impossible de verifier le supplement selectionne.", exception), exception);
        }
    }

    private String resolveSupplementTable() {
        for (String tableName : candidateSupplementTables()) {
            try {
                if (tableExists(tableName) && hasColumn(tableName, "id")) {
                    return tableName;
                }
            } catch (SQLException exception) {
                throw new IllegalStateException(buildSqlErrorMessage("Impossible de lire le schema des supplements.", exception), exception);
            }
        }

        throw new IllegalStateException("Aucune table supplement valide n'a ete trouvee dans fitopiabd.");
    }

    private String resolveReviewTable() {
        List<String> candidates = candidateReviewTables();

        for (String tableName : candidates) {
            try {
                if (tableExists(tableName) && hasReviewColumns(tableName)) {
                    return tableName;
                }
            } catch (SQLException exception) {
                throw new IllegalStateException(buildSqlErrorMessage("Impossible de lire le schema des avis.", exception), exception);
            }
        }

        for (String tableName : candidates) {
            try {
                if (!tableExists(tableName)) {
                    createReviewTable(tableName);
                    return tableName;
                }
            } catch (SQLException exception) {
                throw new IllegalStateException(buildSqlErrorMessage("Impossible d'initialiser la table des avis.", exception), exception);
            }
        }

        throw new IllegalStateException("Aucune table d'avis exploitable n'a ete trouvee dans fitopiabd.");
    }

    private void createReviewTable(String tableName) throws SQLException {
        String createReviewTableQuery = """
                CREATE TABLE IF NOT EXISTS `%s` (
                    `id` INT PRIMARY KEY AUTO_INCREMENT,
                    `supplement_id` INT NOT NULL,
                    `rating` TINYINT NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
                    `comment` TEXT NOT NULL,
                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (`supplement_id`) REFERENCES `%s`(`id`) ON DELETE CASCADE
                )
                """.formatted(tableName, supplementTable);

        try (PreparedStatement preparedStatement = cnx.prepareStatement(createReviewTableQuery)) {
            preparedStatement.executeUpdate();
        }
    }

    private void ensureIdentityColumns() {
        ensureColumn("name", "ALTER TABLE `%s` ADD COLUMN `name` VARCHAR(120) NULL".formatted(reviewTable));
        ensureColumn("email", "ALTER TABLE `%s` ADD COLUMN `email` VARCHAR(180) NULL".formatted(reviewTable));
    }

    private void ensureColumn(String columnName, String alterQuery) {
        try {
            if (hasColumn(reviewTable, columnName)) {
                return;
            }
            try (PreparedStatement preparedStatement = cnx.prepareStatement(alterQuery)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(buildSqlErrorMessage("Impossible d'etendre la table des avis.", exception), exception);
        }
    }

    private boolean hasReviewColumns(String tableName) throws SQLException {
        return hasColumn(tableName, "id")
                && hasColumn(tableName, "supplement_id")
                && hasColumn(tableName, "rating")
                && hasColumn(tableName, "comment");
    }

    private List<String> candidateSupplementTables() {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(SchemaInitializer.SUPPLEMENT_TABLE);
        candidates.add("crud_supplement");
        candidates.add("supplement");
        return List.copyOf(candidates);
    }

    private List<String> candidateReviewTables() {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(SchemaInitializer.SUPPLEMENT_REVIEW_TABLE);
        candidates.add("crud_supplement_review");
        candidates.add("supplement_review");
        candidates.add("review");
        candidates.add("reviews");
        candidates.add("avis");
        return List.copyOf(candidates);
    }

    private boolean tableExists(String tableName) throws SQLException {
        String query = """
                SELECT 1
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                LIMIT 1
                """;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasColumn(String tableName, String columnName) throws SQLException {
        String query = """
                SELECT 1
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                LIMIT 1
                """;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean safeHasColumn(String tableName, String columnName) {
        try {
            return hasColumn(tableName, columnName);
        } catch (SQLException exception) {
            return false;
        }
    }

    private String resolveReviewerName() {
        String displayName = AppSession.getInstance().getDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        return "Anonymous";
    }

    private String resolveReviewerEmail() {
        String email = AppSession.getInstance().getEmail();
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String buildSqlErrorMessage(String message, SQLException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.isBlank()) {
            return message;
        }
        return message + " Detail SQL (" + reviewTable + "): " + detail;
    }
}
