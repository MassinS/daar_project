package Etude;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import DFA.Dfa;
import Minimisation.Minimisation;
import NDFA.Ndfa;
import NDFA.Transformation;
import Regex.RegexArbre;
import Regex.RegexParseur;

public class Test {


    private static Transformation transformNDFA = new Transformation();
    private static DFA.Transformation transformDFA = new DFA.Transformation();
    private static Minimisation minimiseur = new Minimisation();
    
	
	private static void debugSdotStarG() {
	    System.out.println("\n🎯 DEBUG S.*g");
	    
	    String pattern = "S.*g";
	    String fichier = "56667-0.txt";
	    
	    try {
	        // Charger le texte
	        String text = chargerTexte(fichier);
	        
	        System.out.println("🔍 Pattern: " + pattern);
	        System.out.println("📖 Texte chargé: " + text.length() + " caractères");
	        
	        // 1. Trouver les matches avec egrep
	        System.out.println("\n🐧 RECHERCHE AVEC EGREP:");
	        Set<String> matchesEgrep = getMatchesEgrep(pattern, fichier);
	        System.out.println("Egrep trouve: " + matchesEgrep.size() + " matches");
	        
	        // 2. Trouver les matches avec votre automate
	        System.out.println("\n🤖 RECHERCHE AVEC AUTOMATE:");
	        Set<String> matchesAutomate = getMatchesAutomate(pattern, text);
	        System.out.println("Automate trouve: " + matchesAutomate.size() + " matches");
	        
	        // 3. Comparer les différences
	        System.out.println("\n🔍 COMPARAISON DÉTAILLÉE:");
	        comparerMatches(matchesEgrep, matchesAutomate);
	        
	        // 4. Analyser les matches manquants
	        System.out.println("\n🔎 ANALYSE DES MATCHES MANQUANTS:");
	        analyserMatchesManquants(matchesEgrep, matchesAutomate, text);
	        
	    } catch (Exception e) {
	        System.err.println("❌ Erreur: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private static Set<String> getMatchesEgrep(String pattern, String fichier) throws Exception {
	    Set<String> matches = new HashSet<>();
	    
	    ProcessBuilder pb = new ProcessBuilder("egrep", "-o", pattern, fichier);
	    Process process = pb.start();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    
	    String line;
	    while ((line = reader.readLine()) != null) {
	        matches.add(line);
	    }
	    process.waitFor();
	    
	    return matches;
	}

	private static Set<String> getMatchesAutomate(String pattern, String text) throws Exception {
	    Set<String> matches = new HashSet<>();
	    
	    RegexArbre arbre = RegexParseur.parseur(pattern);
	    Ndfa ndfa = transformNDFA.ArbreToNdfa(arbre);
	    Dfa dfa = transformDFA.transformationToDFA(ndfa);
	    Dfa dfaMinimal = minimiseur.minimiser(dfa);
	    
	    // Version modifiée de rechercherAvecDFA qui collecte les matches
	    int index = 0;
	    int n = text.length();
	    
	    while (index < n) {
	        Dfa.Etat currentState = dfaMinimal.etatInitial;
	        int start = index;
	        int currentIndex = index;
	        int lastMatchEnd = -1;
	        
	        while (currentIndex < n && currentState != null) {
	            char currentChar = text.charAt(currentIndex);
	            Dfa.Etat nextState = currentState.obtenirTransition((int)currentChar);
	            
	            if (nextState == null) break;
	            
	            currentState = nextState;
	            currentIndex++;
	            
	            if (dfaMinimal.etatsFinaux.contains(currentState)) {
	                lastMatchEnd = currentIndex;
	            }
	        }
	        
	        if (lastMatchEnd != -1) {
	            String match = text.substring(start, lastMatchEnd);
	            matches.add(match);
	            index = lastMatchEnd;
	        } else {
	            index++;
	        }
	    }
	    
	    return matches;
	}

	private static void comparerMatches(Set<String> egrep, Set<String> automate) {
	    System.out.println("Matches dans Egrep mais PAS dans Automate:");
	    for (String match : egrep) {
	        if (!automate.contains(match)) {
	            System.out.println("  ❌ Manquant: '" + match + "' (longueur: " + match.length() + ")");
	        }
	    }
	    
	    System.out.println("Matches dans Automate mais PAS dans Egrep:");
	    for (String match : automate) {
	        if (!egrep.contains(match)) {
	            System.out.println("  ❌ Supplémentaire: '" + match + "' (longueur: " + match.length() + ")");
	        }
	    }
	}

	private static void analyserMatchesManquants(Set<String> egrep, Set<String> automate, String text) {
	    for (String matchEgrep : egrep) {
	        if (!automate.contains(matchEgrep)) {
	            System.out.println("\n🔍 Analyse du match manquant: '" + matchEgrep + "'");
	            
	            // Trouver toutes les occurrences de ce match dans le texte
	            int index = 0;
	            while ((index = text.indexOf(matchEgrep, index)) != -1) {
	                System.out.println("  Position: " + index);
	                
	                // Afficher le contexte
	                int start = Math.max(0, index - 5);
	                int end = Math.min(text.length(), index + matchEgrep.length() + 5);
	                String contexte = text.substring(start, end)
	                    .replace("\n", "\\n")
	                    .replace("\r", "\\r");
	                System.out.println("  Contexte: ..." + contexte + "...");
	                
	                // Vérifier s'il y a des sauts de ligne dans le match
	                if (matchEgrep.contains("\n") || matchEgrep.contains("\r")) {
	                    System.out.println("  ⚠️  Le match CONTIENT des sauts de ligne!");
	                }
	                
	                index += matchEgrep.length();
	            }
	        }
	    }
	}
	
	public static void main (String [] args) {
		debugSdotStarG();
	}
	 private static String chargerTexte(String chemin) throws IOException {
	        return new String(Files.readAllBytes(Paths.get(chemin)));
	    }
	    
}
