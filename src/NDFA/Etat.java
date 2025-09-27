package NDFA;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Etat {
	private static int numero=0;// pour donner des IDs uniques
	public int Id;
	/** transitions "normales" : symbole -> ensemble d’états */
    private final Map<Integer, Set<Etat>> transitions = new HashMap<>();

    /** transitions epsilon */
    private final Set<Etat> transitionsEpsilon = new HashSet<>();

    public Etat() {
        Id = numero++;
    }
    /** Transition avec symbole (caractère ASCII) */
    public void ajouterTransition(int symbole, Etat suivant) {
        if (!transitions.containsKey(symbole)) {
            transitions.put(symbole, new HashSet<>());
        }
        transitions.get(symbole).add(suivant);
    }
    /** Transition epsilon */
    public void ajouterTransition(Etat suivant) {
        transitionsEpsilon.add(suivant);
    }
    public Set<Etat> getTransitions(int symbole) {
        return transitions.getOrDefault(symbole, Collections.emptySet());
    }

    public Set<Etat> getTransitionsEpsilon() {
        return transitionsEpsilon;
    }
    
	
}
