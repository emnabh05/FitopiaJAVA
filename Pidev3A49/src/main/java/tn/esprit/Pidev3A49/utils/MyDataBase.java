package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance ;
    private static final String DB_NAME = "fitopiabd";
    private final String URL = "jdbc:mysql://127.0.0.1:3306/" + DB_NAME + "?createDatabaseIfNotExist=true&serverTimezone=UTC";
    private final String USERNAME ="root";
    private final String PASSWORD ="";
    private Connection cnx ;

   private MyDataBase(){
       try {
           cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
           SchemaInitializer.initialize(cnx);

           System.out.println("Connected ...");
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
}
