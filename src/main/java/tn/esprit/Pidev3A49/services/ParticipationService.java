package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.dao.ParticipationDAO;
import tn.esprit.Pidev3A49.models.Participation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ParticipationService {
    private final ParticipationDAO participationDAO;

    public ParticipationService() {
        this.participationDAO = new ParticipationDAO();
    }

    public void add(Participation participation) throws SQLException {
        if (participation.getDateInscription() == null) {
            participation.setDateInscription(LocalDateTime.now());
        }
        participationDAO.add(participation);
    }

    public void update(Participation participation) throws SQLException {
        participationDAO.update(participation);
    }

    public void deleteById(int id) throws SQLException {
        participationDAO.deleteById(id);
    }

    public Optional<Participation> findById(int id) throws SQLException {
        return participationDAO.findById(id);
    }

    public List<Participation> findAll() throws SQLException {
        return participationDAO.findAll();
    }
}
