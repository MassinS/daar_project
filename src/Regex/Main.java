package Regex;


public class Main {
    public static void main(String[] args) {
        try {
            RegexArbre arbre = RegexParseur.parseur("(a|b)*abb");
            
            System.out.println("Notation préfixe:");
            System.out.println(arbre.afficherNotationPrefixe());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

