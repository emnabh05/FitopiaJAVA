package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {

    private static MyDataBase instance;

    private final String host = readConfig("db.host", "DB_HOST", "127.0.0.1");
    private final String port = readConfig("db.port", "DB_PORT", "3306");
    private final String databaseName = readConfig("db.name", "DB_NAME", "fitopiabd");
    private final String username = readConfig("db.user", "DB_USER", "root");
    private final String password = readConfig("db.password", "DB_PASSWORD", "");
    private final String serverUrl = "jdbc:mysql://" + host + ":" + port
            + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private final String databaseUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private Connection cnx;

    private MyDataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            createDatabaseIfNeeded();
            cnx = DriverManager.getConnection(databaseUrl, username, password);
            SchemaInitializer.initialize(cnx);
            System.out.println("Connexion etablie avec la base " + databaseName);
        } catch (ClassNotFoundException | SQLException exception) {
            throw new IllegalStateException(
                    "Impossible de se connecter a MySQL. Verifie le host, l'utilisateur et le mot de passe.",
                    exception
            );
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }

        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    private void createDatabaseIfNeeded() throws SQLException {
        try (Connection initConnection = DriverManager.getConnection(serverUrl, username, password);
             Statement statement = initConnection.createStatement()) {
            statement.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS `" + databaseName
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            );
        }
    }

    private String readConfig(String systemProperty, String envVariable, String defaultValue) {
        String systemValue = System.getProperty(systemProperty);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(envVariable);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}
