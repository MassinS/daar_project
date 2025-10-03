package Regex;

import NDFA.Ndfa;

import DFA.Dfa;
import Minimisation.Minimisation;

public class Main {
	
    public static void main(String[] args) {
    	
    	  try {
       
            // ==================== ÉTAPE 1 : Regex → Arbre Syntaxique ====================
            System.out.println("=== ÉTAPE 1 : Parsing de l'expression régulière ===");
            String expression = "(a|(/*))";
            RegexArbre arbre = RegexParseur.parseur(expression);
            
            System.out.println("Expression: " + expression);
            System.out.println("Arbre syntaxique (notation préfixe):");
            System.out.println(arbre.afficherNotationPrefixe());
            System.out.println();
            
            // ==================== ÉTAPE 2 : Arbre → NDFA ====================
            System.out.println("=== ÉTAPE 2 : Transformation en NDFA ===");
            NDFA.Transformation transformNDFA = new NDFA.Transformation();
            Ndfa ndfa = transformNDFA.ArbreToNdfa(arbre);
            
            System.out.println("NDFA créé avec succès !");
            System.out.println("État initial NDFA: " + ndfa.etatInitial.Id);
            System.out.println("État final NDFA: " + ndfa.etatFinal.Id);
            System.out.println();
            
            // ==================== ÉTAPE 3 : NDFA → DFA ====================
            System.out.println("=== ÉTAPE 3 : Transformation en DFA ===");
            
            // Si tu veux gérer le point '.', ajoute tous les caractères ASCII
            // for (int i = 32; i <= 126; i++) { alphabet.add(i); }
            
            DFA.Transformation transformDFA = new DFA.Transformation();
            Dfa dfa = transformDFA.transformationToDFA(ndfa);
            
            System.out.println("DFA créé avec succès !");
            System.out.println("État initial DFA: " + dfa.etatInitial.id);
            System.out.println("États finaux DFA: " + dfa.etatsFinaux.size());
            System.out.println("Nombre total d'états DFA: " + dfa.etats.size());
            System.out.println();
            
            // ==================== ÉTAPE 4 : DFA → DFA Minimal ====================
            System.out.println("=== ÉTAPE 4 : Minimisation du DFA ===");
            try {
                Minimisation minimiseur = new Minimisation();
                long startTime = System.currentTimeMillis();
                Dfa dfaMinimal = minimiseur.minimiser(dfa);
                long endTime = System.currentTimeMillis();
                System.out.println("Temps de minimisation: " + (endTime - startTime) + "ms");
                
                System.out.println("DFA minimal créé avec succès !");
                System.out.println("État initial DFA minimal: " + dfaMinimal.etatInitial.id);
                System.out.println("États finaux DFA minimal: " + dfaMinimal.etatsFinaux.size());
                System.out.println("Nombre total d'états DFA minimal: " + dfaMinimal.etats.size());
                System.out.println();
                
                // ==================== TESTS ====================
                System.out.println("=== TESTS de reconnaissance ===");
                testerAutomate(dfaMinimal, "a");   // Devrait être accepté
                testerAutomate(dfaMinimal, "//");    // Devrait être accepté  
                testerAutomate(dfaMinimal, "");  // ❌  devrait  être accepté
                testerAutomate(dfaMinimal, "aa");  // ❌ Ne devrait pas être accepté
                testerAutomate(dfaMinimal, "ba");  // ❌ Ne devrait pas être accepté
                
            } catch (Exception e) {
                System.out.println("❌ ERREUR lors de la minimisation: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    	// Méthode pour tester si l'automate accepte une chaîne
    public static void testerAutomate(Dfa dfa, String chaine) {
        try {
            boolean accepte = simulerDFA(dfa, chaine);
            System.out.println("Chaîne '" + chaine + "' : " + (accepte ? "✅ ACCEPTÉE" : "❌ REJETÉE"));
        } catch (Exception e) {
            System.out.println("Erreur avec la chaîne '" + chaine + "': " + e.getMessage());
        }
    }
    
    // Simulation d'un DFA
    public static boolean simulerDFA(Dfa dfa, String input) {
        Dfa.Etat etatCourant = dfa.etatInitial;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Dfa.Etat suivant = etatCourant.obtenirTransition((int)c);
            
            if (suivant == null) {
                return false; // Aucune transition → rejet
            }
            etatCourant = suivant;
        }
        
        return dfa.etatsFinaux.contains(etatCourant);
    
    }
    
}