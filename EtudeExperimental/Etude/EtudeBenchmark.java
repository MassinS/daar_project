package Etude;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    
    public static void main(String[] args) {
        
        try {
            String text = chargerTexte("56667-0.txt");
            System.out.println(" Texte chargé: " + text.length() + " caractères\n");
            
            String[] patterns = {
                "Sargon",           // Simple
                "S(a|r)gon",        // Moyen  
                "S.*g",             // Moyen
                "\\S(a|g|r)+on",     // Complexe
                "S(.)*g"
            };
            
            List<ResultatBenchmark> resultats = new ArrayList<>();
            
            for (String pattern : patterns) {
                System.out.println("🔍 Test du pattern: " + pattern);
                
                ResultatBenchmark resAutomate = benchmarkAutomate(pattern, text);
                ResultatBenchmark resKMP = benchmarkKMP(pattern, text);
                ResultatBenchmark resEgrep = benchmarkEgrep(pattern, "56667-0.txt");
                
                resultats.add(resAutomate);
                resultats.add(resKMP);
                resultats.add(resEgrep);
                
                afficherComparaison(resAutomate, resKMP, resEgrep);
            }
            
            genererRapportFinal(resultats);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private static ResultatBenchmark benchmarkAutomate(String pattern, String text) {
        try {
            long debutTotal = System.currentTimeMillis();
            
            RegexArbre arbre = RegexParseur.parseur(pattern);
            Ndfa ndfa = transformNDFA.ArbreToNdfa(arbre);
            Dfa dfa = transformDFA.transformationToDFA(ndfa);
            Dfa dfaMinimal = minimiseur.minimiser(dfa);
            
            String[] lines = text.split("\n");
            int totalMatches = 0;
            for (String line : lines) {
                totalMatches += rechercherAvecDFA(line, dfaMinimal);
            }
            
            long finTotal = System.currentTimeMillis();
            long tempsTotal = finTotal - debutTotal;
            
            System.out.println("   🤖 Automate Time: " + tempsTotal + "ms - " + totalMatches + " matches");
            
            return new ResultatBenchmark(pattern, "Automate", tempsTotal, totalMatches);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur Automate: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Automate", -1, 0);
        }
    }
    
    
    private static int rechercherAvecDFA(String text, Dfa dfa) {
        int totalMatches = 0;
        int index = 0;
        int n = text.length();
        
        while (index < n) {
            Dfa.Etat currentState = dfa.etatInitial;
            int currentIndex = index;
            int lastMatchEnd = -1;
            
            // Chercher le match le plus LONG possible à partir de index
            while (currentIndex < n) {
                char currentChar = text.charAt(currentIndex);
                Dfa.Etat nextState = currentState.obtenirTransition((int)currentChar);
                
                if (nextState == null) {
                    break; // Aucune transition possible
                }
                
                currentState = nextState;
                currentIndex++;
                
                // Mémoriser la fin du dernier match trouvé (le plus long)
                if (dfa.etatsFinaux.contains(currentState)) {
                    lastMatchEnd = currentIndex;
                }
            }
            
            // Si on a trouvé un match
            if (lastMatchEnd != -1) {
                totalMatches++;
                // ⚡ CORRECTION : Avancer APRÈS la fin du match trouvé
                index = lastMatchEnd;
                
                // Debug optionnel
                String match = text.substring(index - (lastMatchEnd - index), lastMatchEnd);
                System.out.println("   ✅ Match #" + totalMatches + ": '" + match + 
                        "' positions "  + "-" + (lastMatchEnd - 1));
            } else {
                // Aucun match trouvé à partir de cette position, avancer d'un caractère
                index++;
            }
        }
        
        return totalMatches;
    }
    
    private static ResultatBenchmark benchmarkKMP(String pattern, String text) {
        // ⚡ VÉRIFIER SI LE PATTERN EST SUPPORTÉ PAR KMP
        if (pattern.contains("(") || pattern.contains(")") || pattern.contains("*") || 
            pattern.contains("|") || pattern.contains(".") || pattern.contains("+") ||
            pattern.contains("\\S") || pattern.contains("\\")) {
            
            System.out.println("   🔍 KMP: Pattern non supporté (regex complexe)");
            return new ResultatBenchmark(pattern, "KMP", -1, 0);
        }
        
        try {
            long debutTotal = System.currentTimeMillis();
            
            // ⚡ KMP SUR LE TEXTE ENTIER (PLUS EFFICACE)
            List<Integer> matches = KmpAlgorithm.KmpImplementation(text, pattern);
            int totalMatches = matches.size();
            
            long finTotal = System.currentTimeMillis();
            long tempsTotal = finTotal - debutTotal;
            
            System.out.println("   🔍 KMP Time: " + tempsTotal + "ms - " + totalMatches + " matches");
            
            return new ResultatBenchmark(pattern, "KMP", tempsTotal, totalMatches);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur KMP: " + e.getMessage());
            return new ResultatBenchmark(pattern, "KMP", -1, 0);
        }
    }
    
    
    private static ResultatBenchmark benchmarkEgrep(String pattern, String fichier) {
        try {
            long debut = System.nanoTime();

            ProcessBuilder pb = new ProcessBuilder("egrep", "-o", pattern, fichier);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // ⚡ CORRECTION: Compter CHAQUE LIGNE = CHAQUE OCCURRENCE
            int occurrenceCount = 0;
            while (reader.readLine() != null) {
                occurrenceCount++;
            }

            process.waitFor();

            long fin = System.nanoTime();
            long temps = (fin - debut) / 1_000_000;

            System.out.println("   🐧 Egrep Time: " + temps + "ms - " + occurrenceCount + " occurrences");

            return new ResultatBenchmark(pattern, "Egrep", temps, occurrenceCount);

        } catch (Exception e) {
            System.err.println("❌ Erreur egrep: " + e.getMessage());
            return new ResultatBenchmark(pattern, "Egrep", -1, 0);
        }
    }


    
    private static void afficherComparaison(ResultatBenchmark automate, ResultatBenchmark kmp, ResultatBenchmark egrep) {
        System.out.println("   📊 Résultats:");
        System.out.printf("   🤖 Automate: %.2fms - %d matches\n", automate.tempsMoyen, automate.nbMatches);
        System.out.printf("   🔍 KMP: %.2fms - %d matches\n", kmp.tempsMoyen, kmp.nbMatches);
        System.out.printf("   🐧 Egrep: %.2fms\n", egrep.tempsMoyen);
        
        // Comparaison Automate vs KMP
        if (automate.tempsMoyen > 0 && kmp.tempsMoyen > 0) {
            double ratioAK = automate.tempsMoyen / kmp.tempsMoyen;
            String gagnantAK = ratioAK > 1 ? "KMP" : "Automate";
            double facteurAK = ratioAK > 1 ? ratioAK : 1/ratioAK;
            System.out.printf("   ⚡ %s est %.2fx plus rapide que %s\n", 
                gagnantAK, facteurAK, gagnantAK.equals("KMP") ? "Automate" : "KMP");
        }
        
        // Comparaison Automate vs Egrep
        if (automate.tempsMoyen > 0 && egrep.tempsMoyen > 0) {
            double ratioAE = automate.tempsMoyen / egrep.tempsMoyen;
            String gagnantAE = ratioAE > 1 ? "Egrep" : "Automate";
            double facteurAE = ratioAE > 1 ? ratioAE : 1/ratioAE;
            System.out.printf("   ⚡ %s est %.2fx plus rapide que %s\n", 
                gagnantAE, facteurAE, gagnantAE.equals("Egrep") ? "Automate" : "Egrep");
        }
        
        System.out.println();
    }
    
    
    private static void genererCSV(List<ResultatBenchmark> resultats) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("benchmark_results.csv"));
            writer.write("pattern,methode,temps_ms,matches\n");
            
            for (ResultatBenchmark res : resultats) {
                // ⚡ FORMAT SIMPLE SANS DÉCIMALES
                writer.write(String.format("%s,%s,%d,%d\n",
                    res.pattern, res.methode, (int)res.tempsMoyen, res.nbMatches));
            }
            
            writer.close();
            System.out.println("💾 Fichier CSV généré: benchmark_results.csv");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur génération CSV: " + e.getMessage());
        }
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
            
            double tempsMoyen = resMethode.stream()
                .mapToDouble(r -> r.tempsMoyen)
                .average().orElse(0);
            
            System.out.printf("%s: %.2fms (sur %d patterns)\n", 
                methode, tempsMoyen, resMethode.size());
        }
        
        System.out.println("\n✅ ÉTUDE TERMINÉE - Voir benchmark_results.csv");
    }
    
    
    private static String chargerTexte(String chemin) throws IOException {
        return new String(Files.readAllBytes(Paths.get(chemin)));
    }
    
    
}

