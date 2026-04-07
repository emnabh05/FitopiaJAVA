package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    private static final String CREATE_REGIME_TABLE = """
            CREATE TABLE IF NOT EXISTS regime_alimentaire (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                description VARCHAR(255),
                objectif_calorique INT NOT NULL,
                actif BOOLEAN NOT NULL DEFAULT TRUE
            )
            """;

    private static final String CREATE_REPAS_TABLE = """
            CREATE TABLE IF NOT EXISTS repas (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                description VARCHAR(255),
                calories INT NOT NULL,
                type_repas VARCHAR(50) NOT NULL,
                date_repas DATE,
                regime_id INT,
                CONSTRAINT fk_repas_regime
                    FOREIGN KEY (regime_id) REFERENCES regime_alimentaire(id)
                    ON UPDATE CASCADE
                    ON DELETE SET NULL
            )
            """;

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_REPAS_TABLE);
        }
    }
}
