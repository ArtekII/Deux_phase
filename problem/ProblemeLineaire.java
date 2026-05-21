package problem;

import java.util.List;

public class ProblemeLineaire {
    double[] fonctionObjectif;
    List<Contrainte> contraintes;
    
    public ProblemeLineaire(double[] fonctionObjectif, List<Contrainte> contraintes) {
        this.fonctionObjectif = fonctionObjectif;
        this.contraintes = contraintes;
    }

    public double[] getFonctionObjectif() {
        return fonctionObjectif;
    }

    public void setFonctionObjectif(double[] fonctionObjectif) {
        this.fonctionObjectif = fonctionObjectif;
    }

    public List<Contrainte> getContraintes() {
        return contraintes;
    }

    public void setContraintes(List<Contrainte> contraintes) {
        this.contraintes = contraintes;
    }

    public void ajouterContrainte(double[] coefficients, TypeContrainte operateur, int secondMembre) {
        Contrainte contrainte = new Contrainte(coefficients, operateur, secondMembre);
        this.contraintes.add(contrainte);
    }
}
