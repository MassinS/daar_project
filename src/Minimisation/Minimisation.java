package Minimisation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import DFA.Dfa;

public class Minimisation {

	
	/* Pour la minimisation on a choisi d'utiliser l'algorithme Hopcroft */
	
	// les étapes de l'algorithme de Hopcoft :
	// 1. Division initiale : Séparer états finaux vs non finaux
	// 2. Raffinement itératif : Séparer les états qui ont des comportements différents
	// 3. Fusion : Regrouper les états équivalents	
	
	
	 public Dfa minimiser(Dfa dfa) {
		 
		
		 // Étape 1: Partition initiale - états finaux vs non finaux
	     
		 Set<Set<Dfa.Etat>> partition = new HashSet<>();
	        
	        Set<Dfa.Etat> finaux = new HashSet<>(dfa.etatsFinaux);
	        Set<Dfa.Etat> nonFinaux = new HashSet<>(dfa.etats);
	        nonFinaux.removeAll(finaux);
	       
	        if (!finaux.isEmpty()) partition.add(finaux);
	        if (!nonFinaux.isEmpty()) partition.add(nonFinaux);
	        
	        
	        // Étape 2: Raffinement de la partition
	        
	        Set<Integer> alphabet = new HashSet<>();
            for (int i = 0; i <= 256; i++) {
                alphabet.add(i);
            }
            
            
	        boolean changed;
	        do {
	            changed = false;
	            Set<Set<Dfa.Etat>> nouvellePartition = new HashSet<>();
	            
	            for (Set<Dfa.Etat> groupe : partition) {
	                if (groupe.size() == 1) {
	                    nouvellePartition.add(groupe);
	                    continue;
	                }
	                
	                Map<String, Set<Dfa.Etat>> signatures = new HashMap<>();
	                
	                
	                for (Dfa.Etat etat : groupe) {
	                    String signature = calculerSignature(etat, partition, alphabet);
	                    signatures.computeIfAbsent(signature, k -> new HashSet<>()).add(etat);
	                }
	                
	                nouvellePartition.addAll(signatures.values());
	            }
	            
	            if (!partition.equals(nouvellePartition)) {
	                changed = true; 
	                partition = nouvellePartition;
	            }
	        } while (changed);
	        
	            
	        
	        // Étape 3: Construire le DFA minimal
	        return construireDFAMinimal(dfa, partition, alphabet);
  
	            
	        }
		 

	
	 public String calculerSignature(Dfa.Etat etat, Set<Set<Dfa.Etat>> partition, 
             Set<Integer> alphabet) {
			
			StringBuilder signature = new StringBuilder();
			
			for (int symbole : alphabet) {
				
			  Dfa.Etat suivant = etat.obtenirTransition(symbole);
			 
			 if (suivant == null) {
			signature.append("N"); 
			     } 
			 else {

			for (Set<Dfa.Etat> p : partition) {
			if (p.contains(suivant)) {
			signature.append(p.hashCode());
			break;
				}
			 }
			 }
			 
			signature.append("|");
			
			}
			
			return signature.toString();
			
	
	}
	
	     private Dfa construireDFAMinimal(Dfa dfa, Set<Set<Dfa.Etat>> partition, Set<Integer> alphabet) {
	       
	    	 Map<Set<Dfa.Etat>, Dfa.Etat> groupeVersEtat = new HashMap<>();
	         
	         for (Set<Dfa.Etat> groupe : partition) {
	             groupeVersEtat.put(groupe, new Dfa.Etat());
	         }
	         
	         
	         Dfa.Etat etatInitialMinimal = null;
	         for (Set<Dfa.Etat> groupe : partition) {
	             if (groupe.contains(dfa.etatInitial)) {
	                 etatInitialMinimal = groupeVersEtat.get(groupe);
	                 break;
	             }
	         }
	         
	         Set<Dfa.Etat> etatsFinauxMinimal = new HashSet<>();
	         for (Set<Dfa.Etat> groupe : partition) {
	             for (Dfa.Etat etat : groupe) {
	                 if (dfa.etatsFinaux.contains(etat)) {
	                     etatsFinauxMinimal.add(groupeVersEtat.get(groupe));
	                     break;
	                 }
	             }
	         }
	         
	          for (Set<Dfa.Etat> groupe : partition) {
	             Dfa.Etat etatMinimal = groupeVersEtat.get(groupe);
	             
	             Dfa.Etat representant = groupe.iterator().next();
	             
	             for (int symbole : alphabet) {
	                 Dfa.Etat suivantOriginal = representant.obtenirTransition(symbole);
	                 if (suivantOriginal != null) {
	                 
	                	 for (Set<Dfa.Etat> groupeSuivant : partition) {
	                         if (groupeSuivant.contains(suivantOriginal)) {
	                             Dfa.Etat etatSuivantMinimal = groupeVersEtat.get(groupeSuivant);
	                             etatMinimal.ajouterTransition(symbole, etatSuivantMinimal);
	                             break;
	                         }
	                     }
	                 }
	             }
	         }
	         
	         Set<Dfa.Etat> etatsMinimal = new HashSet<>(groupeVersEtat.values());
	         
	         return new Dfa(etatInitialMinimal, etatsFinauxMinimal, etatsMinimal);
	     }

}

