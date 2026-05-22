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
        int nbrColonne = tableau[nbrContraintes].length-1;
        int indexMax = -1;
        double maxVal = 0;
        
        for(int j = 0; j < nbrColonne; j++) {
            if(tableau[nbrContraintes][j] > maxVal) {
                maxVal = tableau[nbrContraintes][j];
                indexMax = j;
            }
        }

        return indexMax;
    }

    public int chercherLigneSortante(double[][] tableau, int colEntrante, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableau[0].length-1;

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

        for(int j = 0; j < nbrColonne; j++) {
            tableau[lignePivot][j] = tableau[lignePivot][j]/pivot;
        }

        for(int i = 0; i < nbrLigne; i++) {
            if(i != lignePivot) {
                double coefAElimine = tableau[i][colonnePivot];
                for(int j = 0; j < nbrColonne; j++) {
                    tableau[i][j] -= coefAElimine * tableau[lignePivot][j];
                }
            }
        }
    }
    
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
                throw new ArithmeticException("L'algorithme s'arrête : Le problème est non borné (solution infinie).");
            }

            pivoter(tableau, colEntrante, ligneSortante, probleme);
        }

        double valeurObjectifPhase1 = tableau[nbrContraintes][nbrColonneb];
        if (Math.abs(valeurObjectifPhase1) > 1e-9) {
            throw new IllegalStateException("Le problème est irréalisable : aucune solution ne satisfait toutes les contraintes.");
        }
        
        // System.out.println("Phase 1 réussie ! Une solution réalisable de base a été trouvée.");
    }

    public double[][] preparerTableauPhase2(double[][] tableauPhase1, List<PositionArtificielle> indexArtificielles, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonnesPhase1 = tableauPhase1[0].length;
        
        int nbrColonnesPhase2 = nbrColonnesPhase1 - indexArtificielles.size();
        double[][] tableauPhase2 = new double[nbrContraintes + 1][nbrColonnesPhase2];
        
        List<Integer> colonnesAIgnorer = new ArrayList<>();
        for (PositionArtificielle pos : indexArtificielles) {
            colonnesAIgnorer.add(pos.colonne());
        }

        for (int i = 0; i <= nbrContraintes; i++) {
            int nouvelleColonne = 0;
            for (int j = 0; j < nbrColonnesPhase1; j++) {
                if (colonnesAIgnorer.contains(j)) {
                    continue;
                }
                tableauPhase2[i][nouvelleColonne] = tableauPhase1[i][j];
                nouvelleColonne++;
            }
        }
        
        int nbrVariablesDecisions = probleme.getFonctionObjectif().length;

        for(int j = 0; j < nbrVariablesDecisions; j++) {
            tableauPhase2[nbrContraintes][j] = probleme.getFonctionObjectif()[j];
        }

        for(int j = nbrVariablesDecisions; j < nbrColonnesPhase2 - 1; j++) {
            tableauPhase2[nbrContraintes][j] = 0.0;
        }

        tableauPhase2[nbrContraintes][nbrColonnesPhase2 - 1] = 0.0;
        
        return tableauPhase2;
    }

    public int trouverVariableDeBasePourLigne(double[][] tableau, int ligne) {
        int nbrColonne = tableau[0].length;
        for(int j = 0; j < nbrColonne - 1; j++) {
            if(Math.abs(tableau[ligne][j] - 1.0) < 1e-9) {
                boolean estBase = true;
                for(int i = 0; i < tableau.length - 1; i++) {
                    if(i != ligne && Math.abs(tableau[i][j]) > 1e-9) {
                        estBase = false;
                        break;
                    }
                }
                if(estBase) {
                    return j;
                }
            }
        }
        return -1; 
    }

    public void ajusterLigneObjectif(double[][] tableau, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableau[0].length;

        for(int i = 0; i < nbrContraintes; i++) {
            int varBase = trouverVariableDeBasePourLigne(tableau, i);
            if(varBase != -1) {
                double coef = tableau[nbrContraintes][varBase];
                if(Math.abs(coef) > 1e-9) {
                    for(int j = 0; j < nbrColonne; j++) {
                        tableau[nbrContraintes][j] -= coef * tableau[i][j];
                    }
                }
            }
        }
    }

    public void resoudrePhase2(double[][] tableau, ProblemeLineaire probleme) {
        ajusterLigneObjectif(tableau, probleme);
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableau[0].length;

        while(true) {
            int colEntrante = chercherColonneEntrante(tableau, probleme);
            if(colEntrante == -1) {
                System.out.println("Phase 2 terminée : Tableau optimal atteint.");
                break;
            }

            int ligneSortante = chercherLigneSortante(tableau, colEntrante, probleme);
            if (ligneSortante == -1) {
                throw new ArithmeticException("L'algorithme s'arrête : Le problème est non borné (solution infinie).");
            }

            pivoter(tableau, ligneSortante, colEntrante, probleme);
        }
    }

    public double[][] resoudre(ProblemeLineaire probleme) {
        double[][] tableau = ConvertisseurStandard.construireTableauSimplex(probleme);
        
        resoudrePhase1(tableau, probleme);
        
        double[][] tableauPhase2 = preparerTableauPhase2(tableau, ConvertisseurStandard.getIndexArtificielles(), probleme);
        
        resoudrePhase2(tableauPhase2, probleme);
        
        return tableauPhase2;
    }

    public List<Double> extraireSolution(double[][] tableauPhase2, ProblemeLineaire probleme) {
        int nbrContraintes = probleme.getContraintes().size();
        int nbrColonne = tableauPhase2[0].length;
        List<Double> solution = new ArrayList<>();

        for(int j = 0; j < nbrColonne - 1; j++) {
            boolean estBase = false;
            double valeurVariable = 0.0;

            for(int i = 0; i < nbrContraintes; i++) {
                if(tableauPhase2[i][j] == 1) {
                    if(estBase) {
                        estBase = false;
                        break;
                    } else {
                        estBase = true;
                        valeurVariable = tableauPhase2[i][nbrColonne - 1];
                    }
                } else if(tableauPhase2[i][j] != 0) {
                    estBase = false;
                    break;
                }
            }

            if(estBase) {
                solution.add(valeurVariable);
            } else {
                solution.add(0.0);
            }
        }

        return solution;
    }
}
