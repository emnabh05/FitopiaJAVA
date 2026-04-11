package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.services.ServicePersonne;

public class Main {

    public static void main(String[] args) {
        ServicePersonne sp = new ServicePersonne();

        Personne nouvellePersonne = new Personne(22, "NomDemo", "PrenomDemo");

        try {
            sp.add(nouvellePersonne);
            System.out.println("Create OK");
            System.out.println(sp.getAll());

            Personne premierePersonne = sp.getAll().stream().findFirst().orElse(null);
            if (premierePersonne != null) {
                premierePersonne.setNom("NomModifie");
                premierePersonne.setPrenom("PrenomModifie");
                premierePersonne.setAge(23);
                sp.update(premierePersonne);
                System.out.println("Update OK");
                System.out.println(sp.getById(premierePersonne.getId()));

                sp.delete(premierePersonne);
                System.out.println("Delete OK");
                System.out.println(sp.getAll());
            }
        } catch (RuntimeException e) {
            System.out.println("Execution CRUD interrompue : " + e.getMessage());
        }
    }
}
