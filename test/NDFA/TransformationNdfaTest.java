package NDFA;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import Regex.RegexArbre;
import Regex.RegexParseur;

// Test unitaire pour le transformation en NDFA

public class TransformationNdfaTest {

	  private Transformation transform = new Transformation();

	    // =============================================
	    // TESTS DES CARACTÈRES SIMPLES
	    // =============================================
	  
	  
	@Test
    public void testCaractereSimple() throws Exception {
        
		RegexArbre arbre = new RegexArbre('a', new ArrayList<>());
        
        Ndfa ndfa = transform.ArbreToNdfa(arbre);
        
        assertNotNull(ndfa);
        assertNotNull(ndfa.etatInitial);
        assertNotNull(ndfa.etatFinal);
        assertNotEquals(ndfa.etatInitial, ndfa.etatFinal);
        
        assertEquals(1, ndfa.etatInitial.getTransitions('a').size());
        assertTrue(ndfa.etatInitial.getTransitions('a').contains(ndfa.etatFinal));
    }
	
	@Test
	public void testCaractereDot() throws Exception {
	   
	    RegexArbre arbre = new RegexArbre(RegexParseur.DOT, new ArrayList<>());
	    
	    Ndfa ndfa = transform.ArbreToNdfa(arbre);
	    
	    assertNotNull(ndfa);
	    
	    // Tester que DOT matche tous les caractères SAUF \n (10) et \r (13)
	    for (int i = 0; i <= 255; i++) {
	        boolean shouldMatch = (i != 10 && i != 13); // Votre logique
	        boolean actuallyMatches = ndfa.etatInitial.getTransitions(i).contains(ndfa.etatFinal);
	        
	        assertEquals(shouldMatch, actuallyMatches,
	            "DOT devrait " + (shouldMatch ? "matcher" : "NE PAS matcher") + 
	            " le caractère " + i + " ('" + (char)i + "')");
	    }
	}
	
    
	
	
	 
	@Test
	public void testConcatenation() throws Exception {
	    
		ArrayList<RegexArbre> enfants = new ArrayList<>();
    	
		
	    RegexArbre a = new RegexArbre('a', new ArrayList<>());
	    RegexArbre b = new RegexArbre('b', new ArrayList<>());
	    enfants.add(a);
	    enfants.add(b);
	    
	    RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
	        enfants);
	    
	    Ndfa ndfa = transform.ArbreToNdfa(concat);
	    
	    assertNotNull(ndfa);
	    assertNotNull(ndfa.etatInitial);
	    assertNotNull(ndfa.etatFinal);
	    
	    assertFalse(ndfa.etatInitial.getTransitions('a').isEmpty());
	    
	    assertNotEquals(ndfa.etatInitial, ndfa.etatFinal);
	    
	    boolean aDesEpsilonTransitions = !ndfa.etatInitial.getTransitionsEpsilon().isEmpty();
	    
	    for (Etat etat : ndfa.etatInitial.getTransitions('a')) {
	        if (!etat.getTransitionsEpsilon().isEmpty()) {
	            aDesEpsilonTransitions = true;
	            break;
	        }
	    }
	    
	    assertTrue(aDesEpsilonTransitions, "La concaténation doit utiliser des epsilon-transitions");
	}
	
	
	@Test
    public void testAlternativeCaracteresSpeciaux() throws Exception {
        
		ArrayList<RegexArbre> enfants = new ArrayList<>();
    	
		RegexArbre dollar = new RegexArbre('$', new ArrayList<>());
        RegexArbre pourcent = new RegexArbre('%', new ArrayList<>());
        enfants.add(pourcent);
        enfants.add(dollar);
        RegexArbre altern = new RegexArbre(RegexParseur.ALTERN, 
            enfants);
        
        
        Ndfa ndfa = transform.ArbreToNdfa(altern);
        
        assertNotNull(ndfa);
        
        assertEquals(2, ndfa.etatInitial.getTransitionsEpsilon().size());
        
        boolean dollarTrouve = false, pourcentTrouve = false;
        
        for (Etat etat : ndfa.etatInitial.getTransitionsEpsilon()) {
            if (!etat.getTransitions('$').isEmpty()) dollarTrouve = true;
            if (!etat.getTransitions('%').isEmpty()) pourcentTrouve = true;
        }
        
        assertTrue(dollarTrouve && pourcentTrouve);
    }
	
	
	@Test
    public void testEtoileSurConcatenation() throws Exception {
      
		ArrayList<RegexArbre> enfants1 = new ArrayList<>();
		ArrayList<RegexArbre> enfants2 = new ArrayList<>();
    	
		RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        enfants1.add(a);
        enfants1.add(b);
        RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
            enfants1);
        enfants2.add(concat);
        RegexArbre etoile = new RegexArbre(RegexParseur.ETOILE, 
            enfants2);
         
        Ndfa ndfa = transform.ArbreToNdfa(etoile);
        
       
        assertNotNull(ndfa);
        
        assertEquals(2, ndfa.etatInitial.getTransitionsEpsilon().size());
        
        assertTrue(ndfa.etatInitial.getTransitionsEpsilon().contains(ndfa.etatFinal));
    }

	
	
}
