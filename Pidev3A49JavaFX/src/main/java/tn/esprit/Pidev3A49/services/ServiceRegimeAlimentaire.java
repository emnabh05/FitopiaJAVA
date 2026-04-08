package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceRegimeAlimentaire implements IServices<RegimeAlimentaire> {

    private static final String TABLE_NAME = SchemaInitializer.REGIME_TABLE;

    private final Connection cnx;

    public ServiceRegimeAlimentaire() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(RegimeAlimentaire regimeAlimentaire) {
        validate(regimeAlimentaire);
        String qry = """
                INSERT INTO %s (nom, description, objectif_calorique, actif)
                VALUES (?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, regimeAlimentaire.getNom());
            pstm.setString(2, regimeAlimentaire.getDescription());
            pstm.setInt(3, regimeAlimentaire.getObjectifCalorique());
            pstm.setBoolean(4, regimeAlimentaire.isActif());
            pstm.executeUpdate();

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    regimeAlimentaire.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le regime alimentaire.", exception);
        }
    }

    @Override
    public List<RegimeAlimentaire> getAll() {
        List<RegimeAlimentaire> regimes = new ArrayList<>();
        String qry = "SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC";

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
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

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
    public void update(RegimeAlimentaire regimeAlimentaire) {
        validate(regimeAlimentaire);
        String qry = """
                UPDATE %s
                SET nom = ?, description = ?, objectif_calorique = ?, actif = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, regimeAlimentaire.getNom());
            pstm.setString(2, regimeAlimentaire.getDescription());
            pstm.setInt(3, regimeAlimentaire.getObjectifCalorique());
            pstm.setBoolean(4, regimeAlimentaire.isActif());
            pstm.setInt(5, regimeAlimentaire.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le regime alimentaire.", exception);
        }
    }

    @Override
    public void delete(RegimeAlimentaire regimeAlimentaire) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, regimeAlimentaire.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le regime alimentaire.", exception);
        }
    }

    private RegimeAlimentaire mapResultSet(ResultSet rs) throws SQLException {
        return new RegimeAlimentaire(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getInt("objectif_calorique"),
                rs.getBoolean("actif")
        );
    }

    private void validate(RegimeAlimentaire regimeAlimentaire) {
        if (regimeAlimentaire == null) {
            throw new IllegalArgumentException("Le regime alimentaire est obligatoire.");
        }

        if (regimeAlimentaire.getNom() == null || regimeAlimentaire.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du regime alimentaire est obligatoire.");
        }

        if (regimeAlimentaire.getObjectifCalorique() <= 0) {
            throw new IllegalArgumentException("L'objectif calorique doit etre superieur a 0.");
        }
    }
}
