package NDFA;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import Regex.RegexArbre;
import Regex.RegexParseur;

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
        
        for (int i = 0; i <= 256; i++) {
            assertTrue(ndfa.etatInitial.getTransitions(i).contains(ndfa.etatFinal),
                "DOT doit avoir une transition pour le caractère " + i);
        }
    }
	
	
    
	
	
	 
	@Test
	public void testConcatenation() throws Exception {
	    // Arrange
	    RegexArbre a = new RegexArbre('a', new ArrayList<>());
	    RegexArbre b = new RegexArbre('b', new ArrayList<>());
	    RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
	        new ArrayList<>() {{ add(a); add(b); }});
	    
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
        
		RegexArbre dollar = new RegexArbre('$', new ArrayList<>());
        RegexArbre pourcent = new RegexArbre('%', new ArrayList<>());
        RegexArbre altern = new RegexArbre(RegexParseur.ALTERN, 
            new ArrayList<>() {{ add(dollar); add(pourcent); }});
        
        
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
      
		RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
            new ArrayList<>() {{ add(a); add(b); }});
        RegexArbre etoile = new RegexArbre(RegexParseur.ETOILE, 
            new ArrayList<>() {{ add(concat); }});
        
        Ndfa ndfa = transform.ArbreToNdfa(etoile);
        
       
        assertNotNull(ndfa);
        
        assertEquals(2, ndfa.etatInitial.getTransitionsEpsilon().size());
        
        assertTrue(ndfa.etatInitial.getTransitionsEpsilon().contains(ndfa.etatFinal));
    }

	
	
}
