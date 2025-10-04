package Regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

public class RegexParserTest {

	
	// Idée: 
    // 1.  Expression = "a|b|c"
    // 2.  Parser l'expression
    // 3. Assert : vérifier la structure de l'arbre
	
	
    // =============================================
    // TESTS DES CARACTÈRES SIMPLES
    // =============================================
	
	@Test 
	public void testParserCaractereSimple() throws Exception {
	
		String expression = "a"; 
	    RegexArbre arbre = RegexParseur.parseur(expression);
		assertEquals('a', arbre.getRoot()); 
		assertTrue(arbre.getSousArbre().isEmpty());
	}
	
	
	@Test
    public void testCaractereSpecial() throws Exception {
        RegexArbre arbre = RegexParseur.parseur(".");
        assertEquals(RegexParseur.DOT, arbre.getRoot());
        assertTrue(arbre.getSousArbre().isEmpty());
    }
	
	
    // =============================================
    // TESTS DE CONCATÉNATION
    // =============================================
	
	@Test
    public void testConcatenationSimple() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("ab");
        assertEquals(RegexParseur.CONCAT, arbre.getRoot());
        assertEquals(2, arbre.getSousArbre().size());
        assertEquals('a', arbre.getSousArbre().get(0).getRoot());
        assertEquals('b', arbre.getSousArbre().get(1).getRoot());
    }
	
	@Test
    public void testConcatenationMultiple() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("abc");
        // Devrait être: CONCAT(CONCAT(a, b), c)
        assertEquals(RegexParseur.CONCAT, arbre.getRoot());
        assertEquals(2, arbre.getSousArbre().size());
        
        RegexArbre gauche = arbre.getSousArbre().get(0);
        assertEquals(RegexParseur.CONCAT, gauche.getRoot());
        assertEquals('a', gauche.getSousArbre().get(0).getRoot());
        assertEquals('b', gauche.getSousArbre().get(1).getRoot());
        
        assertEquals('c', arbre.getSousArbre().get(1).getRoot());
    }
	
	
	
	// =============================================
    // TESTS D'ALTERNATIVE
    // =============================================

    @Test
    public void testAlternativeSimple() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("a|b");
        assertEquals(RegexParseur.ALTERN, arbre.getRoot());
        assertEquals(2, arbre.getSousArbre().size());
        assertEquals('a', arbre.getSousArbre().get(0).getRoot());
        assertEquals('b', arbre.getSousArbre().get(1).getRoot());
    }
    
    @Test
    public void testAlternativeMultiple() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("a|b|c");
        // Devrait être: ALTERN(ALTERN(a, b), c)
        assertEquals(RegexParseur.ALTERN, arbre.getRoot());
        assertEquals(2, arbre.getSousArbre().size());
        
        RegexArbre gauche = arbre.getSousArbre().get(0);
        assertEquals(RegexParseur.ALTERN, gauche.getRoot());
        assertEquals('a', gauche.getSousArbre().get(0).getRoot());
        assertEquals('b', gauche.getSousArbre().get(1).getRoot());
        
        assertEquals('c', arbre.getSousArbre().get(1).getRoot());
    }
    
    @Test
    public void testAlternativeAvecConcatenation() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("ab|cd");
        // Devrait être: ALTERN(CONCAT(a,b), CONCAT(c,d))
        assertEquals(RegexParseur.ALTERN, arbre.getRoot());
        assertEquals(2, arbre.getSousArbre().size());
        
        RegexArbre gauche = arbre.getSousArbre().get(0);
        RegexArbre droite = arbre.getSousArbre().get(1);
        
        assertEquals(RegexParseur.CONCAT, gauche.getRoot());
        assertEquals(RegexParseur.CONCAT, droite.getRoot());
    }
    
    
    
    // =============================================
    // TESTS D'ÉTOILE DE KLEENE
    // =============================================
    
    @Test
    public void testEtoileSimple() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("a*");
        assertEquals(RegexParseur.ETOILE, arbre.getRoot());
        assertEquals(1, arbre.getSousArbre().size());
        assertEquals('a', arbre.getSousArbre().get(0).getRoot());
    }
    
    @Test
    public void testEtoileSurGroupe() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("(ab)*");
        assertEquals(RegexParseur.ETOILE, arbre.getRoot());
        assertEquals(1, arbre.getSousArbre().size());
        
        RegexArbre groupe = arbre.getSousArbre().get(0);
        assertEquals(RegexParseur.CONCAT, groupe.getRoot());
    }
    
    
    @Test
    public void testEtoileSurAlternative() throws Exception {
        RegexArbre arbre = RegexParseur.parseur("(a|b)*");
        assertEquals(RegexParseur.ETOILE, arbre.getRoot());
        assertEquals(1, arbre.getSousArbre().size());
        
        RegexArbre groupe = arbre.getSousArbre().get(0);
        assertEquals(RegexParseur.ALTERN, groupe.getRoot());
    }
    
	
    // =============================================
    // TESTS D'OPÉRATEUR PLUS
    // =============================================
	
	@Test 
	public void testPlusSimple() throws Exception {
		
		String expression ="a+";
		
		RegexArbre arbre = RegexParseur.parseur(expression);
		
		assertEquals(RegexParseur.PLUS,arbre.getRoot());
		assertEquals('a', arbre.getSousArbre().get(0).getRoot());
		
	}
	
	 @Test
	    public void testPlusSurGroupe() throws Exception {
	        RegexArbre arbre = RegexParseur.parseur("(ab)+");
	        assertEquals(RegexParseur.PLUS, arbre.getRoot());
	        assertEquals(1, arbre.getSousArbre().size());
	        assertEquals(RegexParseur.CONCAT, arbre.getSousArbre().get(0).getRoot());
	    }
	 
	 
	    // =============================================
	    // TESTS DE PARENTHÈSES
	    // =============================================
	 
	 
	 @Test
	    public void testParenthesesSimple() throws Exception {
	        RegexArbre arbre = RegexParseur.parseur("(a)");
	        // Après removeProtection, devrait retourner 'a' directement
	        assertEquals('a', arbre.getRoot());
	        assertTrue(arbre.getSousArbre().isEmpty());
	    }
	 
	 
	 @Test
	    public void testParenthesesComplexes() throws Exception {
	        RegexArbre arbre = RegexParseur.parseur("(a(b|c))");
	        // Devrait être: CONCAT(a, ALTERN(b,c))
	        assertEquals(RegexParseur.CONCAT, arbre.getRoot());
	        assertEquals(2, arbre.getSousArbre().size());
	        assertEquals('a', arbre.getSousArbre().get(0).getRoot());
	        assertEquals(RegexParseur.ALTERN, arbre.getSousArbre().get(1).getRoot());
	    }
	 
	 
	    // =============================================
	    // TESTS D'ÉCHAPPEMENT
	    // =============================================
	 
	 
	 @Test
	 public void testEchappementPoint() throws Exception {
	     RegexArbre arbre = RegexParseur.parseur("a\\.b");
	     
	     assertEquals(RegexParseur.CONCAT, arbre.getRoot());
	     assertEquals(2, arbre.getSousArbre().size()); // CONCAT a 2 enfants
	     
	     
	     RegexArbre concatInterne = arbre.getSousArbre().get(0);
	     assertEquals(RegexParseur.CONCAT, concatInterne.getRoot());
	     assertEquals(2, concatInterne.getSousArbre().size());
	     
	     assertEquals('a', concatInterne.getSousArbre().get(0).getRoot());
	     
	     assertEquals('.', concatInterne.getSousArbre().get(1).getRoot());
	     
	     assertEquals('b', arbre.getSousArbre().get(1).getRoot());
	 }
	 
	 
	 @Test
	 public void testEchappementPlus() throws Exception {
	     RegexArbre arbre = RegexParseur.parseur("a\\+b");
	     
	     // Vérifier la racine
	     assertEquals(RegexParseur.CONCAT, arbre.getRoot());
	     assertEquals(2, arbre.getSousArbre().size()); // CONCAT a 2 enfants
	     
	     // Premier enfant = CONCAT(a, +)
	     RegexArbre concatInterne = arbre.getSousArbre().get(0);
	     assertEquals(RegexParseur.CONCAT, concatInterne.getRoot());
	     assertEquals(2, concatInterne.getSousArbre().size());
	     
	     // Vérifier 'a'
	     assertEquals('a', concatInterne.getSousArbre().get(0).getRoot());
	     
	     // Vérifier le plus échappé (doit être '+' littéral, pas PLUS)
	     assertEquals('+', concatInterne.getSousArbre().get(1).getRoot());
	     
	     // Deuxième enfant = 'b'
	     assertEquals('b', arbre.getSousArbre().get(1).getRoot());
	 }
	 
	 @Test
	 public void testEchappementEtoile() throws Exception {
	     RegexArbre arbre = RegexParseur.parseur("a\\*b");
	     
	     assertEquals(RegexParseur.CONCAT, arbre.getRoot());
	     assertEquals(2, arbre.getSousArbre().size());
	     
	     RegexArbre concatInterne = arbre.getSousArbre().get(0);
	     assertEquals(RegexParseur.CONCAT, concatInterne.getRoot());
	     assertEquals(2, concatInterne.getSousArbre().size());
	     
	     assertEquals('a', concatInterne.getSousArbre().get(0).getRoot());
	     assertEquals('*', concatInterne.getSousArbre().get(1).getRoot()); // Étoile littérale
	     
	     assertEquals('b', arbre.getSousArbre().get(1).getRoot());
	 } 
	 
	
}
