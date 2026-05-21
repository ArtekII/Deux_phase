package service;

import java.util.ArrayList;
import java.util.List;

import problem.Contrainte;
import problem.ProblemeLineaire;
import problem.TypeContrainte;

public class ConvertisseurStandard {
        record PositionArtificielle(int ligne, int colonne){}
        private static List<PositionArtificielle> indexArtificielles = new ArrayList<>();

        public static List<PositionArtificielle> getIndexArtificielles() {
            return indexArtificielles;
        }
        public static void setIndexArtificielles(List<PositionArtificielle> indexArtificielles) {
            ConvertisseurStandard.indexArtificielles = indexArtificielles;
        }

    // construction de tableau simplex a partir d'un probleme lineaire
    public static double[][] construireTableauSimplex(ProblemeLineaire probleme) {
        // nombre de variables de decision
        int nbrVariablesDecisions = probleme.getFonctionObjectif().length;
        // nombre de contrainte
        int nbrContraintes = probleme.getContraintes().size();
        // nombre de variables artificielles
        int nbrVariablesArtificielle = 0;

        // pour chaque contrainte "<=" ajouter une variable d'ecart
        // pour chaque contrainte "=" ajouter une variable d'artifice
        // pour chaque contrainte ">=" ajouter une variable d'exces et une variable d'artifice
        int nbrColonne = nbrVariablesDecisions;

        // trouver le nombre de colonne
        for(Contrainte c : probleme.getContraintes()) {
            if(c.getOperateur() == TypeContrainte.INFERIEUR_EGAL) {
                nbrColonne++; // variable d'ecart
            } else if(c.getOperateur() == TypeContrainte.EGAL) {
                nbrColonne++; // variable artificielle
                nbrVariablesArtificielle++;
            } else if(c.getOperateur() == TypeContrainte.SUPERIEUR_EGAL) {
                nbrColonne += 2; // variable d'exces + variable artificielle
                nbrVariablesArtificielle++;
            }
        }
        // // au lieu de juste se souvenir de l'index de colonne, on utilise un structure record pour stocker ligne et colonne de la variable artificielle
        // record PositionArtificielle(int ligne, int colonne){}
        // List<PositionArtificielle> indexArtificielles = new ArrayList<>();

        double[][] tableau = new double[nbrContraintes+1][nbrColonne+1];

        int colonneActuel = nbrVariablesDecisions;

        // pour chaque ligne
        for(int i = 0; i < nbrContraintes; i++) {
            Contrainte c = probleme.getContraintes().get(i);

            // placer les coefficients au debut
            for(int j = 0; j < nbrVariablesDecisions; j++) {
                tableau[i][j] = c.getCoefficients()[j];
            }

            // placer le second membre a la fin
            tableau[i][nbrColonne] = c.getSecondMembre();

            // indexation selon le type de contrainte
            if(c.getOperateur() == TypeContrainte.INFERIEUR_EGAL) {
                tableau[i][colonneActuel] = 1.0; // variable d'ecart (+s)
                colonneActuel++;
            } else if(c.getOperateur() == TypeContrainte.SUPERIEUR_EGAL) {
                tableau[i][colonneActuel] = -1.0; // variable d'exces (-s)
                colonneActuel++;

                tableau[i][colonneActuel] = 1.0; // vartiable artificielle (+a)

                // initialiser la derniere ligne, a la colonne correspondante
                // (a modifier si on veut changer le signe de la derniere ligne)
                tableau[nbrContraintes][colonneActuel] = -1.0; // pour avoir 0 apres dans la derniere ligne

                indexArtificielles.add(new PositionArtificielle(i, colonneActuel)); // memoriser la colonne pour la phase 1
                colonneActuel++;
            } else if(c.getOperateur() == TypeContrainte.EGAL) {
                tableau[i][colonneActuel] = 1.0; // variable artificielle (+a)

                // initialiser la derniere ligne, a la colonne correspondante 
                // (a modifier si on veut changer le signe de la derniere ligne)
                tableau[nbrContraintes][colonneActuel] = -1.0; // pour avoir 0 apres dans la derniere ligne \

                indexArtificielles.add(new PositionArtificielle(i, colonneActuel)); // memoriser la colonne pour la phase 1
                colonneActuel++;
            }
        }

        // construction de la derniere ligne (ligne de la fonction objectif de la phase 1)
        // substitution des lignes de contraintes
        for(PositionArtificielle pos : indexArtificielles) {
            // au lieu de recherche la ligne i ou la variable artificielle vaut 1
            int i = pos.ligne(); // on prend direct la ligne de la variable artificielle
            for(int j = 0; j <= nbrColonne; j++) {
                // on soustrait/additioner toute la ligne i a la ligne de l'objectif (la dernière ligne)
                // (a modifier si on veut changer le signe de la derniere ligne)
                tableau[nbrContraintes][j] += tableau[i][j];
            }
        }
        
        return tableau;
    }
}
