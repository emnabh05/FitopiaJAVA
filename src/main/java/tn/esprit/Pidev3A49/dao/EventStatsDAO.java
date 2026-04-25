package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.EventStats;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventStatsDAO {

    private static final String TOP_PROFITABLE_EVENTS_SQL = """
            SELECT
                e.id_event,
                e.titre,
                e.capacite,
                e.prix_event,
                COUNT(r.id) AS nb_reservations,
                COALESCE(SUM(r.montant), 0) AS revenu_total,
                COALESCE(ROUND((COUNT(r.id) * 100.0 / NULLIF(e.capacite, 0)), 2), 0) AS taux_remplissage
            FROM events e
            LEFT JOIN reservation r
                ON e.id_event = r.id_event
               AND UPPER(TRIM(r.statut)) IN ('CONFIRMEE', 'UTILISEE', 'PAYEE')
            GROUP BY e.id_event, e.titre, e.capacite, e.prix_event
            HAVING COUNT(r.id) > 0
            ORDER BY revenu_total DESC, nb_reservations DESC, taux_remplissage DESC
            """;

    private final Connection connection;

    public EventStatsDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public List<EventStats> getTopProfitableEvents() {
        List<EventStats> statsList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(TOP_PROFITABLE_EVENTS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                EventStats stats = new EventStats(
                        resultSet.getInt("id_event"),
                        resultSet.getString("titre"),
                        resultSet.getInt("capacite"),
                        resultSet.getDouble("prix_event"),
                        resultSet.getInt("nb_reservations"),
                        resultSet.getDouble("revenu_total"),
                        resultSet.getDouble("taux_remplissage")
                );
                statsList.add(stats);
            }
        } catch (SQLException e) {
            logSqlError(e);
            throw new RuntimeException("Echec SQL lors du calcul des statistiques des evenements.", e);
        }

        return statsList;
    }

    private void logSqlError(SQLException e) {
        System.err.println("Erreur SQL sur l'analyse des evenements.");
        System.err.println("SQL : " + TOP_PROFITABLE_EVENTS_SQL);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
