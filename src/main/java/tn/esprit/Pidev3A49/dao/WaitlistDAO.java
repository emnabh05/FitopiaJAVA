package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.WaitlistEntry;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitlistDAO {

    private final Connection connection;

    public WaitlistDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(WaitlistEntry entry) {
        String sql = "INSERT INTO waitlist_entry "
                + "(id_event, email, nom, status, position, token, invited_at, expires_at, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("[WaitlistDAO] SQL INSERT = " + sql);
            System.out.println("[WaitlistDAO] params id_event=" + entry.getIdEvent()
                    + ", email=" + entry.getEmailParticipant()
                    + ", nom=" + entry.getNomParticipant()
                    + ", status=" + entry.getStatus()
                    + ", position=" + entry.getPosition());
            fillStatement(statement, entry);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune entree waitlist inseree.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("[WaitlistDAO] INSERT OK id=" + entry.getId());
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL waitlist: " + e.getMessage(), e);
        }
    }

    public boolean existsActiveByEventAndEmail(int idEvent, String emailParticipant) {
        String sql = "SELECT 1 FROM waitlist_entry "
                + "WHERE id_event = ? AND LOWER(email) = LOWER(?) "
                + "AND UPPER(status) IN ('EN_ATTENTE', 'INVITE', 'WAITING', 'INVITED') LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[WaitlistDAO] SQL existsActive = " + sql);
            System.out.println("[WaitlistDAO] params id_event=" + idEvent + ", email=" + emailParticipant);
            statement.setInt(1, idEvent);
            statement.setString(2, emailParticipant);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                System.out.println("[WaitlistDAO] existsActive result = " + exists);
                return exists;
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL verification waitlist: " + e.getMessage(), e);
        }
    }

    public List<WaitlistEntry> findByEvent(int idEvent) {
        String sql = "SELECT id, id_event, nom, email, status, position, created_at, token, invited_at, expires_at "
                + "FROM waitlist_entry WHERE id_event = ? ORDER BY created_at ASC, id ASC";
        List<WaitlistEntry> entries = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(mapEntry(resultSet));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL chargement waitlist: " + e.getMessage(), e);
        }

        return entries;
    }

    public List<WaitlistEntry> getWaitlistForEvent(int idEvent) {
        return findByEvent(idEvent);
    }

    public WaitlistEntry findFirstWaitingByEvent(int idEvent) {
        String sql = "SELECT id, id_event, nom, email, status, position, created_at, token, invited_at, expires_at "
                + "FROM waitlist_entry WHERE id_event = ? AND UPPER(status) IN ('EN_ATTENTE', 'WAITING') "
                + "ORDER BY created_at ASC, id ASC LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEvent);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapEntry(resultSet);
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL priorite waitlist: " + e.getMessage(), e);
        }

        return null;
    }

    public WaitlistEntry getFirstWaitingForEvent(int idEvent) {
        return findFirstWaitingByEvent(idEvent);
    }

    public List<Integer> findActiveEventIdsByEmail(String emailParticipant) {
        String sql = "SELECT id_event FROM waitlist_entry "
                + "WHERE LOWER(email) = LOWER(?) AND UPPER(status) IN ('EN_ATTENTE', 'INVITE', 'WAITING', 'INVITED')";
        List<Integer> eventIds = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    eventIds.add(resultSet.getInt("id_event"));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL inscriptions waitlist: " + e.getMessage(), e);
        }

        return eventIds;
    }

    public Map<Integer, Integer> countActiveByEvent() {
        String sql = "SELECT id_event, COUNT(*) AS total FROM waitlist_entry "
                + "WHERE UPPER(status) IN ('EN_ATTENTE', 'INVITE', 'WAITING', 'INVITED') GROUP BY id_event";
        Map<Integer, Integer> counts = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counts.put(resultSet.getInt("id_event"), resultSet.getInt("total"));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL comptage waitlist: " + e.getMessage(), e);
        }

        return counts;
    }

    public void markInvited(WaitlistEntry entry, LocalDateTime invitedAt, LocalDateTime expiresAt) {
        String sql = "UPDATE waitlist_entry SET status = 'INVITE', invited_at = ?, expires_at = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(invitedAt));
            statement.setTimestamp(2, expiresAt == null ? null : Timestamp.valueOf(expiresAt));
            statement.setInt(3, entry.getId());
            statement.executeUpdate();
            entry.setStatus("INVITE");
            entry.setInvitedAt(invitedAt);
            entry.setExpiresAt(expiresAt);
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL promotion waitlist: " + e.getMessage(), e);
        }
    }

    public void markPromoted(int idWaitlistEntry) {
        String sql = "UPDATE waitlist_entry SET status = 'PROMU' WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[WaitlistDAO] SQL markPromoted = " + sql);
            System.out.println("[WaitlistDAO] params id=" + idWaitlistEntry);
            statement.setInt(1, idWaitlistEntry);
            statement.executeUpdate();
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL promotion definitive waitlist: " + e.getMessage(), e);
        }
    }

    public void deleteById(int idWaitlistEntry) {
        String sql = "DELETE FROM waitlist_entry WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[WaitlistDAO] SQL deleteById = " + sql);
            System.out.println("[WaitlistDAO] params id=" + idWaitlistEntry);
            statement.setInt(1, idWaitlistEntry);
            statement.executeUpdate();
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL suppression waitlist: " + e.getMessage(), e);
        }
    }

    public void removeFromWaitlist(int idWaitlistEntry) {
        deleteById(idWaitlistEntry);
    }

    public int nextPositionForEvent(int idEvent) {
        String sql = "SELECT COALESCE(MAX(position), 0) + 1 AS next_position FROM waitlist_entry WHERE id_event = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[WaitlistDAO] SQL nextPosition = " + sql);
            System.out.println("[WaitlistDAO] params id_event=" + idEvent);
            statement.setInt(1, idEvent);
            try (ResultSet resultSet = statement.executeQuery()) {
                int next = resultSet.next() ? resultSet.getInt("next_position") : 1;
                System.out.println("[WaitlistDAO] nextPosition result = " + next);
                return next;
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Erreur SQL position waitlist: " + e.getMessage(), e);
        }
    }

    private void fillStatement(PreparedStatement statement, WaitlistEntry entry) throws SQLException {
        statement.setInt(1, entry.getIdEvent());
        statement.setString(2, entry.getEmailParticipant());
        statement.setString(3, entry.getNomParticipant());
        statement.setString(4, entry.getStatus());
        statement.setInt(5, entry.getPosition());
        statement.setString(6, entry.getToken());
        statement.setTimestamp(7, entry.getInvitedAt() == null ? null : Timestamp.valueOf(entry.getInvitedAt()));
        statement.setTimestamp(8, entry.getExpiresAt() == null ? null : Timestamp.valueOf(entry.getExpiresAt()));
        statement.setTimestamp(9, Timestamp.valueOf(entry.getCreatedAt()));
    }

    private WaitlistEntry mapEntry(ResultSet resultSet) throws SQLException {
        return new WaitlistEntry(
                resultSet.getInt("id"),
                resultSet.getInt("id_event"),
                resultSet.getString("nom"),
                resultSet.getString("email"),
                resultSet.getString("status"),
                resultSet.getInt("position"),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                resultSet.getString("token"),
                toLocalDateTime(resultSet.getTimestamp("invited_at")),
                toLocalDateTime(resultSet.getTimestamp("expires_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void logSqlError(String sql, SQLException e) {
        System.err.println("[WaitlistDAO] ERREUR SQL waitlist");
        System.err.println("[WaitlistDAO] SQL = " + sql);
        System.err.println("[WaitlistDAO] Message = " + e.getMessage());
        System.err.println("[WaitlistDAO] SQLState = " + e.getSQLState());
        System.err.println("[WaitlistDAO] Code erreur = " + e.getErrorCode());
        e.printStackTrace();
    }
}
