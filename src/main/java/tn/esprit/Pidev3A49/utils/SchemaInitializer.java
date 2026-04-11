package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String REGIME_TABLE = "crud_regime_alimentaire";
    public static final String REPAS_TABLE = "crud_repas";
    public static final String FORUM_TABLE = "forum";
    public static final String COMMENT_TABLE = "comment";

    private static final String CREATE_REGIME_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                description VARCHAR(255),
                objectif_calorique INT NOT NULL,
                actif BOOLEAN NOT NULL DEFAULT TRUE
            )
            """.formatted(REGIME_TABLE);

    private static final String CREATE_REPAS_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                description VARCHAR(255),
                calories INT NOT NULL,
                type_repas VARCHAR(50) NOT NULL,
                date_repas DATE,
                regime_id INT,
                CONSTRAINT fk_crud_repas_regime
                    FOREIGN KEY (regime_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE SET NULL
            )
            """.formatted(REPAS_TABLE, REGIME_TABLE);

    private static final String CREATE_FORUM_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL
            )
            """.formatted(FORUM_TABLE);

    private static final String CREATE_COMMENT_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                content TEXT NOT NULL,
                forum_id INT NOT NULL,
                CONSTRAINT fk_comment_forum
                    FOREIGN KEY (forum_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
            )
            """.formatted(COMMENT_TABLE, FORUM_TABLE);

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_REPAS_TABLE);
            statement.executeUpdate(CREATE_FORUM_TABLE);
            statement.executeUpdate(CREATE_COMMENT_TABLE);
        }
    }
}
