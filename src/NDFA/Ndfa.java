package NDFA;

public class Ndfa {
	/** État initial de l'automate */
    public final Etat etatInitial;

    /** État Final de l'automate */
    public final Etat etatFinal;

    public Ndfa(Etat etatInitial, Etat etatFinal) {
        this.etatInitial = etatInitial;
        this.etatFinal = etatFinal;
    }
    public Etat getEtatFinal() {
    	return this.etatFinal;
    }
}
