package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.services.ServicePersonne;

public class Main {

    public static void main(String[] args) {
        ServicePersonne sp = new ServicePersonne();
        sp.add(new Personne(10, "moula", "nacef"));
    }
}
