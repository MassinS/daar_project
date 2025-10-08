package DFA;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import NDFA.Ndfa;

public class Transformation {
	
	//Sert a retourner tous les etat accessiblle a partir d'une etat qui porte une transition epsilon(utile pour eliminer les transition epsilon dans notre principale algorithme)
	public Set<NDFA.Etat> epsilonClosure(Set<NDFA.Etat> etatsNDFA) {
	    // Ensemble final qui contiendra tous les états accessibles via epsilon
	    Set<NDFA.Etat> fermeture = new HashSet<>(etatsNDFA);
	    
	    // File pour parcours (BFS) des transitions epsilon
	    Deque<NDFA.Etat> pile = new ArrayDeque<>(etatsNDFA);
	    while (!pile.isEmpty()) {
	        NDFA.Etat current = pile.pop();  // prendre un état
	        // pour chaque état accessible via epsilon
	        for (NDFA.Etat suivant : current.getTransitionsEpsilon()) {
	            // si cet état n'est pas encore dans la fermeture
	            if (!fermeture.contains(suivant)) {
	                fermeture.add(suivant);   // on l'ajoute
	                pile.push(suivant);       // et on continue à partir de lui
	            }
	        }
	    }
	    
	    return fermeture;
	}
	
	public Set<NDFA.Etat> move(Set<NDFA.Etat> etats, int symbole) {
	    Set<NDFA.Etat> result = new HashSet<>();
	    for (NDFA.Etat e : etats) {
	        Set<NDFA.Etat> suivants = e.getTransitions(symbole);
	        if (suivants != null) {
	            result.addAll(suivants);
	        }
	    }
	    return result;
	}
	
	public Dfa transformationToDFA(Ndfa automate) {
	    // 1️- ε-closure de l’état initial NDFA
	    Set<NDFA.Etat> initClosure = epsilonClosure(Set.of(automate.etatInitial));

	    // Création structures pour DFA
	    Map<Set<NDFA.Etat>, Dfa.Etat> mapping = new HashMap<>(); // ensemble NDFA -> état DFA
	    Dfa.Etat etatInitialDFA = new Dfa.Etat();
	    mapping.put(initClosure, etatInitialDFA);

	    Set<Dfa.Etat> etats = new HashSet<>();
	    etats.add(etatInitialDFA);

	    Set<Dfa.Etat> etatsFinaux = new HashSet<>();

	    // Structures de contrôle
	    Deque<Set<NDFA.Etat>> aTraiter = new ArrayDeque<>();
	    aTraiter.add(initClosure);

	    // 2️- Exploration des ensembles NDFA
	    while (!aTraiter.isEmpty()) {
	        Set<NDFA.Etat> currentNDFA = aTraiter.pop();
	        Dfa.Etat currentDFA = mapping.get(currentNDFA);

	        for (int symbole=0;symbole<256;symbole++) {
	            // NDFA: move + epsilon-closure
	            Set<NDFA.Etat> nextNDFA = move(currentNDFA, symbole);
	            Set<NDFA.Etat> closure = epsilonClosure(nextNDFA);

	            if (closure.isEmpty()) continue;

	            Dfa.Etat nextDFA = mapping.get(closure);
	            if (nextDFA == null) {
	                nextDFA = new Dfa.Etat();
	                mapping.put(closure, nextDFA);
	                etats.add(nextDFA);
	                aTraiter.add(closure);
	            }

	            // ajout transition DFA
	            currentDFA.ajouterTransition(symbole, nextDFA);
	        }
	    }

	 // déterminer états finaux (préfère isFinal() si disponible)
        for (Map.Entry<Set<NDFA.Etat>, Dfa.Etat> entry : mapping.entrySet()) {
            Set<NDFA.Etat> ensembleNDFA = entry.getKey();
            Dfa.Etat etatDFA = entry.getValue();

            for (NDFA.Etat n : ensembleNDFA) {
                if (n.equals(automate.getEtatFinal())) {
                    etatsFinaux.add(etatDFA);
                    break;
                }
            }
        }

	    return new Dfa(etatInitialDFA, etatsFinaux, etats);
	}

}
