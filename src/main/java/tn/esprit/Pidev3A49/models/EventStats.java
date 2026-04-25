package tn.esprit.Pidev3A49.models;

public class EventStats {

    private final int idEvent;
    private final String titre;
    private final int capacite;
    private final double prix;
    private final int nbReservations;
    private final double revenuTotal;
    private final double tauxRemplissage;

    public EventStats(
            int idEvent,
            String titre,
            int capacite,
            double prix,
            int nbReservations,
            double revenuTotal,
            double tauxRemplissage
    ) {
        this.idEvent = idEvent;
        this.titre = titre;
        this.capacite = capacite;
        this.prix = prix;
        this.nbReservations = nbReservations;
        this.revenuTotal = revenuTotal;
        this.tauxRemplissage = tauxRemplissage;
    }

    public int getIdEvent() {
        return idEvent;
    }

    public String getTitre() {
        return titre;
    }

    public int getCapacite() {
        return capacite;
    }

    public double getPrix() {
        return prix;
    }

    public int getNbReservations() {
        return nbReservations;
    }

    public double getRevenuTotal() {
        return revenuTotal;
    }

    public double getTauxRemplissage() {
        return tauxRemplissage;
    }
}
