package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ParticipationDAO;
import tn.esprit.Pidev3A49.models.Participation;

import java.util.List;

public class ParticipationService {

    private final ParticipationDAO participationDAO = new ParticipationDAO();

    public void add(Participation participation) {
        participationDAO.add(participation);
    }

    public void update(Participation participation) {
        participationDAO.update(participation);
    }

    public void delete(int idParticipation) {
        participationDAO.deleteById(idParticipation);
    }

    public Participation getById(int idParticipation) {
        return participationDAO.findById(idParticipation);
    }

    public List<Participation> getAll() {
        return participationDAO.findAll();
    }

    public boolean existsForEventAndEmail(int idEvent, String emailParticipant) {
        return participationDAO.existsByEventAndEmail(idEvent, emailParticipant);
    }
}
