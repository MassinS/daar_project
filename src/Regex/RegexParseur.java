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
	 * Opérateur d'alternative (ou logique)
	 * Représente le symbole '|' dans les expressions régulières
	 */
	public static final int ALTERN = 0xA17E54;

	/**
	 * Marqueur de protection pour les sous-arbres parenthésés
	 * Permet d'isoler les expressions entre parenthèses lors du parsing
	 */
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
        if (c=='|') return ALTERN;
        if (c=='(') return PARENTHESEOUVRANT;
        if (c==')') return PARENTHESEFERMANT;
        return (int)c;
      }
    
    
    
    public static boolean estOperateur(RegexArbre arbre) {
     return arbre.root == CONCAT || 
        	arbre.root == ETOILE || 
        	arbre.root == ALTERN || 
        	arbre.root == PROTECTION ||
        	arbre.root == PARENTHESEOUVRANT || 
        	arbre.root == PARENTHESEFERMANT || 
        	arbre.root == DOT;
    }
    
    
    
	
   public static RegexArbre parseur(String regex) throws Exception {
	   ArrayList<RegexArbre> result = new ArrayList<>();  
	   
	   for (int i = 0; i < regex.length(); i++) {
	     result.add(new RegexArbre(charToRoot(regex.charAt(i)), new ArrayList<>()));	
	 
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
	    
	 // ÉTAPE 3 : Concaténation 
	    while (contientConcat(tokens)) {
	        tokens = traiterConcat(tokens);
	    }
	    
	 // ÉTAPE 4 : Alternative  
	    while (contientAltern(tokens)) {
	        tokens = traiterAltern(tokens);
	    }
	    
	 // Vérifier qu'il ne reste qu'un seul élément
	    if (tokens.size() != 1) {
	        throw new Exception("Expression invalide");
	    }
		
	    
	    
	    return tokens.get(0);
	    
		
   	}


     
    private static boolean contientParentheses(ArrayList<RegexArbre> tokens) {
	
    for (RegexArbre t: tokens) if (t.root==PARENTHESEFERMANT || t.root==PARENTHESEOUVRANT) return true;
    	    return false;  
     }
    
	private static boolean contientEtoile(ArrayList<RegexArbre> tokens) {
		
   	 for (RegexArbre t: tokens) if (t.root==ETOILE && t.sousArbre.isEmpty() ) return true;
   	    return false;  
    }
	
	
	
	private static boolean contientConcat(ArrayList<RegexArbre> tokens) {
	    for (int i = 0; i < tokens.size() - 1; i++) {
	        RegexArbre current = tokens.get(i);
	        RegexArbre next = tokens.get(i + 1);
	        
	        if (current.root != ALTERN && next.root != ALTERN) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private static boolean contientAltern(ArrayList<RegexArbre> tokens) {
		
   	 for (RegexArbre t: tokens) if (t.root==ALTERN && t.sousArbre.isEmpty() ) return true;
   	    return false;  
    } 
	
	
    
   private static ArrayList<RegexArbre> traiterParentheses(ArrayList<RegexArbre> tokens) throws Exception {
	     
		 /* Lorsque on traite les parentheses , la première étape est de trouver la parenthese fermante */
		 
		 for (int i = 0; i < tokens.size(); i++) {
			    if (tokens.get(i).root == PARENTHESEFERMANT) {
			    	
			        // Parenthese trouvée ! On passera à la deuxième étape
			        return traiterParentheseAPosition(tokens, i);
			    
			    }
			}
		 
		 verifierParenthesesEquilibrees(tokens);
		 
		 return tokens;

     
     }
     private static ArrayList<RegexArbre> traiterParentheseAPosition(ArrayList<RegexArbre> tokens, int i) throws Exception {
		
		int positionFermante = i; 
		int positionOuvrante = -1;
		
		/* La deuxième étape est de trouver la parenthese ouverante  */
		
		/* On possède la position de la parenthese fermante , donc on commence de cette position et on remonte !!  */
		/* Pourquoi on commence pas par le début de 0 jusqu'a a trouver la première parenthese ??? */
		/* c'est parce que il peut y'avoir un cas  ou on aura une double parenthese comme : a((b|c))*              */
		
		for (int j = positionFermante - 1; j >= 0; j--) {
		    if (tokens.get(j).root == PARENTHESEOUVRANT) {
		    	positionOuvrante = j;
		        break;
		    }
		}
		
		if (positionOuvrante == -1) {
		    throw new Exception("Parenthèse fermante ')' sans parenthèse ouvrante '(' correspondante");
		}
		
		
		
		/* troisème étape est d'extraire tout ce qui est entre les deux parenthese */
		
		ArrayList<RegexArbre> contenu = new ArrayList<>();
		
		/* Extraire les tokens entre ( et  ) */
		for (int k = positionOuvrante + 1; k < positionFermante; k++) {
		    contenu.add(tokens.get(k));
		}
		
		/* si le contenu est vide donc on a une paire de parenthese vide par exemple : a()c*         */
		
		if (contenu.isEmpty()) {
		
			throw new Exception("Parenthèses vides '()' non autorisées");
		
		}
		
		
		/* étape 04 consiste à parser récursivement le contenu */
		
		RegexArbre sousArbre = parseur(contenu); // Appel récursif ! 
		
		
		
		/* l'étape 5 consiste à remplacer par PROTECTION(sous-arbre) */
		
		// 1. Supprimer tous les tokens de ( à )
		for (int m = positionFermante; m >= positionOuvrante; m--) {
		    tokens.remove(m);
		}
		
		// 2. Ajouter le nœud PROTECTION à la position de l'ancienne (
		ArrayList<RegexArbre> enfants = new ArrayList<>();
		enfants.add(sousArbre);
		RegexArbre protection = new RegexArbre(PROTECTION, enfants);

		tokens.add(positionOuvrante, protection);
		
		
		
		return tokens;
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
	    for (int i = 0; i < tokens.size() - 1; i++) {
	        RegexArbre current = tokens.get(i);
	        RegexArbre next = tokens.get(i + 1);
	        
	        // Deux éléments consécutifs sans | entre eux ?
	        if (current.root != ALTERN && next.root != ALTERN) {
	            return traiterConcatAPosition(tokens, i, i + 1);
	        }
	    }
	    return tokens;
	}
	
	
	
	
	private static ArrayList<RegexArbre> traiterConcatAPosition(ArrayList<RegexArbre> tokens, int posGauche, int posDroite) throws Exception {
	    // Vérifier que les positions sont valides
	    if (posGauche >= posDroite || posDroite >= tokens.size()) {
	        throw new Exception("Positions de concaténation invalides");
	    }
	    
	    // Récupérer les deux éléments à concaténer
	    RegexArbre gauche = tokens.get(posGauche);
	    RegexArbre droite = tokens.get(posDroite);
	    
	    // Créer le nœud CONCAT
	    ArrayList<RegexArbre> enfants = new ArrayList<>();
	    enfants.add(gauche);
	    enfants.add(droite);
	    RegexArbre concatNode = new RegexArbre(CONCAT, enfants);
	    
	    // Remplacer les deux éléments par le nœud CONCAT
	    tokens.remove(posDroite); // Supprimer d'abord l'élément de droite
	    tokens.remove(posGauche); // Puis l'élément de gauche
	    
	    tokens.add(posGauche, concatNode); // Ajouter CONCAT à la position gauche
	    
	    return tokens;
	}
	
	
	
	private static ArrayList<RegexArbre> traiterAltern(ArrayList<RegexArbre> tokens) throws Exception {
	
		
		 for (int i = 0; i < tokens.size(); i++) {
 	        if (tokens.get(i).root == ALTERN && tokens.get(i).sousArbre.isEmpty()) {
 	            return traiterAlternAPosition(tokens, i);
 	        }
 	    }
		 
 	    return tokens; // Aucune étoile trouvée
	
	}



	private static ArrayList<RegexArbre> traiterAlternAPosition(ArrayList<RegexArbre> tokens, int i) throws Exception {
		
		// Vérifier que | n'est pas en première ou dernière position
	    
		if (i == 0) {
	        throw new Exception("Opérateur '|' en position initiale non autorisé");
	    }
	    if (i == tokens.size() - 1) {
	        throw new Exception("Opérateur '|' en position finale non autorisé");
	    }
		
	 // Vérifier qu'il y a bien un élément avant et après
	    RegexArbre gauche = tokens.get(i - 1);
	    RegexArbre droite = tokens.get(i + 1);
	    
	    // Vérifier que les éléments autour ne sont pas des opérateurs problématiques
	    if (gauche.root == ALTERN || droite.root == ALTERN) {
	        throw new Exception("Double opérateur '|' non autorisé");
	    }
	    
	    // | ne peut pas être adjacent à d'autres opérateurs sauf protection
	    if (( estOperateur(gauche) && gauche.root != PROTECTION) || 
	        (estOperateur(droite) && droite.root != PROTECTION) ) {
	        throw new Exception("Opérateur '|' mal placé");
	    }
	    
	    
	    // Créer le nœud ALTERN avec gauche et droite
	    ArrayList<RegexArbre> enfants = new ArrayList<>();
	    enfants.add(gauche);
	    enfants.add(droite);
	    RegexArbre alternNode = new RegexArbre(ALTERN, enfants);
	    
	    // Supprimer les trois éléments : gauche, |, droite
	    tokens.remove(i + 1); // Supprimer l'élément de droite en premier
	    tokens.remove(i);     // Supprimer le |
	    tokens.remove(i - 1); // Supprimer l'élément de gauche
	    
	    // Ajouter le nouveau nœud ALTERN à la position de l'ancien gauche
	    tokens.add(i - 1, alternNode);
	    
	    
	    
	    
	    return tokens;
	    
	    
	
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





