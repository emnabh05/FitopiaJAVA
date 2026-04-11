package tn.esprit.Pidev3A49.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {

    private static final String PARTICIPATION_BACK_VIEW_SQL = """
            CREATE OR REPLACE VIEW participation_back_view AS
            SELECT
                p.id_participation,
                p.id_event,
                COALESCE(e.titre, CONCAT('Event #', p.id_event)) AS evenement,
                p.nom_participant,
                p.email_participant,
                p.date_inscription
            FROM participation p
            LEFT JOIN events e ON e.id_event = p.id_event
            ORDER BY p.id_participation DESC
            """;

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
                ensureBackViews(connection);
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

    private void ensureBackViews(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(PARTICIPATION_BACK_VIEW_SQL);
            System.out.println("Vue SQL prete : participation_back_view");
        } catch (SQLException e) {
            System.err.println("Impossible de creer la vue participation_back_view.");
            e.printStackTrace();
        }
    }
}
