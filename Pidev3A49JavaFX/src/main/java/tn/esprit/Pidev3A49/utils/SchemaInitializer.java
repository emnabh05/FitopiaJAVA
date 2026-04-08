package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String REGIME_TABLE = "crud_regime_alimentaire";
    public static final String SUPPLEMENT_TABLE = "crud_supplement";
    public static final String ORDER_ITEM_TABLE = "crud_order_item";

    private static final String CREATE_REGIME_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                description VARCHAR(255),
                objectif_calorique INT NOT NULL,
                actif BOOLEAN NOT NULL DEFAULT TRUE
            )
            """.formatted(REGIME_TABLE);

    private static final String CREATE_SUPPLEMENT_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(150) NOT NULL,
                category VARCHAR(100) NOT NULL,
                brand VARCHAR(100) NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                stock INT NOT NULL,
                calories INT NULL,
                description TEXT NOT NULL,
                image VARCHAR(255) NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """.formatted(SUPPLEMENT_TABLE);

    private static final String CREATE_ORDER_ITEM_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                supplement_id INT NOT NULL,
                quantity INT NOT NULL DEFAULT 1,
                CONSTRAINT fk_crud_order_item_supplement
                    FOREIGN KEY (supplement_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE RESTRICT
            )
            """.formatted(ORDER_ITEM_TABLE, SUPPLEMENT_TABLE);

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_TABLE);
            statement.executeUpdate(CREATE_ORDER_ITEM_TABLE);
        }
    }
}
