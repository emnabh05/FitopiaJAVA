package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.Repas;
import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.services.ServiceRepas;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;

public class Main {

    public static void main(String[] args) {
        ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
        ServiceRepas serviceRepas = new ServiceRepas();

        System.out.println("Regimes alimentaires :");
        for (RegimeAlimentaire regime : serviceRegime.getAll()) {
            System.out.println(regime);
        }

        System.out.println("\nRepas :");
        for (Repas repas : serviceRepas.getAll()) {
            System.out.println(repas);
        }
    }
}
