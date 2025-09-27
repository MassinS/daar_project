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
  
  
  
  private String rootToString() {
	
	  return null;
	  
  }
  
  

}
