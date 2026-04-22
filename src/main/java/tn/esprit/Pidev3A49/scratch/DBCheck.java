package tn.esprit.Pidev3A49.scratch;

import tn.esprit.Pidev3A49.utils.MyDataBase;
import java.sql.*;

public class DBCheck {
    public static void main(String[] args) {
        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            System.out.println("Connection: " + (cnx != null));
            
            checkTable(cnx, "users");
            checkTable(cnx, "regime_alimentaire");
            checkTable(cnx, "repas");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkTable(Connection cnx, String table) throws SQLException {
        System.out.println("\n--- Table: " + table + " ---");
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 5")) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                System.out.print(md.getColumnName(i) + " \t");
            }
            System.out.println();
            int count = 0;
            while (rs.next()) {
                count++;
                for (int i = 1; i <= cols; i++) {
                    System.out.print(rs.getObject(i) + " \t");
                }
                System.out.println();
            }
            System.out.println("Found: " + count + " rows (first 5 shown)");
        } catch (Exception e) {
            System.out.println("Error reading table " + table + ": " + e.getMessage());
        }
    }
}
