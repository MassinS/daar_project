package DFA;

import java.util.*;

public class Dfa {

    /** L’état initial */
    public final Etat etatInitial;

    /** Les états finaux */
    public final Set<Etat> etatsFinaux;

    /** Tous les états */
    public final Set<Etat> etats;

    /** Classe représentant un état du DFA */
    public static class Etat {
        private static int compteur = 0;
        public final int id;
        private final Map<Integer, Etat> transitions; // symbole → état unique

        public Etat() {
            this.id = compteur++;
            this.transitions = new HashMap<>();
        }

        /** Ajouter une transition (déterministe) */
        public void ajouterTransition(int symbole, Etat suivant) {
            transitions.put(symbole, suivant);
        }

        /** Obtenir la transition pour un symbole */
        public Etat obtenirTransition(int symbole) {
            return transitions.get(symbole); // peut renvoyer null si pas défini
        }

        @Override
        public String toString() {
            return "Etat(" + id + ")";
        }
    }

    public Dfa(Etat etatInitial, Set<Etat> etatsFinaux, Set<Etat> etats) {
        this.etatInitial = etatInitial;
        this.etatsFinaux = etatsFinaux;
        this.etats = etats;
    }
}
