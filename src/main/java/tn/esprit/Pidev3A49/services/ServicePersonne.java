package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne implements IServices<Personne> {
    private static final String TABLE_NAME = "personne";
    private final Connection cnx;

    public ServicePersonne(){
        cnx = MyDataBase.getInstance().getCnx();
    }

    public boolean isAvailable() {
        return cnx != null;
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException("Connexion a la base de donnees indisponible.");
        }
    }
    @Override
    public void add(Personne personne) {
        ensureConnection();
        String qry ="INSERT INTO `" + TABLE_NAME + "`(`nom`, `prenom`, `age`) VALUES (?,?,?)";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {

            pstm.setString(1,personne.getNom());
            pstm.setString(2,personne.getPrenom());
            pstm.setInt(3,personne.getAge());

            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la personne : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Personne> getAll() {
        ensureConnection();
        List<Personne> personnes = new ArrayList<>();
        String qry ="SELECT `id`, `nom`, `prenom`, `age` FROM `" + TABLE_NAME + "`";
        try (Statement stm  = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {

            while (rs.next()){
                Personne p = new Personne();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setAge(rs.getInt("age"));
                p.setRole("Patient");
                personnes.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la lecture des personnes : " + e.getMessage(), e);
        }
        return personnes;
    }

    @Override
    public void update(Personne personne) {
        ensureConnection();
        String qry = "UPDATE `" + TABLE_NAME + "` SET `nom` = ?, `prenom` = ?, `age` = ? WHERE `id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, personne.getNom());
            pstm.setString(2, personne.getPrenom());
            pstm.setInt(3, personne.getAge());
            pstm.setInt(4, personne.getId());

            int rows = pstm.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aucune ligne modifiee pour l'id " + personne.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la personne : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Personne personne) {
        ensureConnection();
        String qry = "DELETE FROM `" + TABLE_NAME + "` WHERE `id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, personne.getId());

            int rows = pstm.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aucune ligne supprimee pour l'id " + personne.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la personne : " + e.getMessage(), e);
        }
    }

    public Personne getById(int id) {
        ensureConnection();
        String qry = "SELECT `id`, `nom`, `prenom`, `age` FROM `" + TABLE_NAME + "` WHERE `id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return new Personne(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getInt("age")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la personne : " + e.getMessage(), e);
        }
        return null;
    }
}
