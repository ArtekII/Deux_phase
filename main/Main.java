package main;

import java.util.ArrayList;

import problem.ProblemeLineaire;
import problem.TypeContrainte;
import service.ConvertisseurStandard;

public class Main {
    public static void main(String[] args) {
        // donnees d'entree
        // (fonction objectif, contraintes, etc.)

        //min z = 2x1+x2
        ProblemeLineaire probleme = new ProblemeLineaire(new double[]{2.0, 1.0}, new ArrayList<>());
        // contrainte source
        // x1+x2 >= 3
        // 2x1 + x2 = 4
        probleme.ajouterContrainte(new double[]{1.0,1.0}, TypeContrainte.SUPERIEUR_EGAL, 3.0);
        probleme.ajouterContrainte(new double[]{2.0, 1.0}, TypeContrainte.EGAL, 4.0);

        double[][] tab = ConvertisseurStandard.construireTableauSimplex(probleme);

        System.out.println("--- TABLEAU INITIAL PHASE 1 GÉNÉRÉ ---");
        for (double[] ligne : tab) {
            System.out.println(java.util.Arrays.toString(ligne));
        }

        // algorithme simplex a deux phase
    }
}
