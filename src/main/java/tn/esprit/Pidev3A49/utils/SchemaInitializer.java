package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
    public static final String FORUM_LIKE_TABLE = "forum_like";
    public static final String FORUM_REPOST_TABLE = "forum_repost";
    public static final String SUPPLEMENT_TABLE = "crud_supplement";
    public static final String SUPPLEMENT_ORDER_TABLE = "crud_supplement_order";
    public static final String SUPPLEMENT_ORDER_ITEM_TABLE = "crud_supplement_order_item";
    private static final String APP_METADATA_TABLE = "app_metadata";
    private static final String FORUM_SOCIAL_RESET_KEY = "forum_social_reset_v1";

    private static final String CREATE_FORUM_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
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
                user_id INT NOT NULL,
                content TEXT NOT NULL,
                forum_id INT NOT NULL,
                CONSTRAINT fk_forum_comment_forum
                    FOREIGN KEY (forum_id) REFERENCES %s(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
            )
            """.formatted(COMMENT_TABLE, FORUM_TABLE);

    private static final String CREATE_FORUM_LIKE_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                forum_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_forum_like_forum_user (forum_id, user_id)
            )
            """.formatted(FORUM_LIKE_TABLE);

    private static final String CREATE_FORUM_REPOST_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                id INT PRIMARY KEY AUTO_INCREMENT,
                forum_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_forum_repost_forum_user (forum_id, user_id)
            )
            """.formatted(FORUM_REPOST_TABLE);

    private static final String CREATE_APP_METADATA_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                meta_key VARCHAR(100) PRIMARY KEY,
                applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """.formatted(APP_METADATA_TABLE);

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
            statement.execute("SELECT 1");
            statement.executeUpdate(CREATE_USER_TABLE);
            statement.executeUpdate(CREATE_REGIME_TABLE);
            statement.executeUpdate(CREATE_REPAS_TABLE);
            statement.executeUpdate(CREATE_FORUM_TABLE);
            statement.executeUpdate(CREATE_COMMENT_TABLE);
            statement.executeUpdate(CREATE_FORUM_LIKE_TABLE);
            statement.executeUpdate(CREATE_FORUM_REPOST_TABLE);
            statement.executeUpdate(CREATE_APP_METADATA_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_TABLE);
            statement.executeUpdate(CREATE_SUPPLEMENT_ORDER_ITEM_TABLE);
        }
        migrateForumSocialSchema(connection);
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

    private static void migrateForumSocialSchema(Connection connection) throws SQLException {
        ensureColumn(connection, FORUM_TABLE, "user_id",
                "ALTER TABLE `" + FORUM_TABLE + "` ADD COLUMN user_id INT NULL AFTER id");
        ensureColumn(connection, FORUM_TABLE, "image_path",
                "ALTER TABLE `" + FORUM_TABLE + "` ADD COLUMN image_path VARCHAR(500) NULL AFTER content");
        ensureColumn(connection, COMMENT_TABLE, "user_id",
                "ALTER TABLE `" + COMMENT_TABLE + "` ADD COLUMN user_id INT NULL AFTER id");

        if (!metadataFlagExists(connection, FORUM_SOCIAL_RESET_KEY)) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM `" + FORUM_LIKE_TABLE + "`");
                statement.executeUpdate("DELETE FROM `" + FORUM_REPOST_TABLE + "`");
                statement.executeUpdate("DELETE FROM `" + COMMENT_TABLE + "`");
                statement.executeUpdate("DELETE FROM `" + FORUM_TABLE + "`");
            }
            insertMetadataFlag(connection, FORUM_SOCIAL_RESET_KEY);
        }
    }

    private static void ensureColumn(Connection connection, String tableName, String columnName, String alterQuery) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (!rs.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(alterQuery);
                }
            }
        }
    }

    private static boolean metadataFlagExists(Connection connection, String key) throws SQLException {
        String query = "SELECT 1 FROM `" + APP_METADATA_TABLE + "` WHERE meta_key = ? LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void insertMetadataFlag(Connection connection, String key) throws SQLException {
        String query = "INSERT INTO `" + APP_METADATA_TABLE + "` (meta_key) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, key);
            preparedStatement.executeUpdate();
        }
    }
}
