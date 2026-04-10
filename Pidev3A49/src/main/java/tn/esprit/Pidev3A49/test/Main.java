package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.Personne;
import tn.esprit.Pidev3A49.services.ServicePersonne;
import tn.esprit.Pidev3A49.utils.MyDataBase;

public class Main {


    public static void main(String[] args) {
//        Test t1 =Test.getInstance();
//        Test t2 =Test.getInstance();
//
//        System.out.println(t1);
//        System.out.println(t2);
        ServicePersonne sp = new ServicePersonne();

      // sp.add(new Personne(10,"moula","nacef"));

        System.out.println(sp.getAll());

    }
}
