package Etude;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import DFA.Dfa;

public class RechercheDFA {
	
	 public static int rechercherAvecDFA(String line, Dfa dfa, boolean afficherMatches) {
	        int matchesInLine = 0;
	        int lineLength = line.length();
	        
	        for (int start = 0; start < lineLength; start++) {
	            Dfa.Etat currentState = dfa.etatInitial;
	            int currentPos = start;
	            int lastMatchEnd = -1;
	            
	            while (currentPos < lineLength) {
	                char currentChar = line.charAt(currentPos);
	                Dfa.Etat nextState = currentState.obtenirTransition((int)currentChar);
	                
	                if (nextState == null) {
	                    break;
	                }
	                
	                currentState = nextState;
	                currentPos++;
	                
	                if (dfa.etatsFinaux.contains(currentState)) {
	                    lastMatchEnd = currentPos;
	                }
	            }
	            
	            if (lastMatchEnd != -1) {
	                matchesInLine++;
	                if (afficherMatches) {
	                    String match = line.substring(start, lastMatchEnd);
	                    System.out.println("      ✅ Match trouvé: '" + match + "'");
	                    System.out.println("         Ligne: \"" + line + "\"");
	                    System.out.println("         Position: " + start + "-" + (lastMatchEnd-1));
	                }
	                start = lastMatchEnd - 1;
	            }
	        }
	        
	        return matchesInLine;
	    }
	 
	
	public static String chargerTexte(String chemin) throws IOException {
        return new String(Files.readAllBytes(Paths.get(chemin)));
    }
	
	
}
