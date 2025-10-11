package Etude;

import java.awt.BasicStroke;
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

public class EtudeSomeFiles {

	 public static void main(String[] args) {
	        try {
	            // Fichiers de tailles croissantes
	            List<String> fichiers = Arrays.asList(
	                "Samples/petit.txt",      // Environ 100KB
	                "Samples/moyen.txt",      // Environ 500KB  
	                "Samples/grand.txt"       // Environ 1MB
	            );
	            
	            List<String> patterns = Files.readAllLines(Paths.get("Samples/patterns.txt"));
	            
	            // Vérifier que les fichiers existent
	            for (String fichier : fichiers) {
	                if (!Files.exists(Paths.get(fichier))) {
	                    System.err.println("❌ Fichier manquant: " + fichier);
	                    return;
	                }
	            }
	            
	            for (String pattern : patterns) {
	          
	            	genererGraphiquePattern(pattern, fichiers);
	            }
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	    
	    private static void genererGraphiquePattern(String pattern, List<String> fichiers) {
	        try {
	            // Collecter les données
	            List<Long> tempsAutomate = new ArrayList<>();
	            List<Long> tempsKMP = new ArrayList<>();
	            List<Long> tempsEgrep = new ArrayList<>();
	            List<String> nomsFichiers = new ArrayList<>();
	            List<Long> taillesFichiers = new ArrayList<>();
	            
	            for (String fichier : fichiers) {
	                System.out.println("   📁 Fichier: " + fichier);
	                
	                String text = RechercheDFA.chargerTexte(fichier);
	                long taille = Files.size(Paths.get(fichier));
	                
	                ResultatBenchmark resAutomate = EtudeBenchmark.benchmarkAutomate(pattern, text);
	                ResultatBenchmark resKMP = EtudeBenchmark.benchmarkKMP(pattern, text);
	                ResultatBenchmark resEgrep = EtudeBenchmark.benchmarkEgrep(pattern, fichier);
	                
	                tempsAutomate.add((long)resAutomate.tempsMoyen);
	                tempsKMP.add((long)resKMP.tempsMoyen);
	                tempsEgrep.add((long)resEgrep.tempsMoyen);
	                nomsFichiers.add(new File(fichier).getName());
	                taillesFichiers.add(taille / 1024); // Taille en KB
	            }
	            
	            // Générer le graphique
	            genererDiagrammeBaton(pattern, nomsFichiers, taillesFichiers, tempsAutomate, tempsKMP, tempsEgrep);
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur pour le pattern " + pattern + ": " + e.getMessage());
	        }
	    }
	    
	    private static void genererDiagrammeBaton(String pattern, List<String> nomsFichiers, 
	                                            List<Long> taillesFichiers, 
	                                            List<Long> tempsAutomate, List<Long> tempsKMP, List<Long> tempsEgrep) {
	        try {
	            int width = 1000;
	            int height = 700;
	            int padding = 100;
	            int legendeHeight = 80;
	            
	            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	            Graphics2D g2d = image.createGraphics();
	            
	            // Configuration
	            g2d.setColor(Color.WHITE);
	            g2d.fillRect(0, 0, width, height);
	            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            
	            // Calcul des dimensions
	            int graphWidth = width - 2 * padding;
	            int graphHeight = height - 2 * padding - legendeHeight;
	            int nbFichiers = nomsFichiers.size();
	            
	            // Trouver le temps maximum pour l'échelle Y
	            long maxTemps = Math.max(
	                Collections.max(tempsAutomate),
	                Math.max(Collections.max(tempsKMP), Collections.max(tempsEgrep))
	            );
	            maxTemps = Math.max(maxTemps, 10L); 
	            
	            // Couleurs
	            Color[] couleurs = {new Color(255, 99, 132),   // Rouge
	                              new Color(54, 162, 235),    // Bleu  
	                              new Color(75, 192, 192)};   // Vert
	            
	            String[] methodes = {"Automate", "KMP", "Egrep"};
	            
	            // Dessiner les axes
	            g2d.setColor(Color.BLACK);
	            g2d.setStroke(new BasicStroke(2));
	            g2d.drawLine(padding, height - padding - legendeHeight, width - padding, height - padding - legendeHeight); // Axe X
	            g2d.drawLine(padding, padding, padding, height - padding - legendeHeight); // Axe Y
	            
	            // Échelle Y
	            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
	            for (int i = 0; i <= 10; i++) {
	                int y = height - padding - legendeHeight - (i * graphHeight / 10);
	                long value = i * maxTemps / 10;
	                g2d.drawString(value + "ms", padding - 50, y + 5);
	                g2d.setColor(Color.LIGHT_GRAY);
	                g2d.drawLine(padding, y, width - padding, y);
	                g2d.setColor(Color.BLACK);
	            }
	            
	            // Configuration des barres
	            int groupeWidth = graphWidth / nbFichiers;
	            int barWidth = groupeWidth / 4; // 3 barres + espace
	            int espace = barWidth / 3;
	            
	            // Dessiner les barres pour chaque fichier
	            for (int i = 0; i < nbFichiers; i++) {
	                int baseX = padding + i * groupeWidth + espace;
	                
	                // Barre Automate
	                int barHeightAuto = (int)((double)tempsAutomate.get(i) / maxTemps * graphHeight);
	                g2d.setColor(couleurs[0]);
	                g2d.fillRect(baseX, height - padding - legendeHeight - barHeightAuto, barWidth, barHeightAuto);
	                
	                // Barre KMP
	                int barHeightKMP = (int)((double)tempsKMP.get(i) / maxTemps * graphHeight);
	                g2d.setColor(couleurs[1]);
	                g2d.fillRect(baseX + barWidth + espace, height - padding - legendeHeight - barHeightKMP, barWidth, barHeightKMP);
	                
	                // Barre Egrep
	                int barHeightEgrep = (int)((double)tempsEgrep.get(i) / maxTemps * graphHeight);
	                g2d.setColor(couleurs[2]);
	                g2d.fillRect(baseX + 2 * (barWidth + espace), height - padding - legendeHeight - barHeightEgrep, barWidth, barHeightEgrep);
	                
	                // Valeurs sur les barres
	                g2d.setColor(Color.BLACK);
	                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
	                
	                // Automate
	                g2d.drawString(tempsAutomate.get(i) + "ms", 
	                    baseX - 10, height - padding - legendeHeight - barHeightAuto - 5);
	                // KMP
	                g2d.drawString(tempsKMP.get(i) + "ms", 
	                    baseX + barWidth + espace - 10, height - padding - legendeHeight - barHeightKMP - 5);
	                // Egrep
	                g2d.drawString(tempsEgrep.get(i) + "ms", 
	                    baseX + 2 * (barWidth + espace) - 10, height - padding - legendeHeight - barHeightEgrep - 5);
	                
	                String infoFichier = nomsFichiers.get(i) + " (" + taillesFichiers.get(i) + " KB)";
	                g2d.setFont(new Font("Arial", Font.BOLD, 12));
	                g2d.drawString(infoFichier, baseX - 20, height - padding - legendeHeight + 20);
	            }
	            
	            int legendeY = height - legendeHeight + 20;
	            g2d.setFont(new Font("Arial", Font.BOLD, 14));
	            g2d.drawString("Légende:", padding, legendeY);
	            
	            for (int i = 0; i < 3; i++) {
	                g2d.setColor(couleurs[i]);
	                g2d.fillRect(padding + 80 + i * 150, legendeY - 15, 20, 15);
	                g2d.setColor(Color.BLACK);
	                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
	                g2d.drawString(methodes[i], padding + 105 + i * 150, legendeY - 2);
	            }
	            
	            g2d.setFont(new Font("Arial", Font.BOLD, 16));
	            String titre = "Performance pour le pattern: \"" + pattern + "\"";
	            g2d.drawString(titre, width / 2 - 150, 40);
	            
	            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
	            g2d.drawString("Temps d'exécution (ms)", 10, height / 2);
	            g2d.drawString("Fichiers (taille en KB)", width / 2 - 50, height - 30);
	            
	            String nomFichierSafe = pattern.replaceAll("[^a-zA-Z0-9]", "_");
	            File output = new File("Result/Result_Test_Some_files/pattern_" + nomFichierSafe + ".png");
	            ImageIO.write(image, "png", output);
	            System.out.println("📊 Graphique généré: " + output.getName());
	            
	            g2d.dispose();
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur génération diagramme: " + e.getMessage());
	        }
	    }
	
}
