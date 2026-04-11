package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {

    private static MyDataBase instance ;
    private final String SERVER_URL ="jdbc:mysql://127.0.0.1:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private final String DATABASE_NAME = "fitopiabd";
    private final String URL ="jdbc:mysql://127.0.0.1:3306/" + DATABASE_NAME + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private final String USERNAME ="root";
    private final String PASSWORD ="";
    private Connection cnx ;

   private MyDataBase(){
       try {
           initializeDatabase();
           cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
           System.out.println("Connected to " + DATABASE_NAME);
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }
   }


    public static MyDataBase getInstance(){
       if (instance == null)
           instance = new MyDataBase();

       return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    private void initializeDatabase() throws SQLException {
        try (Connection serverConnection = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement statement = serverConnection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DATABASE_NAME + "`");
        }
    }
}
