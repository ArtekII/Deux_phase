package service;

import problem.Contrainte;
import problem.ProblemeLineaire;
import problem.TypeContrainte;

public class ConvertisseurStandard {
    // construction de tableau simplex a partir d'un probleme lineaire
    public double[][] construireTableauSimplex(ProblemeLineaire probleme) {
        // nombre de variables de decision

        // pour chaque contrainte "<=" ajouter une variable d'ecart
        // pour chaque contrainte "=" ajouter une variable d'artifice
        // pour chaque contrainte ">=" ajouter une variable d'exces et une variable d'artifice
        int nbrColonne = probleme.getFonctionObjectif().length;;

        for(Contrainte c : probleme.getContraintes()) {
            if(c.getOperateur() == TypeContrainte.INFERIEUR_EGAL) {
                nbrColonne++; // variable d'ecart
            } else if(c.getOperateur() == TypeContrainte.EGAL) {
                nbrColonne++; // variable artificielle
            } else if(c.getOperateur() == TypeContrainte.SUPERIEUR_EGAL) {
                nbrColonne += 2; // variable d'exces + variable artificielle
            }
        }
        
        return null;
    }
}
