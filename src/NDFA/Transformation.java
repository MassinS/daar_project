package NDFA;

import Regex.RegexArbre;
import Regex.RegexParseur;

/**
 * Transformation d'un arbre syntaxique Regex en NDFA selon l'algorithme de Thompson.
 */
public class Transformation {

    public Ndfa ArbreToNdfa(RegexArbre arbre) {
        
        // --- Cas concaténation AB ---
        // Relie le NDFA de gauche au NDFA de droite
        if (arbre.getRoot() == RegexParseur.CONCAT) {
            Ndfa droite = ArbreToNdfa(arbre.getFilsDroite());
            Ndfa gauche = ArbreToNdfa(arbre.getFilsGauche());
            
            // RELIER les deux automates avec une epsilon-transition
            gauche.etatFinal.ajouterTransition(droite.etatInitial);
            
            // L'état initial est celui de gauche, l'état final est celui de droite
            return new Ndfa(gauche.etatInitial, droite.etatFinal);
        }

        // --- Cas alternation A|B ---
        // Crée un nouvel état initial et un nouvel état final
        if (arbre.getRoot() == RegexParseur.ALTERN) {
            Ndfa droite = ArbreToNdfa(arbre.getFilsDroite());
            Ndfa gauche = ArbreToNdfa(arbre.getFilsGauche());

            Etat etatinitial = new Etat();
            Etat etatfinal = new Etat();

            // ε-transitions vers les NDFA gauche et droite
            etatinitial.ajouterTransition(gauche.etatInitial);
            etatinitial.ajouterTransition(droite.etatInitial);

            // Les états finaux de gauche et droite vont vers le nouvel état final
            droite.etatFinal.ajouterTransition(etatfinal);
            gauche.etatFinal.ajouterTransition(etatfinal);

            return new Ndfa(etatinitial, etatfinal);
        }

        // --- Cas Kleene star A* ---
        if (arbre.getRoot() == RegexParseur.ETOILE) {
            Etat etatinitial = new Etat();
            Etat etatfinal = new Etat();
            Ndfa gauche = ArbreToNdfa(arbre.getFilsGauche());

            // ε-transition initiale vers le NDFA et vers le final (permet 0 occurrence)
            etatinitial.ajouterTransition(gauche.etatInitial);
            etatinitial.ajouterTransition(etatfinal);

            // Les transitions du NDFA vers lui-même (répétition) et vers le final
            gauche.etatFinal.ajouterTransition(gauche.etatInitial);
            gauche.etatFinal.ajouterTransition(etatfinal);

            return new Ndfa(etatinitial, etatfinal);
        }

        // --- Cas symbole simple ou DOT (.) ---
        if (arbre.getSousArbre().isEmpty()) {
            Etat etatinitial = new Etat();
            Etat etatfinal = new Etat();

            if (arbre.getRoot() != RegexParseur.DOT) {
                // Transition normale sur le caractère
                etatinitial.ajouterTransition(arbre.getRoot(), etatfinal);
            } else {
                // DOT = n'importe quel caractère (ASCII 0 à 256)
                for (int i = 0; i <= 256; i++) {
                    etatinitial.ajouterTransition(i, etatfinal);
                }
            }

            return new Ndfa(etatinitial, etatfinal);
        }

        // Retourne null si aucun cas ne correspond (devrait normalement jamais arriver)
        return null;
    }
}
