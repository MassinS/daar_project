package Etude;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

public class EtudeAutomateEgrep {

	
public static void main(String[] args) {
        
	   		/*
	   		 * - Benchmark de patterns complexe pour tester la performance de Egrep et automate seulement 
	   		 * 
	   		 * - Les resultats sont stocké dans le dossier Result/Result_performance_automate_egrep
	   		 */
	
        try {
            String text = RechercheDFA.chargerTexte("Samples/56667-0.txt");
            List<String> patterns = Files.readAllLines(Paths.get("Samples/patternsComplex.txt"));
            
            List<ResultatBenchmark> resultats = new ArrayList<>();
            
            List<String> nomsPatterns = new ArrayList<>();
            List<Long> tempsAutomate = new ArrayList<>();
            List<Long> tempsEgrep = new ArrayList<>();
            
            for (String pattern : patterns) {
                System.out.println(" Test du pattern: " + pattern);
                nomsPatterns.add(pattern);
                
                ResultatBenchmark resAutomate = EtudeBenchmark.benchmarkAutomate(pattern, text);
                ResultatBenchmark resEgrep = EtudeBenchmark.benchmarkEgrep(pattern, "Samples/56667-0.txt");
                 
                resultats.add(resAutomate);
                resultats.add(resEgrep);
                
                tempsAutomate.add((long)resAutomate.temps);
                tempsEgrep.add((long)resEgrep.temps);
                
                EtudeBenchmark.afficherComparaison(resAutomate, resEgrep);
            }
             
            EtudeBenchmark.genererRapportFinal(resultats,"Result/Result_performance_automate_egrep/Benchmark_performance_automate_egrep.csv");
            
            genererGraphique(nomsPatterns, tempsAutomate, tempsEgrep);
            
        } catch (Exception e) {
            System.err.println(" Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
private static void genererGraphique(List<String> patterns, List<Long> automate, List<Long> egrep) {
    try {
        // Vérifications de base
        if (patterns == null || patterns.isEmpty() ||
            automate == null || automate.isEmpty() ||
            egrep == null || egrep.isEmpty()) {
            System.err.println(" Données insuffisantes pour générer le graphique.");
            return;
        }

        if (patterns.size() != automate.size() || patterns.size() != egrep.size()) {
            System.err.println(" Les listes patterns, automate et egrep doivent avoir la même taille.");
            return;
        }

        int pointCount = patterns.size();
        if (pointCount < 2) {
            System.err.println(" Au moins 2 points sont nécessaires pour tracer un graphique.");
            return;
        }

        // Paramètres du graphique
        int width = 1200;
        int height = 600;
        int padding = 80;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Trouver la valeur max
        long maxTemps = Math.max(Collections.max(automate), Collections.max(egrep));
        if (maxTemps == 0) maxTemps = 1; // éviter division par zéro

        int graphWidth = width - 2 * padding;
        int graphHeight = height - 2 * padding;

        // Axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // Axe X
        g2d.drawLine(padding, padding, padding, height - padding); // Axe Y

        // Graduation Y
        for (int i = 0; i <= 10; i++) {
            int y = height - padding - (i * graphHeight / 10);
            long value = i * maxTemps / 10;
            g2d.drawString(value + "ms", 5, y + 5);
            g2d.drawLine(padding - 5, y, padding, y);
        }

        // Légende + données
        Color[] couleurs = {Color.RED, Color.BLUE};
        String[] noms = {"Automate", "Egrep"};
        List<List<Long>> toutesDonnees = Arrays.asList(automate, egrep);

        for (int methode = 0; methode < toutesDonnees.size(); methode++) {
            g2d.setColor(couleurs[methode]);
            List<Long> donnees = toutesDonnees.get(methode);

            // Légende
            g2d.fillRect(width - 150, 50 + methode * 30, 20, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(noms[methode], width - 120, 60 + methode * 30);
            g2d.setColor(couleurs[methode]);

            // Tracer les points et lignes
            for (int i = 0; i < pointCount; i++) {
                int x = padding + (i * graphWidth / (pointCount - 1));
                int y = height - padding - (int) (donnees.get(i) * graphHeight / maxTemps);

                g2d.fillOval(x - 3, y - 3, 6, 6);

                if (i < pointCount - 1) {
                    int nextX = padding + ((i + 1) * graphWidth / (pointCount - 1));
                    int nextY = height - padding - (int) (donnees.get(i + 1) * graphHeight / maxTemps);
                    g2d.drawLine(x, y, nextX, nextY);
                }
            }
        }

        // Labels X
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < pointCount; i++) {
            int x = padding + (i * graphWidth / (pointCount - 1));
            String label = "P" + (i + 1);
            g2d.drawString(label, x - 10, height - padding + 20);
        }

        // Titres
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Performance de l'automate vs egrep", width / 2 - 150, 30);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Patterns", width / 2 - 20, height - 20);
        g2d.drawString("Temps (ms)", 10, height / 2);

        // Sauvegarde de l'image
        File output = new File("Result/Result_performance_automate_egrep/performance_egrep_automate_graph.png");
        ImageIO.write(image, "png", output);
        System.out.println(" Graphique généré: Result/Result_performance_automate_egrep/performance_egrep_automate_graph.png");

        g2d.dispose();

    } catch (Exception e) {
        System.err.println(" Erreur génération graphique: " + e.getMessage());
        e.printStackTrace();
    }
}


}
