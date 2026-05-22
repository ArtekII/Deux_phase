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

    public static double[][] construireTableauSimplex(ProblemeLineaire probleme) {
        indexArtificielles.clear();
        int nbrVariablesDecisions = probleme.getFonctionObjectif().length;
        int nbrContraintes = probleme.getContraintes().size();
        int nbrVariablesArtificielle = 0;

        int nbrColonne = nbrVariablesDecisions;

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

        double[][] tableau = new double[nbrContraintes+1][nbrColonne+1];

        int colonneActuel = nbrVariablesDecisions;

        for(int i = 0; i < nbrContraintes; i++) {
            Contrainte c = probleme.getContraintes().get(i);

            for(int j = 0; j < nbrVariablesDecisions; j++) {
                tableau[i][j] = c.getCoefficients()[j];
            }

            tableau[i][nbrColonne] = c.getSecondMembre();

            if(c.getOperateur() == TypeContrainte.INFERIEUR_EGAL) {
                tableau[i][colonneActuel] = 1.0;
                colonneActuel++;
            } else if(c.getOperateur() == TypeContrainte.SUPERIEUR_EGAL) {
                tableau[i][colonneActuel] = -1.0;
                colonneActuel++;

                tableau[i][colonneActuel] = 1.0;

                tableau[nbrContraintes][colonneActuel] = -1.0;

                indexArtificielles.add(new PositionArtificielle(i, colonneActuel));
                colonneActuel++;
            } else if(c.getOperateur() == TypeContrainte.EGAL) {
                tableau[i][colonneActuel] = 1.0;

                tableau[nbrContraintes][colonneActuel] = -1.0;

                indexArtificielles.add(new PositionArtificielle(i, colonneActuel));
                colonneActuel++;
            }
        }

        for(PositionArtificielle pos : indexArtificielles) {
            int i = pos.ligne();
            for(int j = 0; j <= nbrColonne; j++) {
                tableau[nbrContraintes][j] += tableau[i][j];
            }
        }
        
        return tableau;
    }
}
