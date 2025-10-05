package DFAMinimisaion;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import DFA.Dfa;
import Minimisation.Minimisation;
import NDFA.Transformation ;
import Regex.RegexArbre;
import Regex.RegexParseur;


public class DfaMinimisationTest {

	private Minimisation minimiseur;
    private Transformation transformNdfa;
    private DFA.Transformation transformDfa;
	
    @BeforeEach
    public void setUp() {
        minimiseur = new Minimisation();
        transformNdfa = new Transformation();
        transformDfa = new DFA.Transformation();
    }
	
	
    // =============================================
    // TESTS DE LA MÉTHODE calculerSignature
    // =============================================

    @Test
    public void testCalculerSignature_EtatsEquivalents() {
       
    	Dfa.Etat etat1 = new Dfa.Etat();
        Dfa.Etat etat2 = new Dfa.Etat();
        Dfa.Etat etatCible = new Dfa.Etat();
        
        etat1.ajouterTransition('a', etatCible);
        etat2.ajouterTransition('a', etatCible);
        
        Set<Set<Dfa.Etat>> partition = new HashSet<>();
        Set<Dfa.Etat> groupeCible = new HashSet<>();
        groupeCible.add(etatCible);
        partition.add(groupeCible);
        
        Set<Integer> alphabet = Set.of((int)'a', (int)'b');
        
        String signature1 = minimiseur.calculerSignature(etat1, partition, alphabet);
        String signature2 = minimiseur.calculerSignature(etat2, partition, alphabet);
        
        assertEquals(signature1, signature2);
    }
	
	
 // =============================================
    // TESTS DE MINIMISATION AVEC EXPRESSIONS SIMPLES
    // =============================================

    @Test
    public void testMinimisation_CaractereSimple() throws Exception {
        
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        Dfa dfaOriginal = creerDFA(a);
        
        Dfa dfaMinimal = minimiseur.minimiser(dfaOriginal);
        
        assertNotNull(dfaMinimal);
        assertEquals(2, dfaMinimal.etats.size());
        
        assertTrue(simulerDfa(dfaMinimal, "a"));
        assertFalse(simulerDfa(dfaMinimal, "b"));
        assertFalse(simulerDfa(dfaMinimal, ""));
    }
	
    @Test
    public void testMinimisation_EtoileDejaMinimale() throws Exception {
       
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre etoile = new RegexArbre(RegexParseur.ETOILE, 
            new ArrayList<>() {{ add(a); }});
        Dfa dfaOriginal = creerDFA(etoile);
        
        Dfa dfaMinimal = minimiseur.minimiser(dfaOriginal);
        
        assertNotNull(dfaMinimal);
        
        assertTrue(simulerDfa(dfaMinimal, ""));
        assertTrue(simulerDfa(dfaMinimal, "a"));
        assertTrue(simulerDfa(dfaMinimal, "aa"));
        assertFalse(simulerDfa(dfaMinimal, "b"));
    }
    
    // =============================================
    // TESTS DE MINIMISATION RÉELLE
    // =============================================

    @Test
    public void testMinimisation_ReductionEtats() throws Exception {
       
    	// "a|a" devrait se réduire à "a"
        RegexArbre a1 = new RegexArbre('a', new ArrayList<>());
        RegexArbre a2 = new RegexArbre('a', new ArrayList<>());
        RegexArbre altern = new RegexArbre(RegexParseur.ALTERN, 
            new ArrayList<>() {{ add(a1); add(a2); }});
        
        Dfa dfaOriginal = creerDFA(altern);
        int etatsOriginaux = dfaOriginal.etats.size();
        
        Dfa dfaMinimal = minimiseur.minimiser(dfaOriginal);
        
        assertNotNull(dfaMinimal);
        assertTrue(dfaMinimal.etats.size() <= etatsOriginaux);
        
        assertTrue(simulerDfa(dfaMinimal, "a"));
        assertFalse(simulerDfa(dfaMinimal, "b"));
        assertFalse(simulerDfa(dfaMinimal, "aa"));
    }

    
    @Test
    public void testMinimisation_ComportementIdentique() throws Exception {
       
    	RegexArbre a = new RegexArbre('a', new ArrayList<>());
        RegexArbre b = new RegexArbre('b', new ArrayList<>());
        RegexArbre concat = new RegexArbre(RegexParseur.CONCAT, 
            new ArrayList<>() {{ add(a); add(b); }});
        
        Dfa dfaOriginal = creerDFA(concat);
        
        Dfa dfaMinimal = minimiseur.minimiser(dfaOriginal);
        
        String[] chainesAcceptees = {"ab"};
        String[] chainesRejetees = {"", "a", "b", "ba", "abc"};
        
        for (String chaine : chainesAcceptees) {
            assertEquals(simulerDfa(dfaOriginal, chaine), simulerDfa(dfaMinimal, chaine),
                "Comportement différent pour: " + chaine);
        }
        
        for (String chaine : chainesRejetees) {
            assertEquals(simulerDfa(dfaOriginal, chaine), simulerDfa(dfaMinimal, chaine),
                "Comportement différent pour: " + chaine);
        }
    }
    
    
    
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
	
    private Dfa creerDFA(RegexArbre arbre) throws Exception {
        // Transformation complète : Regex → NDFA → DFA
        NDFA.Ndfa ndfa = transformNdfa.ArbreToNdfa(arbre);
        return transformDfa.transformationToDFA(ndfa);
    }
    
    
}
