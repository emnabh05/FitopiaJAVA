package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.services.ServicePersonne;

public class Main {

    public static void main(String[] args) {
        ServicePersonne sp = new ServicePersonne();
        sp.add(new Personne(10, "moula", "nacef"));
        sp.update(new Personne(1, 21, "moula", "nacef"));
        sp.delete(new Personne(1, 21, "moula", "nacef"));
        System.out.println(sp.getAll());
    }
}
