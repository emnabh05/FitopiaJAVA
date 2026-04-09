package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
