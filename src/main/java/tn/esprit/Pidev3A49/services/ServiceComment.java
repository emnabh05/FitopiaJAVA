package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.Comment;
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

public class ServiceComment implements IServices<Comment> {

    private static final String TABLE_NAME = SchemaInitializer.COMMENT_TABLE;

    private final Connection cnx;

    public ServiceComment() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Comment comment) {
        validate(comment);
        String qry = """
                INSERT INTO %s (content, forum_id)
                VALUES (?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, comment.getContent());
            pstm.setInt(2, comment.getForum().getId());
            pstm.executeUpdate();

            try (ResultSet generatedKeys = pstm.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'ajouter le commentaire.", exception);
        }
    }

    @Override
    public List<Comment> getAll() {
        List<Comment> comments = new ArrayList<>();
        String qry = """
                SELECT c.id, c.content, f.id AS forum_id, f.title, f.content AS forum_content
                FROM %s c
                INNER JOIN %s f ON c.forum_id = f.id
                ORDER BY c.id DESC
                """.formatted(TABLE_NAME, SchemaInitializer.FORUM_TABLE);

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                comments.add(mapResultSet(rs));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les commentaires.", exception);
        }

        return comments;
    }

    public List<Comment> getByForumId(int forumId) {
        List<Comment> comments = new ArrayList<>();
        String qry = """
                SELECT c.id, c.content, f.id AS forum_id, f.title, f.content AS forum_content
                FROM %s c
                INNER JOIN %s f ON c.forum_id = f.id
                WHERE f.id = ?
                ORDER BY c.id DESC
                """.formatted(TABLE_NAME, SchemaInitializer.FORUM_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, forumId);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSet(rs));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les commentaires du forum " + forumId + ".", exception);
        }

        return comments;
    }

    @Override
    public Comment getById(int id) {
        String qry = """
                SELECT c.id, c.content, f.id AS forum_id, f.title, f.content AS forum_content
                FROM %s c
                INNER JOIN %s f ON c.forum_id = f.id
                WHERE c.id = ?
                """.formatted(TABLE_NAME, SchemaInitializer.FORUM_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer le commentaire avec l'id " + id, exception);
        }

        return null;
    }

    @Override
    public void update(Comment comment) {
        validate(comment);
        String qry = """
                UPDATE %s
                SET content = ?, forum_id = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, comment.getContent());
            pstm.setInt(2, comment.getForum().getId());
            pstm.setInt(3, comment.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le commentaire.", exception);
        }
    }

    @Override
    public void delete(Comment comment) {
        String qry = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, comment.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le commentaire.", exception);
        }
    }

    private Comment mapResultSet(ResultSet rs) throws SQLException {
        Forum forum = new Forum(
                rs.getInt("forum_id"),
                rs.getString("title"),
                rs.getString("forum_content")
        );
        return new Comment(
                rs.getInt("id"),
                rs.getString("content"),
                forum
        );
    }

    private void validate(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Le commentaire est obligatoire.");
        }
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("Le contenu du commentaire est obligatoire.");
        }
        if (comment.getForum() == null || comment.getForum().getId() <= 0) {
            throw new IllegalArgumentException("Le forum du commentaire est obligatoire.");
        }
    }
}
