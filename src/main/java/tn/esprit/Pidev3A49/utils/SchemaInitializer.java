package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static final String USER_TABLE = "users";
    public static final String REGIME_TABLE = "regime_alimentaire";
    public static final String REPAS_TABLE = "repas";

    private SchemaInitializer() {
    }

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        }
    }
}
