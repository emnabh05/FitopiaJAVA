package tn.esprit.gestionrepas.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private static final String DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/fitopiabd?useSSL=false&serverTimezone=UTC";
    private final String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
    private final String username = System.getenv().getOrDefault("DB_USERNAME", "root");
    private final String password = System.getenv().getOrDefault("DB_PASSWORD", "");
    private final Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(url, username, password);
            System.out.println("Connexion etablie vers la base de donnees.");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de se connecter a la base de donnees : " + e.getMessage(), e);
        }
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }

        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
