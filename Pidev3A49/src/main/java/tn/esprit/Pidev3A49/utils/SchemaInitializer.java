package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String SUPPLEMENT_TABLE = "crud_supplement";
    public static final String SUPPLEMENT_ORDER_TABLE = "crud_supplement_order";
    public static final String SUPPLEMENT_ORDER_ITEM_TABLE = "crud_supplement_order_item";

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

    private static final String CREATE_SUPPLEMENT_ORDER_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(180) NOT NULL,
                phone VARCHAR(50) NOT NULL,
                address VARCHAR(255) NOT NULL,
                city VARCHAR(120) NOT NULL,
                postal_code VARCHAR(40) NOT NULL,
                notes TEXT NULL,
                payment_method VARCHAR(60) NOT NULL,
                discount_code VARCHAR(80) NULL,
                subtotal DECIMAL(10, 2) NOT NULL,
                shipping_cost DECIMAL(10, 2) NOT NULL,
                discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                total_amount DECIMAL(10, 2) NOT NULL,
                status VARCHAR(40) NOT NULL DEFAULT 'PLACED',
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """.formatted(SUPPLEMENT_ORDER_TABLE);

    private static final String CREATE_SUPPLEMENT_ORDER_ITEM_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                order_id INT NOT NULL,
                supplement_id INT NOT NULL,
                supplement_name VARCHAR(150) NOT NULL,
                unit_price DECIMAL(10, 2) NOT NULL,
                quantity INT NOT NULL,
                line_total DECIMAL(10, 2) NOT NULL,
                CONSTRAINT fk_supplement_order_item_order
                    FOREIGN KEY (order_id) REFERENCES %s(id)
                    ON DELETE CASCADE
            )
            """.formatted(SUPPLEMENT_ORDER_ITEM_TABLE, SUPPLEMENT_ORDER_TABLE);

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_SUPPLEMENT_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_ITEM_TABLE);
        }
    }
}
