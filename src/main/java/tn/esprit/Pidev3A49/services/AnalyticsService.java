package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.RegimeInsight;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'Analyses Avancées pour l'application Fitopia.
 * Ce service implémente une logique métier complexe ("Advanced Business Logic") 
 * demandée pour éblouir lors de la présentation du projet.
 */
public class AnalyticsService {

    private final Connection cnx;

    public AnalyticsService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Méthode MÉTIER AVANCÉE : Audit de performance et d'adhérence nutritionnelle.
     * 
     * Cette méthode combine :
     * 1. Une requête SQL complexe (JOIN, GROUP BY, Scalar Subqueries, Aggregations, HAVING, ORDER BY).
     * 2. Une logique de traitement Java avancée utilisant Stream API.
     * 3. Un algorithme de calcul de "Score d'Adhérence" personnalisé.
     * 
     * @return Une liste de RegimeInsight triée par score de performance.
     */
    public List<RegimeInsight> generateRegimeHealthAudit() {
        List<RegimeInsight> insights = new ArrayList<>();

        // REQUETE SQL COMPLEXE :
        // Elle récupère les statistiques globales par régime tout en utilisant une sous-requête corrélée
        // pour déterminer dynamiquement le type de repas le plus fréquent (favorite_meal) pour chaque régime.
        String query = """
            SELECT 
                r.id as regime_id,
                r.type_sante as label,
                r.calories_cibles as target,
                COUNT(rep.id) as total_meals,
                COALESCE(AVG(rep.calories), 0) as avg_calories_per_meal,
                COALESCE(SUM(rep.proteines), 0) as total_prot,
                COALESCE(SUM(rep.glucides), 0) as total_gluc,
                COALESCE(SUM(rep.lipides), 0) as total_lip,
                (SELECT rep2.type_repas 
                 FROM %s rep2 
                 WHERE rep2.regime_id = r.id 
                 GROUP BY rep2.type_repas 
                 ORDER BY COUNT(*) DESC 
                 LIMIT 1) as favorite_meal
            FROM %s r
            INNER JOIN %s rep ON r.id = rep.regime_id
            GROUP BY r.id, r.type_sante, r.calories_cibles
            HAVING total_meals > 0
            ORDER BY total_meals DESC
        """.formatted(SchemaInitializer.REPAS_TABLE, SchemaInitializer.REGIME_TABLE, SchemaInitializer.REPAS_TABLE);

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                RegimeInsight insight = new RegimeInsight(
                        rs.getInt("regime_id"),
                        rs.getString("label"),
                        rs.getInt("target"),
                        rs.getInt("total_meals"),
                        rs.getDouble("avg_calories_per_meal"),
                        rs.getDouble("total_prot"),
                        rs.getDouble("total_gluc"),
                        rs.getDouble("total_lip"),
                        rs.getString("favorite_meal")
                );
                insights.add(insight);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'exécution de l'audit analytique : " + e.getMessage());
            // Dans un vrai projet, on utiliserait un logger
        }

        // LOGIQUE JAVA COMPLEXE :
        // On utilise l'API Stream pour enrichir les objets DTO avec des calculs algorithmiques
        // et on effectue un tri personnalisé final.
        return insights.stream()
                .map(this::enrichWithIntelligence)
                .sorted(Comparator.comparingDouble(RegimeInsight::getAdherenceScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * "Intelligence Artificielle" simplifiée pour enrichir les données du régime.
     * Calcule le profil macro-nutritionnel dominant et évalue la qualité du suivi.
     */
    private RegimeInsight enrichWithIntelligence(RegimeInsight insight) {
        // 1. Détermination du profil dominant via Stream sur une Map
        Map<String, Double> macros = new HashMap<>();
        macros.put("Haut Protéiné (Protéines)", insight.getTotalProt());
        macros.put("Riche en Glucides (Glucides)", insight.getTotalGluc());
        macros.put("Riche en Lipides (Lipides)", insight.getTotalLip());

        String profile = macros.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("Equilibré / Inconnu");
        
        insight.setMacroDominant(profile);

        // 2. Calcul du Score d'Adhérence (Algorithme métier)
        // On compare l'apport calorique moyen extrapolé sur 3 repas à la cible.
        double dailyExtrapolated = insight.getAvgCaloriesPerMeal() * 3;
        double deviation = Math.abs(dailyExtrapolated - insight.getTargetCalories());
        
        // Plus l'écart est faible, plus le score est haut (base 80 points)
        double accuracyScore = Math.max(0, 80 - (deviation / (insight.getTargetCalories() + 1) * 100));
        
        // Bonus de fidélité : Plus l'utilisateur logue de repas, plus son score est récompensé (max 20 points)
        double loyaltyBonus = Math.min(20, insight.getTotalMeals() * 1.5);
        
        insight.setAdherenceScore(Math.min(100, accuracyScore + loyaltyBonus));

        return insight;
    }
}
