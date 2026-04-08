package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.RegimeAlimentaire;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.services.ServiceRegimeAlimentaire;
import tn.esprit.Pidev3A49.services.ServiceSupplement;

public class Main {

    public static void main(String[] args) {
        ServiceRegimeAlimentaire serviceRegime = new ServiceRegimeAlimentaire();
        ServiceSupplement serviceSupplement = new ServiceSupplement();

        System.out.println("Regimes alimentaires :");
        for (RegimeAlimentaire regime : serviceRegime.getAll()) {
            System.out.println(regime);
        }

        System.out.println("\nSupplements :");
        for (Supplement supplement : serviceSupplement.getAll()) {
            System.out.println(supplement);
        }
    }
}
