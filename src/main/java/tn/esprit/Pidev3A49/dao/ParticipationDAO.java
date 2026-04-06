package tn.esprit.Pidev3A49.dao;

import tn.esprit.Pidev3A49.interfaces.CrudRepository;
import tn.esprit.Pidev3A49.models.Participation;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticipationDAO implements CrudRepository<Participation, Integer> {
    private static final String INSERT_SQL = """
            INSERT INTO participation (id_event, nom_participant, email_participant, date_inscription)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE participation
            SET id_event = ?, nom_participant = ?, email_participant = ?, date_inscription = ?
            WHERE id_participation = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM participation WHERE id_participation = ?";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM participation WHERE id_participation = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM participation ORDER BY id_participation DESC";

    private final Connection connection;

    public ParticipationDAO() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Participation participation) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindCommonFields(statement, participation);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participation.setIdParticipation(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Participation participation) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bindCommonFields(statement, participation);
            statement.setInt(5, participation.getIdParticipation());
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteById(Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Participation> findById(Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Participation> findAll() throws SQLException {
        List<Participation> participations = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                participations.add(mapRow(resultSet));
            }
        }
        return participations;
    }

    private void bindCommonFields(PreparedStatement statement, Participation participation) throws SQLException {
        statement.setInt(1, participation.getIdEvent());
        statement.setString(2, participation.getNomParticipant());
        statement.setString(3, participation.getEmailParticipant());
        statement.setTimestamp(4, Timestamp.valueOf(participation.getDateInscription()));
    }

    private Participation mapRow(ResultSet resultSet) throws SQLException {
        Participation participation = new Participation();
        participation.setIdParticipation(resultSet.getInt("id_participation"));
        participation.setIdEvent(resultSet.getInt("id_event"));
        participation.setNomParticipant(resultSet.getString("nom_participant"));
        participation.setEmailParticipant(resultSet.getString("email_participant"));
        participation.setDateInscription(resultSet.getTimestamp("date_inscription").toLocalDateTime());
        return participation;
    }
}
