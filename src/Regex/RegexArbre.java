package Regex;

import java.util.ArrayList;

public class RegexArbre {

  
  /* La structure de l'arbre */
  protected int root; // le Noeud //
  protected ArrayList<RegexArbre> sousArbre; // le reste de l'arbre //

 
  // initialisation de l'arbre avec un caractère seulement 
  public RegexArbre(char c) {
	    this.root = (int) c;
	    this.sousArbre = new ArrayList<>();
	}
  
  // initialisation de l'arbre avec une regex complex
  public RegexArbre(int root,ArrayList<RegexArbre> sousArbre ) {
	  this.root = root;
	  this.sousArbre=sousArbre;
  }
  
  public ArrayList<RegexArbre> getSousArbre() {
	  return this.sousArbre;
  }
  
  public RegexArbre getFilsDroite() {
	  if(sousArbre.size()<=1) {
		  return null;
	  }
	  return this.sousArbre.get(1);
  }
  
  public RegexArbre getFilsGauche() {
	  if(sousArbre.isEmpty()) {
		  return null;
	  }
	  return this.sousArbre.get(0);
  }
  
  
  public int getRoot() {
	  return this.root;
  }
  
  
  /* Cette méthode est utilisé pour l'affichage */
  public String rootToString() {
      if (root == RegexParseur.CONCAT)
          return ".";
      if (root == RegexParseur.ETOILE)
          return "*";
      if (root == RegexParseur.PLUS) 
      return "+"; 
      if (root == RegexParseur.ALTERN)
          return "|";
      if (root == RegexParseur.DOT)
          return ".";
      return Character.toString((char) root);
  }
  
  
  public String afficherNotationPrefixe() {
	    StringBuilder result = new StringBuilder();
	    afficherPrefixeRecursif(result);
	    return result.toString();
	}
  

	private void afficherPrefixeRecursif(StringBuilder result) {
	    // D'ABORD ajouter l'opérateur ou le caractère courant
	    result.append(rootToString()).append("\n");
	    
	    // PUIS ajouter récursivement les enfants
	    for (RegexArbre enfant : sousArbre) {
	        enfant.afficherPrefixeRecursif(result);
	    }
	}
  
  

}
