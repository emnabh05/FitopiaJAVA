package tn.esprit.Pidev3A49.service;

import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.RecommendationResult;
import tn.esprit.Pidev3A49.models.Reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class RecommendationService {

    private final ReservationService reservationService = new ReservationService();
    private final FavoriteService favoriteService = new FavoriteService();

    public List<RecommendationResult> recommendEvents(
            String emailParticipant,
            boolean vip,
            List<Event> allEvents,
            Set<Integer> favoriteEventIds,
            Set<Integer> completeEventIds,
            int limit
    ) {
        validateEmail(emailParticipant);

        List<Reservation> reservations = reservationService.getByEmail(emailParticipant);
        Set<Integer> reservedEventIds = reservations.stream()
                .filter(reservation -> reservation.getStatut() == null
                        || !"annulee".equalsIgnoreCase(reservation.getStatut().trim()))
                .map(Reservation::getIdEvent)
                .collect(Collectors.toSet());

        Set<String> reservedTypes = reservations.stream()
                .filter(reservation -> reservation.getStatut() == null
                        || !"annulee".equalsIgnoreCase(reservation.getStatut().trim()))
                .map(Reservation::getIdEvent)
                .collect(Collectors.toSet())
                .stream()
                .map(id -> findTypeByEventId(allEvents, id))
                .filter(type -> type != null && !type.isBlank())
                .collect(Collectors.toSet());

        Set<Integer> effectiveFavoriteIds = favoriteEventIds == null || favoriteEventIds.isEmpty()
                ? favoriteService.getFavoriteEventIdsByEmail(emailParticipant)
                : favoriteEventIds;

        Set<String> favoriteTypes = effectiveFavoriteIds.stream()
                .map(id -> findTypeByEventId(allEvents, id))
                .filter(type -> type != null && !type.isBlank())
                .collect(Collectors.toSet());

        Set<Integer> excludedIds = new HashSet<>(reservedEventIds);
        if (completeEventIds != null) {
            excludedIds.addAll(completeEventIds);
        }

        return allEvents.stream()
                .filter(event -> !excludedIds.contains(event.getIdEvent()))
                .map(event -> scoreEvent(event, reservedTypes, favoriteTypes, vip))
                .filter(result -> result.getScore() > 0)
                .sorted(Comparator.comparingInt(RecommendationResult::getScore).reversed()
                        .thenComparing(result -> result.getEvent().getDateEvent(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(result -> result.getEvent().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private RecommendationResult scoreEvent(Event event, Set<String> reservedTypes, Set<String> favoriteTypes, boolean vip) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        String eventType = normalize(event.getTypeEvent());
        if (reservedTypes.contains(eventType)) {
            score += 3;
            reasons.add("+3 meme type qu'une reservation");
        }
        if (favoriteTypes.contains(eventType)) {
            score += 2;
            reasons.add("+2 meme type qu'un favori");
        }
        if (vip && event.isPremium()) {
            score += 2;
            reasons.add("+2 VIP premium");
        }
        if (isRecent(event)) {
            score += 1;
            reasons.add("+1 evenement recent");
        }

        return new RecommendationResult(event, score, String.join(" • ", reasons));
    }

    private boolean isRecent(Event event) {
        LocalDateTime createdAt = event.getCreatedAt();
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    private String findTypeByEventId(List<Event> events, int idEvent) {
        return events.stream()
                .filter(event -> event.getIdEvent() == idEvent)
                .map(Event::getTypeEvent)
                .map(this::normalize)
                .findFirst()
                .orElse(null);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String emailParticipant) {
        if (emailParticipant == null || emailParticipant.trim().isEmpty() || !emailParticipant.contains("@")) {
            throw new IllegalArgumentException("Un email utilisateur valide est obligatoire.");
        }
    }
}
