package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.dao.FavoriteDAO;
import tn.esprit.Pidev3A49.models.Favorite;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class FavoriteService {

    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    public void addFavorite(int idEvent, String emailParticipant) {
        validate(idEvent, emailParticipant);
        if (favoriteDAO.exists(idEvent, emailParticipant.trim())) {
            return;
        }
        favoriteDAO.add(new Favorite(idEvent, emailParticipant.trim(), LocalDateTime.now()));
    }

    public void removeFavorite(int idEvent, String emailParticipant) {
        validate(idEvent, emailParticipant);
        favoriteDAO.remove(idEvent, emailParticipant.trim());
    }

    public boolean isFavorite(int idEvent, String emailParticipant) {
        validate(idEvent, emailParticipant);
        return favoriteDAO.exists(idEvent, emailParticipant.trim());
    }

    public Set<Integer> getFavoriteEventIdsByEmail(String emailParticipant) {
        validateEmail(emailParticipant);
        return favoriteDAO.findFavoriteEventIdsByEmail(emailParticipant.trim());
    }

    public Map<Integer, Integer> countFavoritesByEvent() {
        return favoriteDAO.countByEvent();
    }

    public int countFavoritesByEmail(String emailParticipant) {
        validateEmail(emailParticipant);
        return favoriteDAO.countByEmail(emailParticipant.trim());
    }

    private void validate(int idEvent, String emailParticipant) {
        if (idEvent <= 0) {
            throw new IllegalArgumentException("L'evenement est invalide.");
        }
        validateEmail(emailParticipant);
    }

    private void validateEmail(String emailParticipant) {
        if (emailParticipant == null || emailParticipant.trim().isEmpty() || !emailParticipant.contains("@")) {
            throw new IllegalArgumentException("Un email utilisateur valide est obligatoire.");
        }
    }
}
