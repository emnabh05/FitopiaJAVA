package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.Models.Forum;
import tn.esprit.Pidev3A49.interfaces.IServices;
import tn.esprit.Pidev3A49.test.UserSession;
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
    private static final String LIKE_TABLE = SchemaInitializer.FORUM_LIKE_TABLE;
    private static final String REPOST_TABLE = SchemaInitializer.FORUM_REPOST_TABLE;
    private static final String USER_TABLE = "fitopia_users";

    private final Connection cnx;

    public ServiceForum() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Forum forum) {
        validate(forum);
        String qry = """
                INSERT INTO %s (user_id, title, content, image_path)
                VALUES (?, ?, ?, ?)
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setInt(1, forum.getUserId());
            pstm.setString(2, forum.getTitle());
            pstm.setString(3, forum.getContent());
            if (forum.getImagePath() == null || forum.getImagePath().isBlank()) {
                pstm.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstm.setString(4, forum.getImagePath());
            }
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
        Integer currentUserId = getCurrentUserId();
        String qry = """
                SELECT f.id,
                       f.user_id,
                       f.title,
                       f.content,
                       f.image_path,
                       u.email AS author_email,
                       COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), ''),
                                NULLIF(u.username, ''),
                                NULLIF(u.email, ''),
                                'Utilisateur inconnu') AS author_name,
                       (SELECT COUNT(*) FROM %s fl WHERE fl.forum_id = f.id) AS like_count,
                       (SELECT COUNT(*) FROM %s fr WHERE fr.forum_id = f.id) AS repost_count,
                       EXISTS(SELECT 1 FROM %s fl2 WHERE fl2.forum_id = f.id AND fl2.user_id = ?) AS liked_by_current_user,
                       EXISTS(SELECT 1 FROM %s fr2 WHERE fr2.forum_id = f.id AND fr2.user_id = ?) AS reposted_by_current_user
                FROM %s f
                LEFT JOIN %s u ON u.id = f.user_id
                ORDER BY f.id DESC
                """.formatted(LIKE_TABLE, REPOST_TABLE, LIKE_TABLE, REPOST_TABLE, TABLE_NAME, USER_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, currentUserId == null ? -1 : currentUserId);
            pstm.setInt(2, currentUserId == null ? -1 : currentUserId);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    forums.add(mapResultSet(rs));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les forums.", exception);
        }

        return forums;
    }

    @Override
    public Forum getById(int id) {
        Integer currentUserId = getCurrentUserId();
        String qry = """
                SELECT f.id,
                       f.user_id,
                       f.title,
                       f.content,
                       f.image_path,
                       u.email AS author_email,
                       COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), ''),
                                NULLIF(u.username, ''),
                                NULLIF(u.email, ''),
                                'Utilisateur inconnu') AS author_name,
                       (SELECT COUNT(*) FROM %s fl WHERE fl.forum_id = f.id) AS like_count,
                       (SELECT COUNT(*) FROM %s fr WHERE fr.forum_id = f.id) AS repost_count,
                       EXISTS(SELECT 1 FROM %s fl2 WHERE fl2.forum_id = f.id AND fl2.user_id = ?) AS liked_by_current_user,
                       EXISTS(SELECT 1 FROM %s fr2 WHERE fr2.forum_id = f.id AND fr2.user_id = ?) AS reposted_by_current_user
                FROM %s f
                LEFT JOIN %s u ON u.id = f.user_id
                WHERE f.id = ?
                """.formatted(LIKE_TABLE, REPOST_TABLE, LIKE_TABLE, REPOST_TABLE, TABLE_NAME, USER_TABLE);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, currentUserId == null ? -1 : currentUserId);
            pstm.setInt(2, currentUserId == null ? -1 : currentUserId);
            pstm.setInt(3, id);

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
                SET user_id = ?, title = ?, content = ?, image_path = ?
                WHERE id = ?
                """.formatted(TABLE_NAME);

        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, forum.getUserId());
            pstm.setString(2, forum.getTitle());
            pstm.setString(3, forum.getContent());
            if (forum.getImagePath() == null || forum.getImagePath().isBlank()) {
                pstm.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstm.setString(4, forum.getImagePath());
            }
            pstm.setInt(5, forum.getId());
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de modifier le forum.", exception);
        }
    }

    @Override
    public void delete(Forum forum) {
        String deleteLikes = "DELETE FROM " + LIKE_TABLE + " WHERE forum_id = ?";
        String deleteReposts = "DELETE FROM " + REPOST_TABLE + " WHERE forum_id = ?";
        String deleteForum = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (PreparedStatement likes = cnx.prepareStatement(deleteLikes);
             PreparedStatement reposts = cnx.prepareStatement(deleteReposts);
             PreparedStatement forumDelete = cnx.prepareStatement(deleteForum)) {
            likes.setInt(1, forum.getId());
            likes.executeUpdate();

            reposts.setInt(1, forum.getId());
            reposts.executeUpdate();

            forumDelete.setInt(1, forum.getId());
            forumDelete.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer le forum.", exception);
        }
    }

    public boolean toggleLike(int forumId, int userId) {
        validateUserAction(forumId, userId);
        String existsQry = "SELECT 1 FROM " + LIKE_TABLE + " WHERE forum_id = ? AND user_id = ? LIMIT 1";

        try (PreparedStatement exists = cnx.prepareStatement(existsQry)) {
            exists.setInt(1, forumId);
            exists.setInt(2, userId);
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    deleteInteraction(LIKE_TABLE, forumId, userId);
                    return false;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de basculer le like.", exception);
        }

        insertInteraction(LIKE_TABLE, forumId, userId);
        return true;
    }

    public boolean toggleRepost(int forumId, int userId) {
        validateUserAction(forumId, userId);
        String existsQry = "SELECT 1 FROM " + REPOST_TABLE + " WHERE forum_id = ? AND user_id = ? LIMIT 1";

        try (PreparedStatement exists = cnx.prepareStatement(existsQry)) {
            exists.setInt(1, forumId);
            exists.setInt(2, userId);
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    deleteInteraction(REPOST_TABLE, forumId, userId);
                    return false;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de basculer le repost.", exception);
        }

        insertInteraction(REPOST_TABLE, forumId, userId);
        return true;
    }

    private void insertInteraction(String tableName, int forumId, int userId) {
        String qry = "INSERT INTO " + tableName + " (forum_id, user_id) VALUES (?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, forumId);
            pstm.setInt(2, userId);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer l'interaction.", exception);
        }
    }

    private void deleteInteraction(String tableName, int forumId, int userId) {
        String qry = "DELETE FROM " + tableName + " WHERE forum_id = ? AND user_id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, forumId);
            pstm.setInt(2, userId);
            pstm.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de supprimer l'interaction.", exception);
        }
    }

    private Forum mapResultSet(ResultSet rs) throws SQLException {
        Forum forum = new Forum(
                rs.getInt("id"),
                readNullableInt(rs, "user_id"),
                rs.getString("title"),
                rs.getString("content")
        );
        forum.setImagePath(rs.getString("image_path"));
        forum.setAuthorName(rs.getString("author_name"));
        forum.setAuthorEmail(rs.getString("author_email"));
        forum.setLikeCount(rs.getInt("like_count"));
        forum.setRepostCount(rs.getInt("repost_count"));
        forum.setLikedByCurrentUser(rs.getBoolean("liked_by_current_user"));
        forum.setRepostedByCurrentUser(rs.getBoolean("reposted_by_current_user"));
        return forum;
    }

    private void validate(Forum forum) {
        if (forum == null) {
            throw new IllegalArgumentException("Le forum est obligatoire.");
        }
        if (forum.getUserId() == null || forum.getUserId() <= 0) {
            throw new IllegalArgumentException("Le proprietaire du forum est obligatoire.");
        }
        if (forum.getTitle() == null || forum.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du forum est obligatoire.");
        }
        if (forum.getContent() == null || forum.getContent().isBlank()) {
            throw new IllegalArgumentException("Le contenu du forum est obligatoire.");
        }
    }

    private void validateUserAction(int forumId, int userId) {
        if (forumId <= 0) {
            throw new IllegalArgumentException("Forum introuvable.");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
    }

    private Integer getCurrentUserId() {
        FitopiaUser currentUser = UserSession.getCurrentUser();
        return currentUser == null ? null : currentUser.getId();
    }

    private Integer readNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
