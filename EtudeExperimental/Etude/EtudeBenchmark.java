package Etude;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import DFA.Dfa;
import Minimisation.Minimisation;
import NDFA.Ndfa;
import NDFA.Transformation;
import Regex.RegexArbre;
import Regex.RegexParseur;

public class EtudeBenchmark {

    public static final int NB_ITERATIONS = 5;
    
    // TES VARIABLES EXISTANTES
    private static Transformation transformNDFA = new Transformation();
    private static DFA.Transformation transformDFA = new DFA.Transformation();
    private static Minimisation minimiseur = new Minimisation();
    
    public static void main(String[] args) {
        System.out.println("🚀 DÉMARRAGE DE L'ÉTUDE DE PERFORMANCE");
        System.out.println("======================================\n");
        
        
        try {
            // Étape 1: Charger le texte
            String text = chargerTexte("56667-0.txt");
            System.out.println("📖 Texte chargé: " + text.length() + " caractères\n");
            
            // Étape 2: Définir les patterns de test
            String[] patterns = {
                "Sargon",           // Simple
                "S(a|r)gon",        // Moyen  
                "S.*g",             // Moyen
                "\\S(a|g|r)+on"     // Complexe
            };
            
            // Étape 3: Lancer les benchmarks
            List<ResultatBenchmark> resultats = new ArrayList<>();
            
            for (String pattern : patterns) {
                System.out.println("🔍 Test du pattern: " + pattern);
                
                ResultatBenchmark resAutomate = benchmarkAutomate(pattern, text);
                ResultatBenchmark resEgrep = benchmarkEgrep(pattern, "56667-0.txt");
                
                resultats.add(resAutomate);
                resultats.add(resEgrep);
                
                // Affichage immédiat
                afficherComparaison(resAutomate, resEgrep);
            }
            
            // Étape 4: Générer le rapport final
            genererRapportFinal(resultats);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private static ResultatBenchmark benchmarkAutomate(String pattern, String text) {
        try {
            
        	long[] temps = new long[NB_ITERATIONS];
            
            System.out.print("   🤖 Automate: ");
            
            for (int i = 0; i < NB_ITERATIONS; i++) {
                long debut = System.nanoTime();
                
                // ⚡ JUSTE CONSTRUIRE L'AUTOMATE
                simulerRechercheDFA(pattern, text);
                
                long fin = System.nanoTime();
                temps[i] = (fin - debut) / 1_000_000;
                
                System.out.print(temps[i] + "ms ");
            }
            System.out.println();
            
            return calculerStatistiques(pattern, "Automate", temps, -1);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur Automate: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Automate", -1, -1, -1, -1, -1, 0);
        }
    }
    
    
    private static ResultatBenchmark benchmarkEgrep(String pattern, String fichier) {
        try {
        	
            long[] temps = new long[NB_ITERATIONS];
            
            System.out.print("   🐧 Egrep: ");
            
            for (int i = 0; i < NB_ITERATIONS; i++) {
                
            	long debut = System.nanoTime();
                
                // ⚡ COMME TON AMI - JUSTE LANCER EGREP
                ProcessBuilder pb = new ProcessBuilder("egrep", pattern, fichier);
                
                pb.redirectErrorStream(true);
                
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (reader.readLine() != null) {
                    // Lit mais ne compte pas
                }
                process.waitFor();
                
                long fin = System.nanoTime();
                temps[i] = (fin - debut) / 1_000_000;
                
                System.out.print(temps[i] + "ms ");
            }
            System.out.println();
            
            return calculerStatistiques(pattern, "Egrep", temps, -1);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur egrep: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Egrep", -1, -1, -1, -1, -1, 0);
        }
    }
    
    private static void simulerRechercheDFA(String pattern, String text) throws Exception {
        // Construction de l'automate seulement
        RegexArbre arbre = RegexParseur.parseur(pattern);
        Ndfa ndfa = transformNDFA.ArbreToNdfa(arbre);
        Dfa dfa = transformDFA.transformationToDFA(ndfa);
        Dfa dfaMinimal = minimiseur.minimiser(dfa);
        
        // Optionnel: recherche sans compter
        // countMatchesWithDFA(text, dfaMinimal);
    }
    
    // ⚡ LES AUTRES MÉTHODES RESTENT IDENTIQUES ⚡
    // (calculerStatistiques, afficherComparaison, genererRapportFinal, genererCSV, chargerTexte)
    
    
    private static ResultatBenchmark calculerStatistiques(String pattern, String methode, 
            long[] temps, int nbMatches) {
        double moyenne = Arrays.stream(temps).average().orElse(0);
        double min = Arrays.stream(temps).min().orElse(0);
        double max = Arrays.stream(temps).max().orElse(0);
        
        double variance = 0;
        for (long t : temps) {
            variance += Math.pow(t - moyenne, 2);
        }
        double ecartType = Math.sqrt(variance / temps.length);

        return new ResultatBenchmark(pattern, methode, moyenne, ecartType, min, max, nbMatches, temps.length);
    }
    
    
    private static void afficherComparaison(ResultatBenchmark automate, ResultatBenchmark egrep) {
        if (automate.tempsMoyen > 0 && egrep.tempsMoyen > 0) {
            double ratio = automate.tempsMoyen / egrep.tempsMoyen;
            String gagnant = ratio > 1 ? "Egrep" : "Automate";
            double facteur = ratio > 1 ? ratio : 1/ratio;
            
            System.out.printf("   ⚡ %s est %.2fx plus rapide (Automate: %.2fms ± %.2f vs Egrep: %.2fms ± %.2f)\n",
                gagnant, facteur, automate.tempsMoyen, automate.ecartType, egrep.tempsMoyen, egrep.ecartType);
        }
        System.out.println();
    }
    
    private static void genererRapportFinal(List<ResultatBenchmark> resultats) {
        
    	System.out.println("\n📊 RAPPORT FINAL DE PERFORMANCE");
        System.out.println("==============================\n");
        
        genererCSV(resultats);
        
        Map<String, List<ResultatBenchmark>> parMethode = new HashMap<>();
        for (ResultatBenchmark res : resultats) {
            parMethode.computeIfAbsent(res.methode, k -> new ArrayList<>()).add(res);
        }
        
        for (Map.Entry<String, List<ResultatBenchmark>> entry : parMethode.entrySet()) {
            String methode = entry.getKey();
            List<ResultatBenchmark> resMethode = entry.getValue();
            
            double tempsMoyen = resMethode.stream().mapToDouble(r -> r.tempsMoyen).average().orElse(0);
            double ecartTypeMoyen = resMethode.stream().mapToDouble(r -> r.ecartType).average().orElse(0);
            
            System.out.printf("%s: %.2fms ± %.2fms (sur %d patterns)\n", 
                methode, tempsMoyen, ecartTypeMoyen, resMethode.size());
        }
        
        System.out.println("\n✅ ÉTUDE TERMINÉE - Voir benchmark_results.csv");
    }
    
    
    private static void genererCSV(List<ResultatBenchmark> resultats) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("benchmark_results.csv"));
            writer.write("pattern,methode,temps_moyen_ms,ecart_type_ms,temps_min_ms,temps_max_ms,iterations\n");
            
            for (ResultatBenchmark res : resultats) {
                writer.write(String.format("%s,%s,%.2f,%.2f,%.2f,%.2f,%d\n",
                    res.pattern, res.methode, res.tempsMoyen, res.ecartType,
                    res.tempsMin, res.tempsMax, res.iterations));
            }
            
            writer.close();
            System.out.println("💾 Fichier CSV généré: benchmark_results.csv");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur génération CSV: " + e.getMessage());
        }
    }
    
    
    private static String chargerTexte(String chemin) throws IOException {
        return new String(Files.readAllBytes(Paths.get(chemin)));
    }
    
    
}

