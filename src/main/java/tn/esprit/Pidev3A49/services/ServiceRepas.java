package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.dto.RepasSimilariteDTO;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepas implements IServices<Repas> {

    private static final String TABLE_NAME = SchemaInitializer.REPAS_TABLE;
    private final Connection cnx;

    public ServiceRepas() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Repas repas) {
        validate(repas);
        String qry = """
                INSERT INTO %s (user_id, date_repas, type_repas, nom_repas, calories, proteines, glucides, lipides, commentaire, regime_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            remplirPreparedStatementRepas(pstm, repas, false);
            pstm.executeUpdate();
            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    repas.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le repas. Cause SQL: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Repas> getAll() {
        List<Repas> repasList = new ArrayList<>();
        String qry = """
                SELECT r.id_repas, r.user_id, u.email, r.date_repas, r.type_repas, r.nom_repas, r.calories,
                       r.proteines, r.glucides, r.lipides, r.commentaire, r.regime_id,
                       rg.type_sante, rg.calories_cibles
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                LEFT JOIN %s rg ON rg.id = r.regime_id
                ORDER BY r.date_repas DESC, r.id_repas DESC
                """.formatted(TABLE_NAME, SchemaInitializer.REGIME_TABLE);

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                repasList.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les repas. Cause SQL: " + exception.getMessage(), exception);
        }

        return repasList;
    }

    @Override
    public Repas getById(int id) {
        String qry = """
                SELECT r.id_repas, r.user_id, u.email, r.date_repas, r.type_repas, r.nom_repas, r.calories,
                       r.proteines, r.glucides, r.lipides, r.commentaire, r.regime_id,
                       rg.type_sante, rg.calories_cibles
                FROM %s r
                LEFT JOIN users u ON u.id = r.user_id
                LEFT JOIN %s rg ON rg.id = r.regime_id
                WHERE r.id_repas = ?
                """.formatted(TABLE_NAME, SchemaInitializer.REGIME_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le repas avec l'id " + id, exception);
        }

        return null;
    }

    @Override
    public void update(Repas repas) {
        validate(repas);
        String qry = """
                UPDATE %s
                SET user_id = ?, date_repas = ?, type_repas = ?, nom_repas = ?, calories = ?, proteines = ?,
                    glucides = ?, lipides = ?, commentaire = ?, regime_id = ?
                WHERE id_repas = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            remplirPreparedStatementRepas(pstm, repas, true);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le repas. Cause SQL: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void delete(Repas repas) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id_repas = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, repas.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le repas.", exception);
        }
    }

    private Repas mapResultSet(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("date_repas");
        Integer regimeId = readNullableInt(rs, "regime_id");
        String regimeDisplay = null;
        if (regimeId != null) {
            regimeDisplay = "#" + regimeId + " - " + nullable(rs.getString("type_sante")) + " - "
                    + (readNullableInt(rs, "calories_cibles") == null ? 0 : readNullableInt(rs, "calories_cibles")) + " kcal";
        }

        return new Repas(
                rs.getInt("id_repas"),
                readNullableInt(rs, "user_id"),
                rs.getString("email"),
                timestamp == null ? null : timestamp.toLocalDateTime(),
                rs.getString("type_repas"),
                rs.getString("nom_repas"),
                readNullableInt(rs, "calories"),
                readNullableInt(rs, "proteines"),
                readNullableInt(rs, "glucides"),
                readNullableInt(rs, "lipides"),
                rs.getString("commentaire"),
                regimeId,
                regimeDisplay
        );
    }

    private void remplirPreparedStatementRepas(PreparedStatement pstm, Repas repas, boolean withId) throws SQLException {
        pstm.setInt(1, repas.getUserId());
        pstm.setTimestamp(2, Timestamp.valueOf(repas.getDateRepas()));
        pstm.setString(3, repas.getTypeRepas());
        pstm.setString(4, repas.getNomRepas());
        setNullableInteger(pstm, 5, repas.getCalories());
        setNullableInteger(pstm, 6, repas.getProteines());
        setNullableInteger(pstm, 7, repas.getGlucides());
        setNullableInteger(pstm, 8, repas.getLipides());
        pstm.setString(9, repas.getCommentaire());
        setNullableInteger(pstm, 10, repas.getRegimeId());
        if (withId) {
            pstm.setInt(11, repas.getId());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MOTEUR DE RECOMMANDATION — Similarité Nutritionnelle
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recommande les 5 repas dont le profil nutritionnel est le plus proche
     * des objectifs caloriques du régime alimentaire donné.
     *
     * <p><b>Algorithme — Distance de Manhattan</b> :</p>
     * <pre>
     *   score = |calories_repas - objectif_calories|
     *         + |proteines_repas - moy_proteines_regime|
     *         + |glucides_repas  - moy_glucides_regime|
     *         + |lipides_repas   - moy_lipides_regime|
     * </pre>
     *
     * <p>On utilise {@code ABS()} pour mesurer l'écart dans les deux sens :
     * un repas trop riche ET un repas trop pauvre s'éloignent tous les deux
     * de l'objectif, et doivent donc être pénalisés de façon symétrique.</p>
     *
     * <p>{@code ORDER BY score_similarite ASC} garantit que les repas les
     * plus proches de l'objectif arrivent en tête de liste.</p>
     *
     * <p>{@code LIMIT 5} restreint la réponse aux 5 meilleurs candidats,
     * ce qui est suffisant pour une interface de recommandation.</p>
     *
     * @param regimeId identifiant du régime alimentaire cible
     * @return liste des 5 repas les plus similaires, triés par score croissant.
     *         Retourne une liste vide si le régime est introuvable ou si la
     *         base ne contient aucun repas.
     * @throws IllegalArgumentException si {@code regimeId} est null ou négatif
     * @throws IllegalStateException    en cas d'erreur d'accès à la base de données
     */
    public List<RepasSimilariteDTO> trouverRepasSimilaires(int regimeId) {
        if (regimeId <= 0) {
            throw new IllegalArgumentException("L'identifiant du régime doit être un entier positif.");
        }

        /*
         * Requête SQL complexe avec :
         *  • JOIN entre repas (r) et regime_alimentaire (rg) — chaque repas
         *    est comparé aux objectifs du régime cible.
         *  • ABS() — mesure l'écart absolu entre la valeur réelle et l'objectif.
         *    Sans ABS, un repas à -200 kcal de l'objectif et un repas à +200 kcal
         *    auraient le même bilan algébrique (0 si on additionne), ce qui serait
         *    trompeur.
         *  • Les objectifs macros (protéines, glucides, lipides) sont estimés à
         *    partir des moyennes des repas déjà associés au régime. Si aucun repas
         *    n'est encore associé, les sous-requêtes retournent NULL et COALESCE
         *    remplace par 0, évitant toute erreur.
         *  • ORDER BY score_similarite ASC — les repas les plus proches en premier.
         *  • LIMIT 5 — on ne garde que les 5 meilleurs candidats.
         */
        String qry = """
                SELECT
                    r.id_repas,
                    r.nom_repas,
                    r.type_repas,
                    COALESCE(r.calories,  0) AS calories,
                    COALESCE(r.proteines, 0) AS proteines,
                    COALESCE(r.glucides,  0) AS glucides,
                    COALESCE(r.lipides,   0) AS lipides,
                    (
                        ABS(COALESCE(r.calories,  0) - COALESCE(rg.calories_cibles, 0))
                      + ABS(COALESCE(r.proteines, 0) - COALESCE(
                              (SELECT AVG(rp2.proteines) FROM %2$s rp2 WHERE rp2.regime_id = rg.id), 0))
                      + ABS(COALESCE(r.glucides,  0) - COALESCE(
                              (SELECT AVG(rp2.glucides)  FROM %2$s rp2 WHERE rp2.regime_id = rg.id), 0))
                      + ABS(COALESCE(r.lipides,   0) - COALESCE(
                              (SELECT AVG(rp2.lipides)   FROM %2$s rp2 WHERE rp2.regime_id = rg.id), 0))
                    ) AS score_similarite
                FROM %2$s r
                JOIN %1$s rg ON rg.id = ?
                ORDER BY score_similarite ASC
                LIMIT 5
                """.formatted(SchemaInitializer.REGIME_TABLE, TABLE_NAME);

        List<RepasSimilariteDTO> resultats = new ArrayList<>();

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, regimeId);

            try (ResultSet rs = pstm.executeQuery()) {
                // Si le ResultSet est vide, le régime est introuvable OU il n'y a aucun repas :
                // on retourne une liste vide (comportement défensif).
                while (rs.next()) {
                    resultats.add(mapRepasSimilariteResultSet(rs, "score_similarite"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur lors de la recherche de repas similaires au régime id=" + regimeId, e);
        }

        return resultats;
    }

    /**
     * Recommande les 5 repas dont le profil nutritionnel est le plus proche
     * d'un repas de référence donné.
     *
     * <p>Utile pour proposer des alternatives équivalentes à un repas apprécié,
     * ou pour trouver des repas interchangeables dans un planning hebdomadaire.</p>
     *
     * <p><b>Algorithme — Distance de Manhattan entre deux repas</b> :</p>
     * <pre>
     *   distance = |cal(r1) - cal(r2)| + |prot(r1) - prot(r2)|
     *            + |gluc(r1) - gluc(r2)| + |lip(r1) - lip(r2)|
     * </pre>
     *
     * <p>La jointure {@code r1.id <> r2.id} exclut le repas lui-même
     * des résultats, évitant qu'un repas soit recommandé comme son propre
     * substitut.</p>
     *
     * @param repasId identifiant du repas de référence
     * @return liste des 5 repas les plus proches nutritionnellement, triés
     *         par distance croissante. Liste vide si le repas est introuvable
     *         ou si aucun autre repas n'existe en base.
     * @throws IllegalArgumentException si {@code repasId} est null ou négatif
     * @throws IllegalStateException    en cas d'erreur d'accès à la base de données
     */
    public List<RepasSimilariteDTO> trouverRepasSimilairesAUnRepas(int repasId) {
        if (repasId <= 0) {
            throw new IllegalArgumentException("L'identifiant du repas doit être un entier positif.");
        }

        /*
         * Requête SQL avec auto-jointure (repas r1 vs repas r2) :
         *  • r1 est le repas de référence (WHERE r1.id_repas = ?).
         *  • r2 représente tous les autres repas (r1.id_repas <> r2.id_repas).
         *  • ABS() calcule l'écart absolu pour chaque macro-nutriment.
         *  • ORDER BY distance ASC — les repas les plus proches en premier.
         *  • LIMIT 5 — top 5 des repas substituts.
         */
        String qry = """
                SELECT
                    r2.id_repas,
                    r2.nom_repas,
                    r2.type_repas,
                    COALESCE(r2.calories,  0) AS calories,
                    COALESCE(r2.proteines, 0) AS proteines,
                    COALESCE(r2.glucides,  0) AS glucides,
                    COALESCE(r2.lipides,   0) AS lipides,
                    (
                        ABS(COALESCE(r1.calories,  0) - COALESCE(r2.calories,  0))
                      + ABS(COALESCE(r1.proteines, 0) - COALESCE(r2.proteines, 0))
                      + ABS(COALESCE(r1.glucides,  0) - COALESCE(r2.glucides,  0))
                      + ABS(COALESCE(r1.lipides,   0) - COALESCE(r2.lipides,   0))
                    ) AS score_similarite
                FROM %1$s r1
                JOIN %1$s r2 ON r1.id_repas <> r2.id_repas
                WHERE r1.id_repas = ?
                ORDER BY score_similarite ASC
                LIMIT 5
                """.formatted(TABLE_NAME);

        List<RepasSimilariteDTO> resultats = new ArrayList<>();

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, repasId);

            try (ResultSet rs = pstm.executeQuery()) {
                // Liste vide si le repas est introuvable ou si aucun autre repas n'existe.
                while (rs.next()) {
                    resultats.add(mapRepasSimilariteResultSet(rs, "score_similarite"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erreur lors de la recherche de repas similaires au repas id=" + repasId, e);
        }

        return resultats;
    }

    /**
     * Mappe une ligne du ResultSet vers un {@link RepasSimilariteDTO}.
     *
     * <p>Les valeurs nutritionnelles sont lues comme {@code double} car
     * le moteur SQL peut retourner des moyennes décimales (AVG).</p>
     *
     * @param rs          le ResultSet positionné sur la ligne courante
     * @param scoreColumn nom de la colonne SQL contenant le score de similarité
     * @return instance de {@link RepasSimilariteDTO} renseignée
     * @throws SQLException en cas de problème de lecture du ResultSet
     */
    private RepasSimilariteDTO mapRepasSimilariteResultSet(ResultSet rs, String scoreColumn) throws SQLException {
        return new RepasSimilariteDTO(
                rs.getInt("id_repas"),
                rs.getString("nom_repas"),
                rs.getString("type_repas"),
                rs.getDouble("calories"),
                rs.getDouble("proteines"),
                rs.getDouble("glucides"),
                rs.getDouble("lipides"),
                rs.getDouble(scoreColumn)  // Score de distance nutritionnelle (0 = parfait)
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Méthodes privées utilitaires
    // ═══════════════════════════════════════════════════════════════════════════

    private void validate(Repas repas) {
        if (repas == null) {
            throw new IllegalArgumentException("Le repas est obligatoire.");
        }
        if (repas.getUserId() == null) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire.");
        }
        if (repas.getDateRepas() == null) {
            throw new IllegalArgumentException("La date du repas est obligatoire.");
        }
        if (repas.getTypeRepas() == null || repas.getTypeRepas().isBlank()) {
            throw new IllegalArgumentException("Le type de repas est obligatoire.");
        }
        if (repas.getNomRepas() == null || repas.getNomRepas().isBlank()) {
            throw new IllegalArgumentException("Le nom du repas est obligatoire.");
        }
        if (repas.getCalories() != null && repas.getCalories() < 0) {
            throw new IllegalArgumentException("Les calories doivent etre positives.");
        }
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private Integer readNullableInt(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
