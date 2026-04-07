package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.TypeRepas;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepas implements IServices<Repas> {

    private final Connection cnx;

    public ServiceRepas() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Repas repas) {
        validate(repas);
        String qry = """
                INSERT INTO repas (nom, description, calories, type_repas, date_repas, regime_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, repas.getNom());
            pstm.setString(2, repas.getDescription());
            pstm.setInt(3, repas.getCalories());
            pstm.setString(4, repas.getTypeRepas().name());
            setDateValue(pstm, 5, repas);
            setRegimeValue(pstm, 6, repas.getRegimeId());
            pstm.executeUpdate();

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    repas.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le repas.", exception);
        }
    }

    @Override
    public List<Repas> getAll() {
        List<Repas> repasList = new ArrayList<>();
        String qry = "SELECT * FROM repas ORDER BY id DESC";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                repasList.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les repas.", exception);
        }

        return repasList;
    }

    @Override
    public Repas getById(int id) {
        String qry = "SELECT * FROM repas WHERE id = ?";

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
                UPDATE repas
                SET nom = ?, description = ?, calories = ?, type_repas = ?, date_repas = ?, regime_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, repas.getNom());
            pstm.setString(2, repas.getDescription());
            pstm.setInt(3, repas.getCalories());
            pstm.setString(4, repas.getTypeRepas().name());
            setDateValue(pstm, 5, repas);
            setRegimeValue(pstm, 6, repas.getRegimeId());
            pstm.setInt(7, repas.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le repas.", exception);
        }
    }

    @Override
    public void delete(Repas repas) {
        String qry = "DELETE FROM repas WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, repas.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le repas.", exception);
        }
    }

    public List<Repas> getByRegimeId(int regimeId) {
        List<Repas> repasList = new ArrayList<>();
        String qry = "SELECT * FROM repas WHERE regime_id = ? ORDER BY id DESC";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, regimeId);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    repasList.add(mapResultSet(rs));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les repas du regime " + regimeId, exception);
        }

        return repasList;
    }

    private Repas mapResultSet(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("date_repas");
        int regimeId = rs.getInt("regime_id");

        return new Repas(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getInt("calories"),
                TypeRepas.valueOf(rs.getString("type_repas")),
                sqlDate != null ? sqlDate.toLocalDate() : null,
                rs.wasNull() ? null : regimeId
        );
    }

    private void validate(Repas repas) {
        if (repas == null) {
            throw new IllegalArgumentException("Le repas est obligatoire.");
        }

        if (repas.getNom() == null || repas.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du repas est obligatoire.");
        }

        if (repas.getCalories() <= 0) {
            throw new IllegalArgumentException("Les calories doivent etre superieures a 0.");
        }

        if (repas.getTypeRepas() == null) {
            throw new IllegalArgumentException("Le type de repas est obligatoire.");
        }
    }

    private void setDateValue(PreparedStatement pstm, int parameterIndex, Repas repas) throws SQLException {
        if (repas.getDateRepas() == null) {
            pstm.setNull(parameterIndex, Types.DATE);
            return;
        }

        pstm.setDate(parameterIndex, Date.valueOf(repas.getDateRepas()));
    }

    private void setRegimeValue(PreparedStatement pstm, int parameterIndex, Integer regimeId) throws SQLException {
        if (regimeId == null) {
            pstm.setNull(parameterIndex, Types.INTEGER);
            return;
        }

        pstm.setInt(parameterIndex, regimeId);
    }
}
