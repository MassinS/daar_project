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
  
  
  
  private String rootToString() {
	
	  return null;
	  
  }
  
  

}
