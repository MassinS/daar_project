package Etude;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class EtudeSomePatterns {	
    
    public static void main(String[] args) {
        
        try {
            String text = RechercheDFA.chargerTexte("Samples/56667-0.txt");
            List<String> patterns = Files.readAllLines(Paths.get("Samples/patterns.txt"));
            
            List<ResultatBenchmark> resultats = new ArrayList<>();
            
            List<String> nomsPatterns = new ArrayList<>();
            List<Long> tempsAutomate = new ArrayList<>();
            List<Long> tempsKMP = new ArrayList<>();
            List<Long> tempsEgrep = new ArrayList<>();
            
            for (String pattern : patterns) {
                System.out.println(" Test du pattern: " + pattern);
                nomsPatterns.add(pattern);
                
                ResultatBenchmark resAutomate = EtudeBenchmark.benchmarkAutomate(pattern, text);
                ResultatBenchmark resKMP = EtudeBenchmark.benchmarkKMP(pattern, text);
                ResultatBenchmark resEgrep = EtudeBenchmark.benchmarkEgrep(pattern, "Samples/56667-0.txt");
                 
                resultats.add(resAutomate);
                resultats.add(resKMP);
                resultats.add(resEgrep);
                
                tempsAutomate.add((long)resAutomate.tempsMoyen);
                tempsKMP.add((long)resKMP.tempsMoyen);
                tempsEgrep.add((long)resEgrep.tempsMoyen);
                
                EtudeBenchmark.afficherComparaison(resAutomate, resKMP, resEgrep);
            }
             
            EtudeBenchmark.genererRapportFinal(resultats,"Result/Result_performance_KMP_automate_egrep/Benchmark_performance_KMP_automate_egrep.csv");
            
            genererGraphique(nomsPatterns, tempsAutomate, tempsKMP, tempsEgrep);
            
        } catch (Exception e) {
            System.err.println(" Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void genererGraphique(List<String> patterns, List<Long> automate, List<Long> kmp, List<Long> egrep) {
        try {
        	
            int width = 1200;
            int height = 600;
            int padding = 80;
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            long maxTemps = Math.max(
                Collections.max(automate),
                Math.max(Collections.max(kmp), Collections.max(egrep))
            );
            
            int graphWidth = width - 2 * padding;
            int graphHeight = height - 2 * padding;
            int pointCount = patterns.size();
            
            g2d.setColor(Color.BLACK);
            g2d.drawLine(padding, height - padding, width - padding, height - padding); // Axe X
            g2d.drawLine(padding, padding, padding, height - padding); // Axe Y
            
            for (int i = 0; i <= 10; i++) {
                int y = height - padding - (i * graphHeight / 10);
                long value = i * maxTemps / 10;
                g2d.drawString(value + "ms", 5, y + 5);
                g2d.drawLine(padding - 5, y, padding, y);
            }
            
            Color[] couleurs = {Color.RED, Color.BLUE, Color.GREEN};
            String[] noms = {"Automate", "KMP", "Egrep"};
            List<List<Long>> toutesDonnees = Arrays.asList(automate, kmp, egrep);
            
            for (int methode = 0; methode < 3; methode++) {
                g2d.setColor(couleurs[methode]);
                List<Long> donnees = toutesDonnees.get(methode);
                
                g2d.fillRect(width - 150, 50 + methode * 30, 20, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString(noms[methode], width - 120, 60 + methode * 30);
                g2d.setColor(couleurs[methode]);
                
                for (int i = 0; i < pointCount; i++) {
                    int x = padding + (i * graphWidth / (pointCount - 1));
                    int y = height - padding - (int)(donnees.get(i) * graphHeight / maxTemps);
                    
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                    
                    if (i < pointCount - 1) {
                        int nextX = padding + ((i + 1) * graphWidth / (pointCount - 1));
                        int nextY = height - padding - (int)(donnees.get(i + 1) * graphHeight / maxTemps);
                        g2d.drawLine(x, y, nextX, nextY);
                    }
                }
            }
            
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < pointCount; i++) {
                int x = padding + (i * graphWidth / (pointCount - 1));
                String label = "P" + (i + 1); // P1, P2, P3...
                g2d.drawString(label, x - 10, height - padding + 20);
                
             
            }
            
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Performance de l'automate vs KMP vs egrep ", width / 2 - 150, 30);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Patterns", width / 2 - 20, height - 20);
            g2d.drawString("Temps (ms)", 10, height / 2);
            
            File output = new File("Result/Result_performance_KMP_automate_egrep/performance_graph.png");
            ImageIO.write(image, "png", output);
            System.out.println(" Graphique généré: Result/performance_automate_kmp_egrep_graph.png");
            
            g2d.dispose();
            
        } catch (Exception e) {
            System.err.println(" Erreur génération graphique: " + e.getMessage());
        }
    }
}