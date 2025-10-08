package Regex;

import java.util.ArrayList;

public class RegexParseur {

	/**
	 * Opérateur de concaténation implicite
	 * Valeur hexadécimale distinctive pour éviter les conflits avec les caractères ASCII
	 */
	public static final int CONCAT = 0xC04CA7;

	/**
	 * Opérateur de fermeture de Kleene (répétition zéro ou plusieurs fois)
	 * Représente l'étoile '*' dans les expressions régulières
	 */
	public static final int ETOILE = 0xE7011E;

	
	/**
	 * Répresente l'opérateur plus +
	*/
	public static final int PLUS = 0xCAFE01;
			
			
	/**
	 * Opérateur d'alternative (ou logique)
	 * Représente le symbole '|' dans les expressions régulières
	 */
	public static final int ALTERN = 0xA17E54;

	/**
	 * Marqueur de protection pour les sous-arbres parenthésés
	 * Permet d'isoler les expressions entre parenthèses lors du parsing
	 */
	/* 
	 *  Pendant le parsing : On emballe les expressions entre parenthèses dans du papier PROTECTION
        À la fin : On déballle les cadeaux pour voir le contenu directement
	 * 
	 * */
	public static final int PROTECTION = 0xBADDAD;

	/**
	 * Parenthèse ouvrant '(' 
	 * Délimiteur de début de groupe dans les expressions régulières
	 */
	public static final int PARENTHESEOUVRANT = 0x16641664;

	/**
	 * Parenthèse fermante ')'
	 * Délimiteur de fin de groupe dans les expressions régulières  
	 */
	public static final int PARENTHESEFERMANT = 0x51515151;

	/**
	 * Caractère universel (any character)
	 * Représente le point '.' qui correspond à n'importe quel caractère
	 */
	public static final int DOT = 0xD07;
	
    
    private static int charToRoot(char c) {
        if (c=='.') return DOT;
        if (c=='*') return ETOILE;
        if (c=='+') return PLUS; 
        if (c=='|') return ALTERN;
        if (c=='(') return PARENTHESEOUVRANT;
        if (c==')') return PARENTHESEFERMANT;
        return (int)c;
      }
    
    
    public static boolean estOperateur(RegexArbre arbre) {
     return arbre.root == CONCAT || 
        	arbre.root == ETOILE || 
        	arbre.root == ALTERN ||
        	arbre.root == PLUS   ||  
        	arbre.root == PROTECTION ||
        	arbre.root == PARENTHESEOUVRANT || 
        	arbre.root == PARENTHESEFERMANT || 
        	arbre.root == DOT;
    }
    
    
    
	
    public static RegexArbre parseur(String regex) throws Exception {
        ArrayList<RegexArbre> result = new ArrayList<>();  
        
        // Tokenisation avec gestion d'échappement
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            
            if (c == '\\') {
                // Gestion de l'échappement
                if (i + 1 >= regex.length()) {
                    throw new Exception("Caractère d'échappement '\\' en fin d'expression");
                }
                char nextChar = regex.charAt(i + 1);
                
                // Ajouter le caractère échappé comme un token littéral
                result.add(new RegexArbre((int) nextChar, new ArrayList<>()));
                i++; // Sauter le prochain caractère puisqu'on l'a déjà traité
            } else {
                result.add(new RegexArbre(charToRoot(c), new ArrayList<>()));
            }
        }
        
        try {
            return parseur(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    

   
   private static RegexArbre parseur(ArrayList<RegexArbre> tokens) throws Exception {
	
	 // ÉTAPE 1 : Parenthèses
	   
		/* La priorité la plus elevé dans un RegEx est les parentheses */
	   
	    /* Donc on va d'abord traiter les parentheses */
	   
	   while (contientParentheses(tokens)) {
	        tokens = traiterParentheses(tokens);
	    }	   
	    
	   // ÉTAPE 2 : Étoile
	    while (contientEtoile(tokens)  ) {
	        tokens = traiterEtoile(tokens);
	    }    
	    
	    // Etape 03  : le plus
	    
	    while(contientPlus(tokens)) {
	    	tokens = traiterPlus(tokens);
	    }
	    
	 // ÉTAPE 04 : Concaténation 
	 
	    while (contientConcat(tokens)) {
	        tokens = traiterConcat(tokens);
	    }  
	    
	 // ÉTAPE 05 : Alternative  
	    while (contientAltern(tokens)) {
	        tokens = traiterAltern(tokens);
	    }    
	    
	    
	 // Vérifier qu'il ne reste qu'un seul élément
	    if (tokens.size() != 1) {
	        throw new Exception("Expression invalide");
	    }
		   
	    
	 // NETTOYAGE FINAL : supprimer les protections
	    return removeProtection(tokens.get(0));
	    		
   	}

     
    private static boolean contientParentheses(ArrayList<RegexArbre> tokens) {
	
    for (RegexArbre t: tokens) if (t.root==PARENTHESEFERMANT || t.root==PARENTHESEOUVRANT) return true;
    	    return false;  
     }
	
    private static boolean contientEtoile(ArrayList<RegexArbre> tokens) {
		
	   	 for (RegexArbre t: tokens) if (t.root==ETOILE && t.sousArbre.isEmpty() ) return true;
	   	    return false;  
	    }
	
	private static boolean contientPlus(ArrayList<RegexArbre> tokens) {
	    for (RegexArbre t: tokens) if (t.root==PLUS && t.sousArbre.isEmpty()) return true;
	    return false;  
	}
	
	private static boolean contientConcat(ArrayList<RegexArbre> tokens) {
	    boolean firstFound = false;
	    for (RegexArbre t : tokens) {
	        if (!firstFound && t.root != ALTERN) {
	            firstFound = true;
	            continue;
	        }
	        if (firstFound) {
	            if (t.root != ALTERN) return true;
	            else firstFound = false;
	        }
	    }
	    return false;
	}
	
	private static boolean contientAltern(ArrayList<RegexArbre> tokens) {
		
	   	 for (RegexArbre t: tokens) if (t.root==ALTERN && t.sousArbre.isEmpty() ) return true;
	   	    return false;  
	    } 		
    
	private static ArrayList<RegexArbre> traiterParentheses(ArrayList<RegexArbre> tokens) throws Exception {
	    ArrayList<RegexArbre> result = new ArrayList<>();
	    boolean found = false;
	    
	    for (RegexArbre t : tokens) {
	        if (!found && t.root == PARENTHESEFERMANT) {
	            boolean done = false;
	            ArrayList<RegexArbre> content = new ArrayList<>();
	            
	            // Utiliser une pile pour trouver l'ouvrante correspondante
	            while (!done && !result.isEmpty()) {
	                if (result.get(result.size() - 1).root == PARENTHESEOUVRANT) {
	                    done = true;
	                    result.remove(result.size() - 1);
	                } else {
	                    content.add(0, result.remove(result.size() - 1)); // addFirst
	                }
	            }
	            
	            if (!done) throw new Exception("Parenthèse fermante sans ouvrante");
	            
	            found = true;
	            ArrayList<RegexArbre> subTrees = new ArrayList<>();
	            subTrees.add(parseur(content));
	            result.add(new RegexArbre(PROTECTION, subTrees));
	        } else {
	            result.add(t);
	        }
	    }
	    
	    if (!found) throw new Exception("Parenthèse ouvrante sans fermante");
	    return result;
	}
   
	
	
    	 
     /* cette fonction sert à vérifier est ce que les parentheses sont équilibré */
     /* Exemple : si on a 4 parenthese fermantes on doit avoir forcèment 4 parenthese ouverantes  */
     /* Cette fonction sera appelé lorsque on traitera toute les parenthese parce que on supprime les parenthese ouvert et ferme */
     /* Si on trouve un parenthese après la suppression des parenthese donc il doit y'avoir forcèment un diséquilibre */
     public static void verifierParenthesesEquilibrees(ArrayList<RegexArbre> tokens) throws Exception {
    	    for (RegexArbre token : tokens) {
    	        if (token.root == PARENTHESEOUVRANT) {
    	            throw new Exception("Parenthèse ouvrante '(' non fermée");
    	        }
    	    }
    	}    
     
     private static ArrayList<RegexArbre> traiterEtoile(ArrayList<RegexArbre> tokens) throws Exception {
 	    for (int i = 0; i < tokens.size(); i++) {
 	        if (tokens.get(i).root == ETOILE && tokens.get(i).sousArbre.isEmpty()) {
 	            return traiterEtoileAPosition(tokens, i);
 	        }
 	    }
 	    return tokens; // Aucune étoile trouvée
 	}

	private static ArrayList<RegexArbre> traiterEtoileAPosition(ArrayList<RegexArbre> tokens, int i) throws Exception {
		
		// Vérifier que l'étoile n'est pas en première position
		if (i == 0 ) {
			throw new Exception("Opérateur '*' en position initiale non autorisé");
		}
		
		 /* Prendre l'élément avant l'étoile       */
	    RegexArbre elementAvant = tokens.get(i - 1);
	    
	    /* Vérifications spécifiques               */
	    if (elementAvant.root == ETOILE) {
	        throw new Exception("Double étoile '**' non autorisée");
	    }
	    if (elementAvant.root == ALTERN) {
	        throw new Exception("Opérateur '*' ne peut pas suivre '|'");
	    }
	    if (elementAvant.root == PARENTHESEFERMANT || elementAvant.root == PARENTHESEOUVRANT) {
	        // Normalement, les parenthèses sont déjà traitées à cette étape
	        // Donc ce cas ne devrait pas se produire
	        throw new Exception("Erreur interne: parenthèse non traitée avant étoile");
	    }    

	 // Créer le nouveau nœud ETOILE
	    ArrayList<RegexArbre> enfants = new ArrayList<>();
	    enfants.add(elementAvant);
	    RegexArbre etoileNode = new RegexArbre(ETOILE, enfants);   
	    
	 // Remplacer les deux éléments par le nouveau nœud
	    tokens.remove(i);    // Supprimer l'étoile
	    tokens.remove(i-1);  // Supprimer l'élément avant
	    
	    tokens.add(i-1, etoileNode); // Ajouter ETOILE(élément)   
		
		
	    return tokens;
	}

	private static ArrayList<RegexArbre> traiterConcat(ArrayList<RegexArbre> tokens) throws Exception {
	    ArrayList<RegexArbre> result = new ArrayList<>();
	    boolean found = false;
	    boolean firstFound = false;
	    
	    for (RegexArbre t : tokens) {
	        if (!found && !firstFound && t.root != ALTERN) {
	            firstFound = true;
	            result.add(t);
	            continue;
	        }
	        if (!found && firstFound && t.root == ALTERN) {
	            firstFound = false;
	            result.add(t);
	            continue;
	        }
	        if (!found && firstFound) {
	            found = true;
	            RegexArbre last = result.remove(result.size() - 1);
	            ArrayList<RegexArbre> enfants = new ArrayList<>();
	            enfants.add(last);
	            enfants.add(t);
	            result.add(new RegexArbre(CONCAT, enfants));
	        } else {
	            result.add(t);
	        }
	    }
	    return result;
	}
	
	
	
	
	private static ArrayList<RegexArbre> traiterAltern(ArrayList<RegexArbre> tokens) throws Exception {
	    ArrayList<RegexArbre> result = new ArrayList<>();
	    boolean found = false;
	    RegexArbre gauche = null;
	    boolean done = false;
	    
	    for (RegexArbre t : tokens) {
	        if (!found && t.root == ALTERN && t.sousArbre.isEmpty()) {
	            if (result.isEmpty()) throw new Exception("Opérateur '|' sans opérande gauche");
	            found = true;
	            gauche = result.remove(result.size() - 1);
	            continue;
	        }
	        if (found && !done) {
	            if (gauche == null) throw new Exception("Erreur interne: opérande gauche manquant");
	            done = true;
	            ArrayList<RegexArbre> enfants = new ArrayList<>();
	            enfants.add(gauche);
	            enfants.add(t);
	            result.add(new RegexArbre(ALTERN, enfants));
	        } else {
	            result.add(t);
	        }
	    }
	    return result;
	}
	
	
	private static ArrayList<RegexArbre> traiterPlus(ArrayList<RegexArbre> tokens) throws Exception {
	    for (int i = 0; i < tokens.size(); i++) {
	        if (tokens.get(i).root == PLUS && tokens.get(i).sousArbre.isEmpty()) {
	            return traiterPlusAPosition(tokens, i);
	        }
	    }
	    return tokens;
	}
	
	
	private static ArrayList<RegexArbre> traiterPlusAPosition(ArrayList<RegexArbre> tokens, int i) throws Exception {
	   
		// Vérifier que le PLUS n'est pas en première position
	    if (i == 0) {
	        throw new Exception("Opérateur '+' en position initiale non autorisé");
	    }
	    
	    // Prendre l'élément avant le PLUS
	    RegexArbre elementAvant = tokens.get(i - 1);
	    
	    // Vérifications spécifiques
	    if (elementAvant.root == ETOILE || elementAvant.root == PLUS) {
	        throw new Exception("Double opérateur de répétition non autorisé");
	    }
	    if (elementAvant.root == ALTERN) {
	        throw new Exception("Opérateur '+' ne peut pas suivre '|'");
	    }
	    
	    // Créer le nouveau nœud PLUS
	    ArrayList<RegexArbre> enfants = new ArrayList<>();
	    enfants.add(elementAvant);
	    RegexArbre plusNode = new RegexArbre(PLUS, enfants);
	    
	    // Remplacer les deux éléments par le nouveau nœud
	    tokens.remove(i);    // Supprimer le +
	    tokens.remove(i-1);  // Supprimer l'élément avant
	    
	    tokens.add(i-1, plusNode); // Ajouter PLUS(élément)
	    
	    return tokens;
	}
	
	
	/**
	 * Supprime les nœuds PROTECTION inutiles de l'arbre syntaxique
	 * Un nœud PROTECTION avec un seul enfant est remplacé par cet enfant
	 */
	private static RegexArbre removeProtection(RegexArbre tree) throws Exception {
	    if (tree.root == PROTECTION && tree.sousArbre.size() != 1) {
	        throw new Exception("Nœud PROTECTION invalide");
	    }
	    if (tree.sousArbre.isEmpty()) {
	        return tree;
	    }
	    if (tree.root == PROTECTION) {
	        return removeProtection(tree.sousArbre.get(0));
	    }

	    // Appliquer récursivement aux enfants
	    ArrayList<RegexArbre> newSubTrees = new ArrayList<>();
	    for (RegexArbre child : tree.sousArbre) {
	        newSubTrees.add(removeProtection(child));
	    }
	    return new RegexArbre(tree.root, newSubTrees);
	}
	
	
}

	/*
Grammaire simplifiée des ERE (extrait)

Les opérateurs, par ordre de priorité (du plus prioritaire au moins) :

1) ( ... )

2) * (étoile de Kleene)

3) concaténation (implicite, pas d’opérateur visible)

4) | (alternative)

Exemple : a(b|c)* signifie : a concaténé avec (b|c)*.

	  
	  */

	
	
	
	
	/* L'arbre final de "a(b|c)*" */

/*

   CONCAT(
  Char('a')        // root = 97
  ETOILE(          // root = 0xE7011E
    ALTERN(        // root = 0xA17E54
      Char('b')    // root = 98
      Char('c')    // root = 99
          )
       )
    )      
  
  
*/





