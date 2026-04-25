package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.LoyaltyStatus;
import tn.esprit.Pidev3A49.models.ReservationHistoryItem;
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

public class ReservationDAO {

    private final Connection connection;

    public ReservationDAO() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    public void add(Reservation reservation) {
        String sql = "INSERT INTO reservation "
                + "(id_event, nom_participant, email_participant, date_reservation, montant, statut, transaction_id, qr_token, checked_in_at, used_at, qr_generated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reservation.getIdEvent());
            statement.setString(2, reservation.getNomParticipant());
            statement.setString(3, reservation.getEmailParticipant());
            statement.setTimestamp(4, Timestamp.valueOf(reservation.getDateReservation()));
            statement.setDouble(5, reservation.getMontant());
            statement.setString(6, reservation.getStatut());
            statement.setString(7, reservation.getTransactionId());
            statement.setString(8, reservation.getQrToken());
            statement.setTimestamp(9, reservation.getCheckedInAt() == null ? null : Timestamp.valueOf(reservation.getCheckedInAt()));
            statement.setTimestamp(10, reservation.getUsedAt() == null ? null : Timestamp.valueOf(reservation.getUsedAt()));
            statement.setTimestamp(11, reservation.getQrGeneratedAt() == null ? null : Timestamp.valueOf(reservation.getQrGeneratedAt()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune reservation inseree.");
            }

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'ajout de la reservation.", e);
        }
    }

    public List<Reservation> findByEmail(String emailParticipant) {
        String sql = "SELECT id, id_event, nom_participant, email_participant, date_reservation, montant, statut, "
                + "transaction_id, qr_token, checked_in_at, used_at, qr_generated_at "
                + "FROM reservation WHERE LOWER(email_participant) = LOWER(?) ORDER BY date_reservation DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement des reservations.", e);
        }

        return reservations;
    }

    public List<ReservationHistoryItem> findHistoryByEmail(String emailParticipant) {
        String sql = "SELECT r.*, e.titre, e.date_event, e.lieu, e.prix_event, e.image_event, e.description, e.type_event, e.capacite, e.created_at, e.is_premium "
                + "FROM reservation r "
                + "JOIN events e ON r.id_event = e.id_event "
                + "WHERE r.email_participant = ? "
                + "ORDER BY r.date_reservation DESC";
        List<ReservationHistoryItem> items = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);
            System.out.println("[ReservationDAO] Historique email_participant = " + emailParticipant);
            System.out.println("[ReservationDAO] SQL = " + sql);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Reservation reservation = mapReservation(resultSet);
                    Event event = mapEvent(resultSet);
                    items.add(new ReservationHistoryItem(reservation, event));
                }
            }
            System.out.println("[ReservationDAO] Nombre de reservations trouvees = " + items.size());
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement de l'historique des reservations.", e);
        }

        return items;
    }

    public void cancelReservation(int reservationId) {
        String sql = "UPDATE reservation SET statut = 'Annulee' WHERE id = ? AND LOWER(statut) IN ('confirmee', 'payee', 'confirmed', 'paid')";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'annulation de la reservation.", e);
        }
    }

    public int cancelReservationIfCancelable(int reservationId) {
        String sql = "UPDATE reservation SET statut = 'Annulee' "
                + "WHERE id = ? AND LOWER(TRIM(statut)) IN ('confirmee', 'payee', 'confirmed', 'paid')";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de l'annulation de la reservation.", e);
        }
    }

    public Reservation findById(int reservationId) {
        String sql = "SELECT id, id_event, nom_participant, email_participant, date_reservation, montant, statut, "
                + "transaction_id, qr_token, checked_in_at, used_at, qr_generated_at "
                + "FROM reservation WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapReservation(resultSet);
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la recherche de la reservation.", e);
        }

        return null;
    }

    public boolean existsByQrToken(String qrToken) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE qr_token = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, qrToken);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la verification du token QR.", e);
        }
    }

    public int countValidReservationsByEvent(int idEvent) {
        String sql = "SELECT COUNT(*) FROM reservation "
                + "WHERE id_event = ? AND LOWER(TRIM(statut)) IN ('confirmee', 'utilisee', 'confirmed', 'used')";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[ReservationDAO] SQL countValidReservationsByEvent = " + sql);
            System.out.println("[ReservationDAO] params id_event=" + idEvent);
            statement.setInt(1, idEvent);
            try (ResultSet resultSet = statement.executeQuery()) {
                int count = resultSet.next() ? resultSet.getInt(1) : 0;
                System.out.println("[ReservationDAO] countValidReservationsByEvent result = " + count);
                return count;
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du comptage des reservations valides.", e);
        }
    }

    public Map<Integer, Long> countValidReservationsByEvent() {
        String sql = "SELECT id_event, COUNT(*) AS total FROM reservation "
                + "WHERE LOWER(TRIM(statut)) IN ('confirmee', 'utilisee', 'confirmed', 'used') GROUP BY id_event";
        Map<Integer, Long> counts = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                counts.put(resultSet.getInt("id_event"), resultSet.getLong("total"));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du comptage des reservations valides.", e);
        }

        return counts;
    }

    public boolean existsValidReservationByEventAndEmail(int idEvent, String emailParticipant) {
        String sql = "SELECT 1 FROM reservation "
                + "WHERE id_event = ? AND LOWER(email_participant) = LOWER(?) "
                + "AND LOWER(TRIM(statut)) IN ('confirmee', 'utilisee', 'confirmed', 'used') LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            System.out.println("[ReservationDAO] SQL existsValidReservationByEventAndEmail = " + sql);
            System.out.println("[ReservationDAO] params id_event=" + idEvent + ", email=" + emailParticipant);
            statement.setInt(1, idEvent);
            statement.setString(2, emailParticipant);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                System.out.println("[ReservationDAO] existsValidReservation result = " + exists);
                return exists;
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors de la verification de reservation valide.", e);
        }
    }

    public LoyaltyStatus findLoyaltyStatusByEmail(String emailParticipant) {
        String sql = "SELECT email_participant, MAX(nom_participant) AS nom_participant, COUNT(*) AS valid_count, "
                + "COALESCE(SUM(montant), 0) AS total_spent, MAX(date_reservation) AS last_purchase "
                + "FROM reservation "
                + "WHERE LOWER(email_participant) = LOWER(?) "
                + "AND LOWER(TRIM(statut)) IN ('confirmee', 'utilisee', 'confirmed', 'used') "
                + "GROUP BY email_participant";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailParticipant);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapLoyaltyStatus(resultSet);
                }
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement du statut fidelite.", e);
        }

        return new LoyaltyStatus(emailParticipant, "", 0, 0.0, null);
    }

    public List<LoyaltyStatus> findAllLoyaltyStatuses() {
        String sql = "SELECT email_participant, MAX(nom_participant) AS nom_participant, COUNT(*) AS valid_count, "
                + "COALESCE(SUM(montant), 0) AS total_spent, MAX(date_reservation) AS last_purchase "
                + "FROM reservation "
                + "WHERE LOWER(TRIM(statut)) IN ('confirmee', 'utilisee', 'confirmed', 'used') "
                + "GROUP BY email_participant "
                + "ORDER BY valid_count DESC, total_spent DESC, last_purchase DESC";
        List<LoyaltyStatus> items = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(mapLoyaltyStatus(resultSet));
            }
        } catch (SQLException e) {
            logSqlError(sql, e);
            throw new RuntimeException("Echec SQL lors du chargement du tableau fidelite.", e);
        }

        return items;
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        return new Reservation(
                resultSet.getInt("id"),
                resultSet.getInt("id_event"),
                resultSet.getString("nom_participant"),
                resultSet.getString("email_participant"),
                toLocalDateTime(resultSet.getTimestamp("date_reservation")),
                resultSet.getDouble("montant"),
                resultSet.getString("statut"),
                resultSet.getString("transaction_id"),
                resultSet.getString("qr_token"),
                toLocalDateTime(resultSet.getTimestamp("checked_in_at")),
                toLocalDateTime(resultSet.getTimestamp("used_at")),
                toLocalDateTime(resultSet.getTimestamp("qr_generated_at"))
        );
    }

    private Event mapEvent(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        java.sql.Date dateEvent = resultSet.getDate("date_event");
        return new Event(
                resultSet.getInt("id_event"),
                resultSet.getString("titre"),
                resultSet.getString("description"),
                dateEvent == null ? null : dateEvent.toLocalDate(),
                resultSet.getString("lieu"),
                resultSet.getInt("capacite"),
                resultSet.getString("type_event"),
                resultSet.getString("image_event"),
                resultSet.getDouble("prix_event"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                resultSet.getBoolean("is_premium")
        );
    }

    private LoyaltyStatus mapLoyaltyStatus(ResultSet resultSet) throws SQLException {
        return new LoyaltyStatus(
                resultSet.getString("email_participant"),
                resultSet.getString("nom_participant"),
                resultSet.getInt("valid_count"),
                resultSet.getDouble("total_spent"),
                toLocalDateTime(resultSet.getTimestamp("last_purchase"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL lors de l'ajout d'une reservation.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
