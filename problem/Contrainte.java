package problem;
public class Contrainte {
    double[] coefficients;
    TypeContrainte operateur;
    double secondMembre;
    
    public Contrainte(double[] coefficients, TypeContrainte operateur, double secondMembre) {
        this.coefficients = coefficients;
        this.operateur = operateur;
        this.secondMembre = secondMembre;
    }

    public double[] getCoefficients() {
        return coefficients;
    }
    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }
    public TypeContrainte getOperateur() {
        return operateur;
    }
    public void setOperateur(TypeContrainte operateur) {
        this.operateur = operateur;
    }
    public double getSecondMembre() {
        return secondMembre;
    }
    public void setSecondMembre(double secondMembre) {
        this.secondMembre = secondMembre;
    }

}
