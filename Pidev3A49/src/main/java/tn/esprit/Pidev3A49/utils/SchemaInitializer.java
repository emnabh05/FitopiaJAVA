package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String SUPPLEMENT_TABLE = "crud_supplement";
    public static final String SUPPLEMENT_ORDER_TABLE = "crud_supplement_order";
    public static final String SUPPLEMENT_ORDER_ITEM_TABLE = "crud_supplement_order_item";
    public static final String REGIME_TABLE = "crud_regime_alimentaire";
    public static final String REPAS_TABLE = "crud_repas";
    public static final String EXERCISE_TABLE = "fitness_exercise";

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

    private static final String CREATE_EXERCISE_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                muscle_group VARCHAR(100) NOT NULL,
                difficulty VARCHAR(50) NOT NULL,
                sets_count INT NOT NULL,
                repetitions INT NOT NULL,
                duration INT NOT NULL,
                video_url VARCHAR(500),
                image_url VARCHAR(500),
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """.formatted(EXERCISE_TABLE);

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_SUPPLEMENT_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_ITEM_TABLE);
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_REPAS_TABLE);
            statement.executeUpdate(CREATE_EXERCISE_TABLE);
            synchronizeExerciseTable(connection, statement);
        }
    }

    private static void synchronizeExerciseTable(Connection connection, Statement statement) throws SQLException {
        boolean hasLegacySets = hasColumn(connection, EXERCISE_TABLE, "sets");
        boolean hasSetsCount = hasColumn(connection, EXERCISE_TABLE, "sets_count");

        if (!hasSetsCount) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN sets_count INT NOT NULL DEFAULT 1 AFTER difficulty"
            );
        }

        if (hasLegacySets) {
            statement.executeUpdate(
                    "UPDATE " + EXERCISE_TABLE + " SET sets_count = COALESCE(sets_count, `sets`)"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "repetitions")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN repetitions INT NOT NULL DEFAULT 1 AFTER sets_count"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "duration")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN duration INT NOT NULL DEFAULT 1 AFTER repetitions"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "video_url")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN video_url VARCHAR(500) NULL AFTER duration"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "image_url")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN image_url VARCHAR(500) NULL AFTER video_url"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "created_at")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER image_url"
            );
        } else {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
            );
        }

        if (!hasColumn(connection, EXERCISE_TABLE, "updated_at")) {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at"
            );
        } else {
            statement.executeUpdate(
                    "ALTER TABLE " + EXERCISE_TABLE + " MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            );
        }

        if (hasLegacySets) {
            statement.executeUpdate("ALTER TABLE " + EXERCISE_TABLE + " DROP COLUMN `sets`");
        }
    }

    private static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet resultSet = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return resultSet.next();
        }
    }
}
