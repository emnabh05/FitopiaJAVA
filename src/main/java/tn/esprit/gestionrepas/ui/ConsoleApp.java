package tn.esprit.gestionrepas.ui;

import tn.esprit.gestionrepas.Models.RegimeAlimentaire;
import tn.esprit.gestionrepas.Models.Repas;
import tn.esprit.gestionrepas.services.ServiceRegimeAlimentaire;
import tn.esprit.gestionrepas.services.ServiceRepas;
import tn.esprit.gestionrepas.utils.InputHelper;

import java.util.List;

public class ConsoleApp {
    private final InputHelper inputHelper = new InputHelper();
    private final ServiceRegimeAlimentaire serviceRegimeAlimentaire = new ServiceRegimeAlimentaire();
    private final ServiceRepas serviceRepas = new ServiceRepas();

    public void run() {
        int choix;

        do {
            afficherMenuPrincipal();
            choix = inputHelper.readInt("Votre choix : ");

            try {
                switch (choix) {
                    case 1 -> menuRegimes();
                    case 2 -> menuRepas();
                    case 0 -> System.out.println("Au revoir.");
                    default -> System.out.println("Choix invalide.");
                }
            } catch (RuntimeException e) {
                System.out.println("Operation impossible : " + e.getMessage());
            }
        } while (choix != 0);
    }

    private void afficherMenuPrincipal() {
        System.out.println();
        System.out.println("=== Gestion Fitopia : Repas et Regimes ===");
        System.out.println("1. CRUD Regime Alimentaire");
        System.out.println("2. CRUD Repas");
        System.out.println("0. Quitter");
    }

    private void menuRegimes() {
        int choix;

        do {
            System.out.println();
            System.out.println("--- Menu Regime Alimentaire ---");
            System.out.println("1. Ajouter un regime");
            System.out.println("2. Afficher tous les regimes");
            System.out.println("3. Rechercher un regime par id");
            System.out.println("4. Modifier un regime");
            System.out.println("5. Supprimer un regime");
            System.out.println("0. Retour");

            choix = inputHelper.readInt("Votre choix : ");

            switch (choix) {
                case 1 -> ajouterRegime();
                case 2 -> afficherTousLesRegimes();
                case 3 -> afficherRegimeParId();
                case 4 -> modifierRegime();
                case 5 -> supprimerRegime();
                case 0 -> {
                }
                default -> System.out.println("Choix invalide.");
            }
        } while (choix != 0);
    }

    private void menuRepas() {
        int choix;

        do {
            System.out.println();
            System.out.println("--- Menu Repas ---");
            System.out.println("1. Ajouter un repas");
            System.out.println("2. Afficher tous les repas");
            System.out.println("3. Rechercher un repas par id");
            System.out.println("4. Modifier un repas");
            System.out.println("5. Supprimer un repas");
            System.out.println("0. Retour");

            choix = inputHelper.readInt("Votre choix : ");

            switch (choix) {
                case 1 -> ajouterRepas();
                case 2 -> afficherTousLesRepas();
                case 3 -> afficherRepasParId();
                case 4 -> modifierRepas();
                case 5 -> supprimerRepas();
                case 0 -> {
                }
                default -> System.out.println("Choix invalide.");
            }
        } while (choix != 0);
    }

    private void ajouterRegime() {
        RegimeAlimentaire regime = saisirRegime();
        serviceRegimeAlimentaire.add(regime);
        System.out.println("Regime ajoute avec succes :");
        System.out.println(regime);
    }

    private void afficherTousLesRegimes() {
        List<RegimeAlimentaire> regimes = serviceRegimeAlimentaire.getAll();

        if (regimes.isEmpty()) {
            System.out.println("Aucun regime trouve.");
            return;
        }

        regimes.forEach(System.out::println);
    }

    private void afficherRegimeParId() {
        int id = inputHelper.readInt("Id du regime : ");
        RegimeAlimentaire regime = serviceRegimeAlimentaire.getById(id);

        if (regime == null) {
            System.out.println("Aucun regime trouve pour cet id.");
            return;
        }

        System.out.println(regime);
    }

    private void modifierRegime() {
        int id = inputHelper.readInt("Id du regime a modifier : ");
        RegimeAlimentaire existant = serviceRegimeAlimentaire.getById(id);

        if (existant == null) {
            System.out.println("Aucun regime trouve pour cet id.");
            return;
        }

        System.out.println("Valeur actuelle : " + existant);
        RegimeAlimentaire regime = saisirRegime();
        regime.setId(id);
        serviceRegimeAlimentaire.update(regime);
        System.out.println("Regime modifie avec succes :");
        System.out.println(regime);
    }

    private void supprimerRegime() {
        int id = inputHelper.readInt("Id du regime a supprimer : ");
        boolean deleted = serviceRegimeAlimentaire.deleteById(id);
        System.out.println(deleted ? "Regime supprime avec succes." : "Aucun regime supprime.");
    }

    private void ajouterRepas() {
        Repas repas = saisirRepas();
        serviceRepas.add(repas);
        System.out.println("Repas ajoute avec succes :");
        System.out.println(repas);
    }

    private void afficherTousLesRepas() {
        List<Repas> repasList = serviceRepas.getAll();

        if (repasList.isEmpty()) {
            System.out.println("Aucun repas trouve.");
            return;
        }

        repasList.forEach(System.out::println);
    }

    private void afficherRepasParId() {
        int id = inputHelper.readInt("Id du repas : ");
        Repas repas = serviceRepas.getById(id);

        if (repas == null) {
            System.out.println("Aucun repas trouve pour cet id.");
            return;
        }

        System.out.println(repas);
    }

    private void modifierRepas() {
        int id = inputHelper.readInt("Id du repas a modifier : ");
        Repas existant = serviceRepas.getById(id);

        if (existant == null) {
            System.out.println("Aucun repas trouve pour cet id.");
            return;
        }

        System.out.println("Valeur actuelle : " + existant);
        Repas repas = saisirRepas();
        repas.setIdRepas(id);
        serviceRepas.update(repas);
        System.out.println("Repas modifie avec succes :");
        System.out.println(repas);
    }

    private void supprimerRepas() {
        int id = inputHelper.readInt("Id du repas a supprimer : ");
        boolean deleted = serviceRepas.deleteById(id);
        System.out.println(deleted ? "Repas supprime avec succes." : "Aucun repas supprime.");
    }

    private RegimeAlimentaire saisirRegime() {
        RegimeAlimentaire regime = new RegimeAlimentaire();
        regime.setUserId(inputHelper.readInt("User id : "));
        regime.setTaille(inputHelper.readOptionalDouble("Taille en cm : "));
        regime.setPoids(inputHelper.readOptionalDouble("Poids en kg : "));
        regime.setAge(inputHelper.readOptionalInt("Age : "));
        regime.setCaloriesCibles(inputHelper.readOptionalInt("Calories cibles : "));
        regime.setRepasAdequats(inputHelper.readOptionalText("Repas adequats : "));
        return regime;
    }

    private Repas saisirRepas() {
        Repas repas = new Repas();
        repas.setUserId(inputHelper.readInt("User id : "));
        repas.setDateRepas(inputHelper.readDateTime("Date du repas (yyyy-MM-dd HH:mm) : "));
        repas.setTypeRepas(inputHelper.readRequiredText("Type de repas : "));
        repas.setNomRepas(inputHelper.readRequiredText("Nom du repas : "));
        repas.setCalories(inputHelper.readOptionalInt("Calories : "));
        repas.setProteines(inputHelper.readOptionalInt("Proteines : "));
        repas.setGlucides(inputHelper.readOptionalInt("Glucides : "));
        repas.setLipides(inputHelper.readOptionalInt("Lipides : "));
        repas.setCommentaire(inputHelper.readOptionalText("Commentaire : "));
        repas.setRegimeId(inputHelper.readOptionalInt("Id du regime associe (laisser vide si aucun) : "));
        return repas;
    }
}
