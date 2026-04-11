package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/fitopiabd?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static MyDataBase instance;
    private Connection connection;

    private MyDataBase() {
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connexion a MySQL : " + URL);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion MySQL reussie.");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Impossible de se connecter a la base fitopiabd.");
            e.printStackTrace();
            throw new RuntimeException("Impossible de se connecter a la base fitopiabd : " + e.getMessage(), e);
        }
    }

    public Connection getCnx() {
        return getConnection();
    }
}