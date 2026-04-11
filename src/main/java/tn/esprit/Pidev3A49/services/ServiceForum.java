package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Forum;
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

public class ServiceForum implements IServices<Forum> {

    private static final String TABLE_NAME = SchemaInitializer.FORUM_TABLE;

    private final Connection cnx;

    public ServiceForum() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Forum forum) {
        validate(forum);
        String qry = """
                INSERT INTO %s (title, content)
                VALUES (?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, forum.getTitle());
            pstm.setString(2, forum.getContent());
            pstm.executeUpdate();

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    forum.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le forum.", exception);
        }
    }

    @Override
    public List<Forum> getAll() {
        List<Forum> forums = new ArrayList<>();
        String qry = "SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                forums.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les forums.", exception);
        }

        return forums;
    }

    @Override
    public Forum getById(int id) {
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le forum avec l'id " + id, exception);
        }

        return null;
    }

    @Override
    public void update(Forum forum) {
        validate(forum);
        String qry = """
                UPDATE %s
                SET title = ?, content = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, forum.getTitle());
            pstm.setString(2, forum.getContent());
            pstm.setInt(3, forum.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le forum.", exception);
        }
    }

    @Override
    public void delete(Forum forum) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, forum.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le forum.", exception);
        }
    }

    private Forum mapResultSet(ResultSet rs) throws SQLException {
        return new Forum(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content")
        );
    }

    private void validate(Forum forum) {
        if (forum == null) {
            throw new IllegalArgumentException("Le forum est obligatoire.");
        }
        if (forum.getTitle() == null || forum.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du forum est obligatoire.");
        }
        if (forum.getContent() == null || forum.getContent().isBlank()) {
            throw new IllegalArgumentException("Le contenu du forum est obligatoire.");
        }
    }
}
