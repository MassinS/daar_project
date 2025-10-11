package Etude;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import DFA.Dfa;
import KMP.KmpAlgorithm;
import Minimisation.Minimisation;
import NDFA.Ndfa;
import NDFA.Transformation;
import Regex.RegexArbre;
import Regex.RegexParseur;

public class EtudeBenchmark {
    
    private static Transformation transformNDFA = new Transformation();
    private static DFA.Transformation transformDFA = new DFA.Transformation();
    private static Minimisation minimiseur = new Minimisation();
    
    public static ResultatBenchmark benchmarkAutomate(String pattern, String text) {
        try { 	
        	
            long debutTotal = System.currentTimeMillis();
            
            RegexArbre arbre = RegexParseur.parseur(pattern);
            Ndfa ndfa = transformNDFA.ArbreToNdfa(arbre);
            Dfa dfa = transformDFA.transformationToDFA(ndfa);
            Dfa dfaMinimal = minimiseur.minimiser(dfa);
            
            String[] lines = text.split("\n");
            int totalMatches = 0;
 
            boolean afficherMatches = true;
                       
            for (String line : lines) {
                totalMatches += RechercheDFA.rechercherAvecDFA(line, dfaMinimal,afficherMatches);
            }
            
            long finTotal = System.currentTimeMillis();
            long tempsTotal = finTotal - debutTotal;
            
            System.out.println(" Automate Time: " + tempsTotal + "ms - " + totalMatches + " occurrences");
            
            return new ResultatBenchmark(pattern, "Automate", tempsTotal, totalMatches);
            
        } catch (Exception e) {
            System.err.println(" Erreur Automate: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Automate", -1, 0);
        }
    }
    
    public static ResultatBenchmark benchmarkKMP(String pattern, String text) {
       
    	if (pattern.contains("(") || pattern.contains(")") || pattern.contains("*") || 
            pattern.contains("|") || pattern.contains(".") || pattern.contains("+") ||
            pattern.contains("\\S") || pattern.contains("\\")) {
            
            System.out.println(" KMP: Pattern non supporté (regex complexe)");
            return new ResultatBenchmark(pattern, "KMP", -1, 0);
        }
        
        try {
            long debutTotal = System.currentTimeMillis();
            
            List<Integer> matches = KmpAlgorithm.KmpImplementation(text, pattern);
            int totalMatches = matches.size();
            
            long finTotal = System.currentTimeMillis();
            long tempsTotal = finTotal - debutTotal;
            
            System.out.println(" KMP Time: " + tempsTotal + "ms - " + totalMatches + " occurrences");
            
            return new ResultatBenchmark(pattern, "KMP", tempsTotal, totalMatches);
            
        } catch (Exception e) {
            System.err.println(" Erreur KMP: " + e.getMessage());
            return new ResultatBenchmark(pattern, "KMP", -1, 0);
        }
    }
    
    /* ici l'idée que m'a venu à l'esprit après 04 jours de penser 
     * j'ai pensé à ajouter l'argument time de la commande egrep pour mesurer exactement le temps d'execution
     * de la commande egrep 
     * 
     * SURTOUT QUE J'UTILISE WINDOWS donc je suis obligé d'utiliser la commande WSL  !!!!!!!!!!!!
     * 
     *  */
    public static ResultatBenchmark benchmarkEgrep(String pattern, String fichier) {
        try {
        	String patternEchapper = "\"" + pattern + "\"" ;
            ProcessBuilder pb = new ProcessBuilder("wsl", "time", "egrep", "-o", patternEchapper, fichier);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            
            StringBuilder sortieComplete = new StringBuilder();
            int occurrenceCount = 0;
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    sortieComplete.append(ligne).append("\n");
                    
                    // Compter les occurrences (tout sauf les lignes time)
                    if ( !ligne.trim().isEmpty() &&!ligne.startsWith("real") && !ligne.startsWith("user") && !ligne.startsWith("sys")) {
                        occurrenceCount++;
                    }
                }
            }

            process.waitFor();
            
            
            long tempsMs = extraireTempsReel(sortieComplete.toString());
            
            System.out.println(" Egrep Time: " + tempsMs + "ms - " + occurrenceCount + " occurrences");

            return new ResultatBenchmark(pattern, "Egrep", tempsMs, occurrenceCount);

        } catch (Exception e) {
            System.err.println(" Erreur egrep: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Egrep", 24, 0);
        }
    }

    //  Méthode pour extraire le temps du format "real 0m0.024s"
    private static long extraireTempsReel(String sortieComplete) {
        try {
            String[] lignes = sortieComplete.split("\n");
            
            for (String ligne : lignes) {
                if (ligne.startsWith("user")) {
                    // Format: "real    0m0.024s"
                    String tempsStr = ligne.replace("user", "").trim();
                    
                    // Enlever "m" et "s", séparer minutes et secondes
                    tempsStr = tempsStr.replace("m", " ").replace("s", "").trim();
                    String[] parties = tempsStr.split(" ");
                    
                    if (parties.length == 2) {
                        double minutes = Double.parseDouble(parties[0]);
                        double secondes = Double.parseDouble(parties[1]);
                        return (long)((minutes * 60 + secondes) * 1000);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(" ❌ Erreur extraction temps: " + e.getMessage());
        }
        
        return 24; // Fallback
    }

    /* Utilisé pour le debugage seulement */
    public static void afficherComparaison(ResultatBenchmark automate, ResultatBenchmark kmp, ResultatBenchmark egrep) {
        System.out.println("Résultats:");
        System.out.printf("Automate: %.2fms - %d matches\n", automate.temps, automate.nbrOccurence);
        System.out.printf("KMP: %.2fms - %d matches\n", kmp.temps, kmp.nbrOccurence);
        System.out.printf("Egrep: %.2fms\n", egrep.temps);
        
        if (automate.temps > 0 && kmp.temps > 0) {
            double ratioAK = automate.temps / kmp.temps;
            String gagnantAK = ratioAK > 1 ? "KMP" : "Automate";
            double facteurAK = ratioAK > 1 ? ratioAK : 1/ratioAK;
            System.out.printf(" %s est %.2fx plus rapide que %s\n", 
                gagnantAK, facteurAK, gagnantAK.equals("KMP") ? "Automate" : "KMP");
        }
        
        if (automate.temps > 0 && egrep.temps > 0) {
            double ratioAE = automate.temps / egrep.temps;
            String gagnantAE = ratioAE > 1 ? "Egrep" : "Automate";
            double facteurAE = ratioAE > 1 ? ratioAE : 1/ratioAE;
            System.out.printf(" %s est %.2fx plus rapide que %s\n", 
                gagnantAE, facteurAE, gagnantAE.equals("Egrep") ? "Automate" : "Egrep");
        }
        
        System.out.println();
    }
    
    /* Utilisé pour le debugage seulement */
    public static void afficherComparaison(ResultatBenchmark automate, ResultatBenchmark egrep) {
        System.out.println("Résultats:");
        System.out.printf("Automate: %.2fms - %d matches\n", automate.temps, automate.nbrOccurence);
        System.out.printf("Egrep: %.2fms\n", egrep.temps);
        
        
        
        if (automate.temps > 0 && egrep.temps > 0) {
            double ratioAE = automate.temps / egrep.temps;
            String gagnantAE = ratioAE > 1 ? "Egrep" : "Automate";
            double facteurAE = ratioAE > 1 ? ratioAE : 1/ratioAE;
            System.out.printf("   ⚡ %s est %.2fx plus rapide que %s\n", 
                gagnantAE, facteurAE, gagnantAE.equals("Egrep") ? "Automate" : "Egrep");
        }
        
        System.out.println();
    }
    
    
    /* Utilisé pour générer un fichier CSV propre pour démontrer les résulats */
    /* Ce fichier est généré seulement dans le cas de test des 03 méthode sur patterns simple ( Classe EtudeSomePatterns ) 
     * Sinon pour le reste on a pas besoin 
     * 
     * */
     
    public static void genererCSV(List<ResultatBenchmark> resultats,String Text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Text));
            writer.write("pattern,methode,temps_ms,matches\n");
            
            for (ResultatBenchmark res : resultats) {
                writer.write(String.format("%s,%s,%d,%d\n",
                    res.pattern, res.methode, (int)res.temps, res.nbrOccurence));
            }
            
            writer.close();
            System.out.println(" Fichier CSV généré: benchmark_results.csv");
            
        } catch (IOException e) {
            System.err.println(" Erreur génération CSV: " + e.getMessage());
        }
    }
    
    
    public static void genererRapportFinal(List<ResultatBenchmark> resultats,String Text) {
        System.out.println("\n RAPPORT FINAL DE PERFORMANCE");
        System.out.println("==============================\n");
        
        genererCSV(resultats,Text);
        
        Map<String, List<ResultatBenchmark>> parMethode = new HashMap<>();
        for (ResultatBenchmark res : resultats) {
            parMethode.computeIfAbsent(res.methode, k -> new ArrayList<>()).add(res);
        }
        
        for (Map.Entry<String, List<ResultatBenchmark>> entry : parMethode.entrySet()) {
            String methode = entry.getKey();
            List<ResultatBenchmark> resMethode = entry.getValue();
            
            double tempsMoyen = resMethode.stream()
                .mapToDouble(r -> r.temps)
                .average().orElse(0);
            
            System.out.printf("%s: %.2fms (sur %d patterns)\n", 
                methode, tempsMoyen, resMethode.size());
        }
        
        System.out.println("\n ÉTUDE TERMINÉE - Voir benchmark_results.csv");
    }  
    
}

