package service;

import java.util.ArrayList;
import java.util.List;

import problem.Contrainte;
import problem.ProblemeLineaire;
import problem.TypeContrainte;
import service.ConvertisseurStandard.PositionArtificielle;

public class SimplexDeuxPhase {

    public int chercherColonneEntrante(double[][] tableau, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableau[nbrContraintes].length-1; // sans le second membre
        int indexMax = -1; // on retourne -1 si aucun valeur dans la ligne objectif est superieur a 0
        double maxVal = 0; // on ne prend que des valeurs positifs
        
        for(int j = 0; j < nbrColonne; j++) {
            if(tableau[nbrContraintes][j] > maxVal) {
                maxVal = tableau[nbrContraintes][j];
                indexMax = j;
            }
        }

        return indexMax; // retourne -1 ou l'index du max > 0
    }

    public int chercherLigneSortante(double[][] tableau, int colEntrante, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableau[0].length-1; // index de la colonne du second membre

        int indexLigneMin=-1;
        double ratioMin = Double.MAX_VALUE;

        for (int i = 0; i < nbrContraintes; i++) {
            double coefficients = tableau[i][colEntrante];
            double b = tableau[i][nbrColonne];

            if(coefficients > 0 && b >= 0) {
                double ratio = b / coefficients;
                if(ratio < ratioMin) {
                    ratioMin = ratio;
                    indexLigneMin = i;
                }
            }
        }
        
        return indexLigneMin;
    }

    public void pivoter(double[][] tableau, int lignePivot, int colonnePivot, ProblemeLineaire probleme) {
        int nbrLigne = tableau.length;
        int nbrColonne = tableau[0].length;

        double pivot = tableau[lignePivot][colonnePivot];

        // normaliser la ligne du pivot pour que le pivot = 1
        for(int j = 0; j < nbrColonne; j++) {
            tableau[lignePivot][j] = tableau[lignePivot][j]/pivot;
        }

        // mettre a jour les autres lignes pour mettre des 0 dans la colonne du pivot
        for(int i = 0; i < nbrLigne; i++) {
            if(i != lignePivot) {
                double coefAElimine = tableau[i][colonnePivot];
                for(int j = 0; j < nbrColonne; j++) {
                    tableau[i][j] -= coefAElimine * tableau[lignePivot][j];
                }
            }
        }
    }

    // reduire la somme des variables artificielles a 0 (W = a1 + a2)
    
    public void resoudrePhase1(double[][] tableau, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonneb = tableau[0].length - 1;
        while(true) {
            int colEntrante = chercherColonneEntrante(tableau, probleme);
            if(colEntrante == -1) {
                System.out.println("Phase 1 terminée : Tableau optimal atteint.");
                break;
            }

            int ligneSortante = chercherLigneSortante(tableau, colEntrante, probleme);
            if (ligneSortante == -1) {
                // si toutes les coeff de la colonne entrante est negatif sur chaque ligne, alors la fonction objectif tend vers l'infinie sans rencontrer les contraintes
                throw new ArithmeticException("L'algorithme s'arrête : Le problème est non borné (solution infinie).");
            }

            pivoter(tableau, colEntrante, ligneSortante, probleme);
        }

        // si la valeur finale de la fonction objectif de la Phase 1 est différente de zéro, 
        // cela signifie qu'il impossible de réduire les variables artificielles à zéro. 
        // en d'autres termes, il n'existe aucune combinaison de variables qui puisse satisfaire toutes tes contraintes en même temps.
        // donc la fonction objectif est ierralisable avec ces contraintes

        // On utilise une petite marge d'erreur (epsilon) pour les arrondis des double
        double valeurObjectifPhase1 = tableau[nbrContraintes][nbrColonneb];
        if (Math.abs(valeurObjectifPhase1) > 1e-9) {
            throw new IllegalStateException("Le problème est irréalisable : aucune solution ne satisfait toutes les contraintes.");
        }
        
        System.out.println("Phase 1 réussie ! Une solution réalisable de base a été trouvée.");
    }

    public double[][] preparerTableauPhase2(double[][] tableauPhase1, List<PositionArtificielle> indexArtificielles, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonnesPhase1 = tableauPhase1[0].length;
        
        // calculer la largeur du nouveau tableau (Taille Phase 1 - Nombre d'artificielles)
        int nbrColonnesPhase2 = nbrColonnesPhase1 - indexArtificielles.size();
        double[][] tableauPhase2 = new double[nbrContraintes + 1][nbrColonnesPhase2];
        
        // extraire uniquement les index des colonnes à bannir
        List<Integer> colonnesAIgnorer = new ArrayList<>();
        for (PositionArtificielle pos : indexArtificielles) {
            colonnesAIgnorer.add(pos.colonne());
        }

        // copier les cases valides
        for (int i = 0; i <= nbrContraintes; i++) {
            int nouvelleColonne = 0;
            for (int j = 0; j < nbrColonnesPhase1; j++) {
                // si la colonne j est une variable artificielle, on skip
                if (colonnesAIgnorer.contains(j)) {
                    continue;
                }
                // sinon, on la copie dans notre tableau
                tableauPhase2[i][nouvelleColonne] = tableauPhase1[i][j];
                nouvelleColonne++;
            }
        }
        
        int nbrVariablesDecisions = probleme.getFonctionObjectif().length;

        // injecter les coefficients de l'objectif en inversant le signe (-c_j)
        for(int j = 0; j < nbrVariablesDecisions; j++) {
            tableauPhase2[nbrContraintes][j] = -probleme.getFonctionObjectif()[j];
        }

        // initialiser les variables d'écart/excès restantes à 0.0
        for(int j = nbrVariablesDecisions; j < nbrColonnesPhase2 - 1; j++) {
            tableauPhase2[nbrContraintes][j] = 0.0;
        }

        // initialiser la valeur de la fonction objectif (colonne b) à 0.0
        tableauPhase2[nbrContraintes][nbrColonnesPhase2 - 1] = 0.0;
        
        return tableauPhase2;
    }

    public double[][] resoudre(ProblemeLineaire probleme) {
        // on construit le tableau initial avec les variables artificielles
        double[][] tableau = ConvertisseurStandard.construireTableauSimplex(probleme);
        
        // on élimine les variables artificielles
        resoudrePhase1(tableau, probleme);
        
        // on nettoie le tableau pour le problème reel
        double[][] tableauPhase2 = preparerTableauPhase2(tableau, ConvertisseurStandard.getIndexArtificielles(), probleme);
        
        // on cherche la solution optimale finale
        resoudrePhase2(tableauPhase2, probleme);
        
        return tableauPhase2; // contient la solution optimale
    }
}
