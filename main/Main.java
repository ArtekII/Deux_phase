package main;

import java.util.ArrayList;

import problem.ProblemeLineaire;
import problem.TypeContrainte;
import service.ConvertisseurStandard;
import service.SimplexDeuxPhase;

public class Main {
    public static void main(String[] args) {
        ProblemeLineaire probleme = new ProblemeLineaire(new double[]{3.0, 2.0}, new ArrayList<>());
        
        probleme.ajouterContrainte(new double[]{1.0,1.0}, TypeContrainte.EGAL, 4.0);
        probleme.ajouterContrainte(new double[]{1.0, 2.0}, TypeContrainte.SUPERIEUR_EGAL, 6.0);

        double[][] tab = ConvertisseurStandard.construireTableauSimplex(probleme);

        System.out.println("--- TABLEAU INITIAL PHASE 1 GÉNÉRÉ ---");
        for (double[] ligne : tab) {
            System.out.println(java.util.Arrays.toString(ligne));
        }

        SimplexDeuxPhase simplex = new SimplexDeuxPhase();
        simplex.resoudrePhase1(tab, probleme);  

        System.out.println("--- TABLEAU APRES PHASE 1 ---");
        for (double[] ligne : tab) {
            System.out.println(java.util.Arrays.toString(ligne));
        }

        double[][] tab2 = simplex.preparerTableauPhase2(tab, ConvertisseurStandard.getIndexArtificielles(), probleme);
        System.out.println("--- TABLEAU POUR PHASE 2 ---");
        for (double[] ligne : tab2) {
            System.out.println(java.util.Arrays.toString(ligne));
        }
        simplex.ajusterLigneObjectif(tab2, probleme);
        System.out.println("--- TABLEAU POUR PHASE 2 AVEC AJUSTEMENT ---");
        for (double[] ligne : tab2) {
            System.out.println(java.util.Arrays.toString(ligne));
        }

        double[][] solutionPhase2 = simplex.resoudre(probleme);
        System.out.println("--- TABLEAU FINAL APRES PHASE 2 ---");
        for (double[] ligne : solutionPhase2) {
            System.out.println(java.util.Arrays.toString(ligne));
        }
    }
}
