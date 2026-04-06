package tn.esprit.Pidev3A49.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyDataBase {

    private static MyDataBase instance;
    private final String url;
    private final String username;
    private final String password;
    private Connection cnx;

    private MyDataBase() {
        Properties properties = loadProperties();
        url = properties.getProperty("db.url", "jdbc:mysql://127.0.0.1:3306/fitopiabd?serverTimezone=UTC");
        username = properties.getProperty("db.username", "root");
        password = properties.getProperty("db.password", "");

        try {
            cnx = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to fitopiabd");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur de connexion a la base de donnees: " + e.getMessage(), e);
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

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier db.properties", e);
        }
        return properties;
    }
}
