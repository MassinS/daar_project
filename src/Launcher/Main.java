package Launcher;

import java.io.IOException;
import Etude.EtudeBenchmark;
import Etude.RechercheDFA;

public class Main {

     
	// Couleurs pour l'affichage
    public static final String RED = "\u001B[31m";
    public static final String BLUE = "\u001B[34m";
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\033[1m";
    
    
	public static void main (String [] args) {
		
		    System.out.println(BLUE + BOLD + "========================================" + RESET);
	        System.out.println(BLUE + BOLD + "   PROJET egrep clone - RECHERCHE DE MOTIFS" + RESET);
	        System.out.println(BLUE + BOLD + "========================================" + RESET);

	        if (args.length < 3) {
	            afficherAide();
	            return;
	        }
	        
	        String method = args[0].toLowerCase();
	        String pattern = args[1];
	        String filePath = args[2];
	        
	        try {
	            String text = RechercheDFA.chargerTexte(filePath);
	            System.out.println(" Fichier : " + filePath + " (" + text.length() + " caractères)");
	            System.out.println(" Pattern : " + pattern);
	            System.out.println(" Méthode : " + method.toUpperCase());
	            System.out.println();

	            switch (method) {
	                case "automate":
	                	EtudeBenchmark.benchmarkAutomate(pattern, text);
	                    break;
	                case "kmp":
	                	EtudeBenchmark.benchmarkKMP(pattern, text);
	                    break;
	                case "egrep":
	                	EtudeBenchmark.benchmarkEgrep(pattern, filePath);
	                    break;
	                case "compare":
	                    comparerToutesMethodes(pattern, text, filePath);
	                    break;
	                default:
	                    System.out.println(RED + " Méthode inconnue : " + method + RESET);
	                    afficherAide();
	            }

	        } catch (IOException e) {
	            System.err.println(RED + " Erreur de fichier : " + e.getMessage()  + RESET);
	        } catch (Exception e) {
	            System.err.println(RED + " Erreur : " + e.getMessage() + RESET);
	            e.printStackTrace();
	        }
	        
	}
	
	 private static void comparerToutesMethodes(String pattern, String text, String filePath) {
	        System.out.println(BLUE + BOLD + " COMPARAISON DES 3 MÉTHODES..." + RESET);
	        System.out.println();
	        
	        EtudeBenchmark.benchmarkAutomate(pattern, text);
	        System.out.println("---");
	        EtudeBenchmark.benchmarkKMP(pattern, text);
	        System.out.println("---");
	    	EtudeBenchmark.benchmarkEgrep(pattern, filePath);
            
	    }
	
	
	private static void afficherAide() {
		System.out.println("1)-Si vous utilisez le MakeFile essayez : make run ARG1=<method> "
				+ "ARG2=<pattern> ARG3=<path fichier>");
		System.out.println("Exemples:");
		System.out.println("make run ARG1=\"automate\"  ARG2=\"Sargon\" ARG3=\"Samples/56667-0.txt");
	    System.out.println();      
        System.out.println("2)-Si vous utiliser le fichier binaire essayez : java -jar binaire.jar <method> <pattern> <fichier>");
        System.out.println("Exemples:");
        System.out.println("  java -jar binaire.jar automate \"hello\" Samples/56667-0.txt");
        System.out.println();
        System.out.println("3)-Méthodes disponibles:");
        System.out.println("  automate  - Utilise l'automate fini");
        System.out.println("  kmp       - Utilise l'algorithme KMP");
        System.out.println("  egrep     - Utilise egrep (WSL)");
        System.out.println("  compare   - Compare les 3 méthodes");
        System.out.println();
        System.out.println("4)-Patterns complexes supportés: *, +, |, (), .");
    }
	
}
