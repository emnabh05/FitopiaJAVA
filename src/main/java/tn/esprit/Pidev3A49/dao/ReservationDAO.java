package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.models.Reservation;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

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

    private void logSqlError(String sql, SQLException e) {
        System.err.println("Erreur SQL lors de l'ajout d'une reservation.");
        System.err.println("SQL : " + sql);
        System.err.println("Message : " + e.getMessage());
        System.err.println("SQLState : " + e.getSQLState());
        System.err.println("Code erreur : " + e.getErrorCode());
        e.printStackTrace();
    }
}
