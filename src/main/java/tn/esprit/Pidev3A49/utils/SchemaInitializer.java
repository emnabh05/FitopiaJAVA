package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String USER_TABLE = "users";
    public static final String REGIME_TABLE = "regime_alimentaire";
    public static final String REPAS_TABLE = "repas";
    public static final String FORUM_TABLE = "forum";
    public static final String COMMENT_TABLE = "forum_comment";

    private static final String CREATE_FORUM_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL
            )
            """.formatted(FORUM_TABLE);

    private static final String CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                email VARCHAR(191) NOT NULL,
                first_name VARCHAR(100) NULL,
                last_name VARCHAR(100) NULL,
                UNIQUE KEY uk_users_email (email)
            )
            """.formatted(USER_TABLE);

    private static final String CREATE_REGIME_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                taille DOUBLE NULL,
                poids DOUBLE NULL,
                age INT NULL,
                bmi DOUBLE NULL,
                type_sante VARCHAR(100) NULL,
                calories_cibles INT NULL,
                repas_adequats TEXT NULL,
                CONSTRAINT fk_regime_user
                    FOREIGN KEY (user_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE RESTRICT
            )
            """.formatted(REGIME_TABLE, USER_TABLE);

    private static final String CREATE_REPAS_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id_repas INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                date_repas DATETIME NOT NULL,
                type_repas VARCHAR(100) NOT NULL,
                nom_repas VARCHAR(255) NOT NULL,
                calories INT NULL,
                proteines INT NULL,
                glucides INT NULL,
                lipides INT NULL,
                commentaire TEXT NULL,
                regime_id INT NULL,
                CONSTRAINT fk_repas_user
                    FOREIGN KEY (user_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE RESTRICT,
                CONSTRAINT fk_repas_regime
                    FOREIGN KEY (regime_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE SET NULL
            )
            """.formatted(REPAS_TABLE, USER_TABLE, REGIME_TABLE);

    private static final String CREATE_COMMENT_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                content TEXT NOT NULL,
                forum_id INT NOT NULL,
                CONSTRAINT fk_forum_comment_forum
                    FOREIGN KEY (forum_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
            )
            """.formatted(COMMENT_TABLE, FORUM_TABLE);

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            statement.executeUpdate(CREATE_USER_TABLE);
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_REPAS_TABLE);
            statement.executeUpdate(CREATE_FORUM_TABLE);
            statement.executeUpdate(CREATE_COMMENT_TABLE);
        }
        seedDefaultUserIfNeeded(connection);
    }

    private static void seedDefaultUserIfNeeded(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM " + USER_TABLE;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(countQuery)) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return;
            }
        }

        String insertQuery = """
                INSERT INTO %s (email, first_name, last_name)
                VALUES (?, ?, ?)
                """.formatted(USER_TABLE);
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, "demo@fitopia.local");
            preparedStatement.setString(2, "Demo");
            preparedStatement.setString(3, "User");
            preparedStatement.executeUpdate();
        }
    }
}
