package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServicePersonne implements IServices<Personne> {
    private Connection cnx;

    public ServicePersonne() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Personne personne) {
        String qry = "INSERT INTO `personne`( `nom`, `prenom`, `age`) VALUES (?,?,?)";

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);

            pstm.setString(1, personne.getNom());
            pstm.setString(2, personne.getPrenom());
            pstm.setInt(3, personne.getAge());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Personne> getAll() {
        List<Personne> personnes = new ArrayList<>();
        String qry = "SELECT * FROM `personne`";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Personne p = new Personne();
                p.setId(rs.getInt(1));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString(3));
                p.setAge(rs.getInt("age"));

                personnes.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return personnes;
    }
}
