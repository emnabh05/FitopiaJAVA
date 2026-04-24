package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.dto.RapportContradictionDTO;
import tn.esprit.Pidev3A49.dto.RepasProblematiqueDTO;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegimeAlimentaire implements IServices<RegimeAlimentaire> {

    private static final String TABLE_NAME = SchemaInitializer.REGIME_TABLE;
    private final Connection cnx;

    public ServiceRegimeAlimentaire() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(RegimeAlimentaire regime) {
        validate(regime);
        String qry = """
                INSERT INTO %s (user_id, taille, poids, age, bmi, type_sante, calories_cibles, repas_adequats)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            remplirPreparedStatementRegime(pstm, regime, false);
            pstm.executeUpdate();
            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    regime.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le regime alimentaire.", exception);
        }
    }

    @Override
    public List<RegimeAlimentaire> getAll() {
        List<RegimeAlimentaire> regimes = new ArrayList<>();
        String qry = """
                SELECT r.id, r.user_id, u.email, r.taille, r.poids, r.age, r.bmi, r.type_sante, r.calories_cibles, r.repas_adequats
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                ORDER BY r.id DESC
                """.formatted(TABLE_NAME);

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                regimes.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les regimes alimentaires.", exception);
        }
        return regimes;
    }

    @Override
    public RegimeAlimentaire getById(int id) {
        String qry = """
                SELECT r.id, r.user_id, u.email, r.taille, r.poids, r.age, r.bmi, r.type_sante, r.calories_cibles, r.repas_adequats
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                WHERE r.id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le regime alimentaire avec l'id " + id, exception);
        }
        return null;
    }

    @Override
    public void update(RegimeAlimentaire regime) {
        validate(regime);
        String qry = """
                UPDATE %s
                SET user_id = ?, taille = ?, poids = ?, age = ?, bmi = ?, type_sante = ?, calories_cibles = ?, repas_adequats = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            remplirPreparedStatementRegime(pstm, regime, true);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le regime alimentaire.", exception);
        }
    }

    @Override
    public void delete(RegimeAlimentaire regime) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, regime.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le regime alimentaire.", exception);
        }
    }

    private RegimeAlimentaire mapResultSet(ResultSet rs) throws SQLException {
        return new RegimeAlimentaire(
                rs.getInt("id"),
                readNullableInt(rs, "user_id"),
                rs.getString("email"),
                readNullableDouble(rs, "taille"),
                readNullableDouble(rs, "poids"),
                readNullableInt(rs, "age"),
                readNullableDouble(rs, "bmi"),
                rs.getString("type_sante"),
                readNullableInt(rs, "calories_cibles"),
                rs.getString("repas_adequats")
        );
    }

    private void remplirPreparedStatementRegime(PreparedStatement pstm, RegimeAlimentaire regime, boolean withId) throws SQLException {
        pstm.setInt(1, regime.getUserId());
        setNullableDouble(pstm, 2, regime.getTaille());
        setNullableDouble(pstm, 3, regime.getPoids());
        setNullableInteger(pstm, 4, regime.getAge());
        setNullableDouble(pstm, 5, regime.getBmi());
        pstm.setString(6, regime.getTypeSante());
        setNullableInteger(pstm, 7, regime.getCaloriesCibles());
        pstm.setString(8, regime.getRepasAdequats());
        if (withId) {
            pstm.setInt(9, regime.getId());
        }
    }


    /**
     * Moteur d'audit intelligent des régimes alimentaires.
     * Analyse les contradictions entre un régime et ses repas associés.
     */
    public RapportContradictionDTO analyserContradictionsRegime(int regimeId) {
        RapportContradictionDTO rapport = new RapportContradictionDTO();
        List<RepasProblematiqueDTO> repasProblematiques = new ArrayList<>();

        // 1. Requête SQL complexe pour les statistiques globales et le taux de contradiction
        String qryStats = """
                SELECT
                    rg.id AS regime_id,
                    rg.type_sante AS regime_objectif,
                    COUNT(rp.id_repas) AS nombre_repas,
                    SUM(rp.calories) AS total_calories,
                    AVG(rp.calories) AS moyenne_calories,
                    AVG(rp.proteines) AS moyenne_proteines,
                    AVG(rp.glucides) AS moyenne_glucides,
                    AVG(rp.lipides) AS moyenne_lipides,
                    SUM(
                        CASE
                            WHEN rg.type_sante = 'PERTE_POIDS' AND rp.calories > 600 THEN 1
                            WHEN rg.type_sante = 'PERTE_POIDS' AND rp.lipides > 25 THEN 1
                            WHEN rg.type_sante = 'PRISE_MASSE' AND rp.proteines < 20 THEN 1
                            WHEN rg.type_sante = 'DIABETIQUE' AND rp.glucides > 45 THEN 1
                            WHEN rg.type_sante = 'EQUILIBRE' AND (rp.calories > 700 OR rp.lipides > 30 OR rp.glucides > 80) THEN 1
                            ELSE 0
                        END
                    ) AS nombre_contradictions,
                    ROUND(
                        SUM(
                            CASE
                                WHEN rg.type_sante = 'PERTE_POIDS' AND rp.calories > 600 THEN 1
                                WHEN rg.type_sante = 'PERTE_POIDS' AND rp.lipides > 25 THEN 1
                                WHEN rg.type_sante = 'PRISE_MASSE' AND rp.proteines < 20 THEN 1
                                WHEN rg.type_sante = 'DIABETIQUE' AND rp.glucides > 45 THEN 1
                                WHEN rg.type_sante = 'EQUILIBRE' AND (rp.calories > 700 OR rp.lipides > 30 OR rp.glucides > 80) THEN 1
                                ELSE 0
                            END
                        ) * 100.0 / NULLIF(COUNT(rp.id_repas), 0), 2
                    ) AS taux_contradiction
                FROM %s rg
                JOIN %s rp ON rg.id = rp.regime_id
                WHERE rg.id = ?
                GROUP BY rg.id, rg.type_sante
                """.formatted(TABLE_NAME, SchemaInitializer.REPAS_TABLE);

        // 2. Requête pour récupérer les repas problématiques détaillés
        String qryRepas = """
                SELECT
                    rp.id_repas AS repas_id,
                    rp.nom_repas AS repas_nom,
                    rp.type_repas,
                    rp.calories,
                    rp.proteines,
                    rp.glucides,
                    rp.lipides,
                    CASE
                        WHEN rg.type_sante = 'PERTE_POIDS' AND rp.calories > 600 THEN 'Calories trop élevées pour une perte de poids'
                        WHEN rg.type_sante = 'PERTE_POIDS' AND rp.lipides > 25 THEN 'Lipides trop élevés pour une perte de poids'
                        WHEN rg.type_sante = 'PRISE_MASSE' AND rp.proteines < 20 THEN 'Protéines insuffisantes pour une prise de masse'
                        WHEN rg.type_sante = 'DIABETIQUE' AND rp.glucides > 45 THEN 'Glucides trop élevés pour un régime diabétique'
                        WHEN rg.type_sante = 'EQUILIBRE' AND rp.calories > 700 THEN 'Calories trop élevées pour un régime équilibré'
                        WHEN rg.type_sante = 'EQUILIBRE' AND rp.lipides > 30 THEN 'Lipides trop élevés pour un régime équilibré'
                        WHEN rg.type_sante = 'EQUILIBRE' AND rp.glucides > 80 THEN 'Glucides trop élevés pour un régime équilibré'
                        ELSE 'Compatible'
                    END AS diagnostic,
                    CASE
                        WHEN rg.type_sante = 'PERTE_POIDS' AND rp.calories > 600 THEN 40
                        WHEN rg.type_sante = 'PERTE_POIDS' AND rp.lipides > 25 THEN 30
                        WHEN rg.type_sante = 'PRISE_MASSE' AND rp.proteines < 20 THEN 35
                        WHEN rg.type_sante = 'DIABETIQUE' AND rp.glucides > 45 THEN 45
                        WHEN rg.type_sante = 'EQUILIBRE' AND (rp.calories > 700 OR rp.lipides > 30 OR rp.glucides > 80) THEN 25
                        ELSE 0
                    END AS score_risque
                FROM %s rg
                JOIN %s rp ON rg.id = rp.regime_id
                WHERE rg.id = ?
                HAVING score_risque > 0
                ORDER BY score_risque DESC, rp.calories DESC, rp.lipides DESC
                """.formatted(TABLE_NAME, SchemaInitializer.REPAS_TABLE);

        try (PreparedStatement pstmStats = cnx.prepareStatement(qryStats)) {
            pstmStats.setInt(1, regimeId);
            try (ResultSet rs = pstmStats.executeQuery()) {
                if (rs.next()) {
                    rapport.setNomRegime("Régime #" + rs.getInt("regime_id"));
                    rapport.setObjectif(rs.getString("regime_objectif"));
                    rapport.setNombreRepas(rs.getInt("nombre_repas"));
                    rapport.setTotalCalories(rs.getInt("total_calories"));
                    rapport.setMoyenneCalories(rs.getDouble("moyenne_calories"));
                    rapport.setMoyenneProteines(rs.getDouble("moyenne_proteines"));
                    rapport.setMoyenneGlucides(rs.getDouble("moyenne_glucides"));
                    rapport.setMoyenneLipides(rs.getDouble("moyenne_lipides"));
                    rapport.setNombreContradictions(rs.getInt("nombre_contradictions"));
                    rapport.setTauxContradiction(rs.getDouble("taux_contradiction"));
                } else {
                    // Cas où le régime n'a aucun repas ou n'existe pas
                    RegimeAlimentaire reg = getById(regimeId);
                    if (reg != null) {
                        rapport.setNomRegime("Régime #" + reg.getId());
                        rapport.setObjectif(reg.getTypeSante());
                        rapport.setNombreRepas(0);
                        rapport.setTauxContradiction(0);
                        rapport.setDiagnosticGlobal("Aucun repas enregistré pour ce régime.");
                        rapport.setConseilAutomatique("Commencez par ajouter des repas pour obtenir une analyse.");
                        rapport.setRepasProblematiques(new ArrayList<>());
                        return rapport;
                    }
                    return null;
                }
            }

            // Récupération des repas problématiques
            try (PreparedStatement pstmRepas = cnx.prepareStatement(qryRepas)) {
                pstmRepas.setInt(1, regimeId);
                try (ResultSet rsRepas = pstmRepas.executeQuery()) {
                    while (rsRepas.next()) {
                        repasProblematiques.add(new RepasProblematiqueDTO(
                                rsRepas.getInt("repas_id"),
                                rsRepas.getString("repas_nom"),
                                rsRepas.getString("type_repas"),
                                rsRepas.getInt("calories"),
                                rsRepas.getInt("proteines"),
                                rsRepas.getInt("glucides"),
                                rsRepas.getInt("lipides"),
                                rsRepas.getString("diagnostic"),
                                rsRepas.getInt("score_risque")
                        ));
                    }
                }
            }

            rapport.setRepasProblematiques(repasProblematiques);

            // Génération du diagnostic global et du conseil automatique
            double taux = rapport.getTauxContradiction();
            if (taux <= 20) {
                rapport.setDiagnosticGlobal("Régime cohérent");
                rapport.setConseilAutomatique("Excellent travail ! Votre régime est bien suivi et les repas sont adaptés à votre objectif.");
            } else if (taux <= 50) {
                rapport.setDiagnosticGlobal("Régime à surveiller");
                rapport.setConseilAutomatique("Attention, certains repas s'éloignent de votre objectif nutritionnel. Essayez d'ajuster vos choix.");
            } else if (taux <= 80) {
                rapport.setDiagnosticGlobal("Régime contradictoire");
                rapport.setConseilAutomatique("Votre alimentation actuelle est en forte contradiction avec votre objectif. Une révision complète est recommandée.");
            } else {
                rapport.setDiagnosticGlobal("Régime dangereux ou mal construit");
                rapport.setConseilAutomatique("ALERTE : Vos repas sont totalement inadaptés. Veuillez consulter un nutritionniste ou revoir vos objectifs.");
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Erreur lors de l'audit du régime.", exception);
        }

        return rapport;
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }

    private Integer readNullableInt(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private Double readNullableDouble(ResultSet resultSet, String column) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }
}
