package DFA;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.ArrayList;

import NDFA.Ndfa;
import Regex.RegexArbre;
import Regex.RegexParseur;


public class TransformationDfaTest {
    private DFA.Transformation transformDfa;
    private NDFA.Transformation transformNdfa;

    @BeforeEach
    public void setUp() {
        transformDfa = new DFA.Transformation();
        transformNdfa = new NDFA.Transformation();
    }

    // =============================================
    // TESTS DE LA MÉTHODE epsilonClosure
    // =============================================

    @Test
    public void testEpsilonClosure_SansEpsilon() {
        
    	NDFA.Etat etat1 = new NDFA.Etat();
        NDFA.Etat etat2 = new NDFA.Etat();
        
        Set<NDFA.Etat> etatsInitiaux = Set.of(etat1);
        
        Set<NDFA.Etat> fermeture = transformDfa.epsilonClosure(etatsInitiaux);
        
        
        assertEquals(1, fermeture.size());
        assertTrue(fermeture.contains(etat1));
    }
	
    @Test
    public void testEpsilonClosure_AvecEpsilon() {
        
        NDFA.Etat etat1 = new NDFA.Etat();
        NDFA.Etat etat2 = new NDFA.Etat();
        NDFA.Etat etat3 = new NDFA.Etat();
        
       
        etat1.ajouterTransition(etat2);
        etat2.ajouterTransition(etat3);
        
        Set<NDFA.Etat> etatsInitiaux = Set.of(etat1);
        

        Set<NDFA.Etat> fermeture = transformDfa.epsilonClosure(etatsInitiaux);
        
        
        assertEquals(3, fermeture.size());
        assertTrue(fermeture.contains(etat1));
        assertTrue(fermeture.contains(etat2));
        assertTrue(fermeture.contains(etat3));
    }
    
    
    
    // =============================================
    // TESTS DE LA MÉTHODE move
    // =============================================

    @Test
    public void testMoveTransitionsSimples() {
       
    	NDFA.Etat etat1 = new NDFA.Etat();
        NDFA.Etat etat2 = new NDFA.Etat();
        NDFA.Etat etat3 = new NDFA.Etat();
        
        etat1.ajouterTransition('a', etat2);
        etat2.ajouterTransition('a', etat3);
        
        Set<NDFA.Etat> etats = Set.of(etat1, etat2);
        
       
        Set<NDFA.Etat> result = transformDfa.move(etats, 'a');
        
        assertEquals(2, result.size());
        assertTrue(result.contains(etat2));
        assertTrue(result.contains(etat3));
    }

    
    
    // =============================================
    // TESTS DE TRANSFORMATION COMPLÈTE
    // =============================================

    @Test
    public void testTransformationCaractereSimple() throws Exception {
       
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        Ndfa ndfa = transformNdfa.ArbreToNdfa(a);
        
       
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        assertNotNull(dfa.etatInitial);
        assertEquals(1, dfa.etatsFinaux.size());
        
        Dfa.Etat suivant = dfa.etatInitial.obtenirTransition('a');
        assertNotNull(suivant);
        assertTrue(dfa.etatsFinaux.contains(suivant));
    }
    
    
    @Test
    public void testTransformationConcatenation() throws Exception {
        RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
            new ArrayList<>() {{ add(a); add(b); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(concat);
        
      
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        
        assertTrue(simulerDfa(dfa, "ab"));
        assertFalse(simulerDfa(dfa, "a"));
        assertFalse(simulerDfa(dfa, "b"));
        assertFalse(simulerDfa(dfa, ""));
    }
    
    
    @Test
    public void testTransformationAlternative() throws Exception {
        RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        RegexArbre altern = new RegexArbre(RegexParseur.ALTERN, 
            new ArrayList<>() {{ add(a); add(b); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(altern);
        
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        
        assertTrue(simulerDfa(dfa, "a"));
        assertTrue(simulerDfa(dfa, "b"));
        assertFalse(simulerDfa(dfa, "ab"));
        assertFalse(simulerDfa(dfa, ""));
    }
    
    @Test
    public void testTransformationEtoile() throws Exception {
      
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre etoile = new RegexArbre(RegexParseur.ETOILE, 
            new ArrayList<>() {{ add(a); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(etoile);
        
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        
        assertTrue(simulerDfa(dfa, ""));
        assertTrue(simulerDfa(dfa, "a"));
        assertTrue(simulerDfa(dfa, "aa"));
        assertTrue(simulerDfa(dfa, "aaa"));
        assertFalse(simulerDfa(dfa, "b"));
    }

    @Test
    public void testTransformationPlus() throws Exception {
       
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre plus = new RegexArbre(RegexParseur.PLUS, 
            new ArrayList<>() {{ add(a); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(plus);
        
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        
        assertFalse(simulerDfa(dfa, ""));
        assertTrue(simulerDfa(dfa, "a"));
        assertTrue(simulerDfa(dfa, "aa"));
        assertTrue(simulerDfa(dfa, "aaa"));
        assertFalse(simulerDfa(dfa, "b"));
    }
    
    
    @Test
    public void testTransformationDot() throws Exception {
      
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre dot = new RegexArbre(RegexParseur.DOT, new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        
        RegexArbre concat1 = new RegexArbre(RegexParseur.CONCAT, 
            new ArrayList<>() {{ add(a); add(dot); }});
        RegexArbre concat2 = new RegexArbre(RegexParseur.CONCAT, 
            new ArrayList<>() {{ add(concat1); add(b); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(concat2);
        
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        assertNotNull(dfa);
        
        assertTrue(simulerDfa(dfa, "a b"));
        assertTrue(simulerDfa(dfa, "acb"));
        assertTrue(simulerDfa(dfa, "a1b"));
        assertFalse(simulerDfa(dfa, "ab")); // manque le caractère du milieu
        assertFalse(simulerDfa(dfa, "ac")); // manque le 'b' final
    }
    
    // =============================================
    // Des tests pour vérifier si la transformation en DFA donne un automate déterministe
    // =============================================
    
    @Test
    public void testDfaDeterminisme() throws Exception {
        
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        Ndfa ndfa = transformNdfa.ArbreToNdfa(a);
        
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        for (Dfa.Etat etat : dfa.etats) {
            Dfa.Etat suivant = etat.obtenirTransition('a');
            if (suivant != null) {
                assertTrue(dfa.etats.contains(suivant));
            }
        }
    }
    
    @Test
    public void testDeterminismePasDeChoixMultiples() throws Exception {
       
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('a', new ArrayList<>()); // Même caractère !
        RegexArbre altern = new RegexArbre(RegexParseur.ALTERN, 
            new ArrayList<>() {{ add(a); add(b); }});
        
        Ndfa ndfa = transformNdfa.ArbreToNdfa(altern);
        Dfa dfa = transformDfa.transformationToDFA(ndfa);
        
        Dfa.Etat etatInitial = dfa.etatInitial;
        Dfa.Etat transitionA = etatInitial.obtenirTransition('a');
        
        assertNotNull(transitionA);
        
        assertEquals(transitionA, etatInitial.obtenirTransition('a'));
        assertEquals(transitionA, etatInitial.obtenirTransition('a')); // Encore
    }
    
    
    // =============================================
    // Méthode pour tester une chaine de caractère dans l'automate
    // =============================================

    private boolean simulerDfa(Dfa dfa, String input) {
        Dfa.Etat etatCourant = dfa.etatInitial;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Dfa.Etat suivant = etatCourant.obtenirTransition((int)c);
            
            if (suivant == null) {
                return false; 
            }
            etatCourant = suivant;
        }
        
        return dfa.etatsFinaux.contains(etatCourant);
    }

	
}
