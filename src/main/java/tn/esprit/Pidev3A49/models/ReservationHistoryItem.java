package tn.esprit.Pidev3A49.models;

public class ReservationHistoryItem {

    private final Reservation reservation;
    private final Event event;

    public ReservationHistoryItem(Reservation reservation, Event event) {
        this.reservation = reservation;
        this.event = event;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Event getEvent() {
        return event;
    }
}
