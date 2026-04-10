package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.ParticipationDAO;
import tn.esprit.Pidev3A49.models.Participation;

import java.util.List;

public class ParticipationService {

    private final ParticipationDAO participationDAO;

    public ParticipationService() {
        this.participationDAO = new ParticipationDAO();
    }

    public List<Participation> getAll() {
        return participationDAO.getAll();
    }
}
