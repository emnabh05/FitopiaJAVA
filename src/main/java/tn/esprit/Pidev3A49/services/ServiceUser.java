package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.User;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    private final Connection cnx;

    public ServiceUser() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String qry = "SELECT id, email, first_name, last_name FROM " + SchemaInitializer.USER_TABLE + " ORDER BY email ASC";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les utilisateurs.", exception);
        }

        return users;
    }

    public User getById(int id) {
        String qry = "SELECT id, email, first_name, last_name FROM " + SchemaInitializer.USER_TABLE + " WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer l'utilisateur " + id, exception);
        }
        return null;
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name")
        );
    }
}
