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
import java.util.List;

import javax.imageio.ImageIO;

public class EtudeNombreDeMots {

	
	 public static void main(String[] args) {
	        try {
	            // Un seul fichier à tester
	            String fichier = "Samples/56667-0.txt";
	            
	            List<String> patterns = Files.readAllLines(Paths.get("Samples/patterns.txt"));
	            
	            // Vérifier que le fichier existe
	            if (!Files.exists(Paths.get(fichier))) {
	                System.err.println("❌ Fichier manquant: " + fichier);
	                return;
	            }
	            
	            for (String pattern : patterns) {
	                genererGraphiquePattern(pattern, fichier);
	            }
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	    
	    private static void genererGraphiquePattern(String pattern, String fichier) {
	        try {
	            System.out.println("🔍 Pattern: " + pattern + " - Fichier: " + fichier);
	            
	            String text = RechercheDFA.chargerTexte(fichier);
	            long taille = Files.size(Paths.get(fichier));
	            
	            // Exécuter les benchmarks
	            ResultatBenchmark resAutomate = EtudeBenchmark.benchmarkAutomate(pattern, text);
	            ResultatBenchmark resKMP = EtudeBenchmark.benchmarkKMP(pattern, text);
	            ResultatBenchmark resEgrep = EtudeBenchmark.benchmarkEgrep(pattern, fichier);
	            
	            // Générer le graphique avec les occurrences
	            genererDiagrammeBatonOccurrences(pattern, fichier, taille, 
	                                           resAutomate.nbMatches , 
	                                           resKMP.nbMatches, 
	                                           resEgrep.nbMatches);
	            
	            // Afficher les résultats en console
	            System.out.println("📊 Résultats pour le pattern: " + pattern);
	            System.out.println("   🤖 Automate: " + resAutomate.nbMatches + " occurrences");
	            System.out.println("   🔍 KMP: " + resKMP.nbMatches + " occurrences");
	            System.out.println("   🐧 Egrep: " + resEgrep.nbMatches + " occurrences");
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur pour le pattern " + pattern + ": " + e.getMessage());
	        }
	    }
	    
	    private static void genererDiagrammeBatonOccurrences(String pattern, String fichier, 
	                                                       long tailleFichier,
	                                                       int occurrencesAutomate, 
	                                                       int occurrencesKMP, 
	                                                       int occurrencesEgrep) {
	        try {
	            int width = 800;  // Largeur réduite pour un seul fichier
	            int height = 600;
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
	            
	            // Trouver le nombre maximum d'occurrences pour l'échelle Y
	            int maxOccurrences = Math.max(
	                Math.max(occurrencesAutomate, occurrencesKMP), 
	                occurrencesEgrep
	            );
	            maxOccurrences = Math.max(maxOccurrences, 10); // Au moins 10 occurrences
	            
	            // Couleurs
	            Color[] couleurs = {new Color(255, 99, 132),   // Rouge
	                              new Color(54, 162, 235),    // Bleu  
	                              new Color(75, 192, 192)};   // Vert
	            
	            String[] methodes = {"Automate", "KMP", "Egrep"};
	            int[] occurrences = {occurrencesAutomate, occurrencesKMP, occurrencesEgrep};
	            
	            // Dessiner les axes
	            g2d.setColor(Color.BLACK);
	            g2d.setStroke(new BasicStroke(2));
	            g2d.drawLine(padding, height - padding - legendeHeight, width - padding, height - padding - legendeHeight); // Axe X
	            g2d.drawLine(padding, padding, padding, height - padding - legendeHeight); // Axe Y
	            
	            // Échelle Y - OCCURRENCES
	            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
	            for (int i = 0; i <= 10; i++) {
	                int y = height - padding - legendeHeight - (i * graphHeight / 10);
	                int value = i * maxOccurrences / 10;
	                g2d.drawString(value + " occ", padding - 50, y + 5);
	                g2d.setColor(Color.LIGHT_GRAY);
	                g2d.drawLine(padding, y, width - padding, y);
	                g2d.setColor(Color.BLACK);
	            }
	            
	            // Configuration des barres - 3 barres côte à côte
	            int barWidth = graphWidth / 5; // Largeur des barres
	            int espace = barWidth / 2;     // Espace entre les barres
	            
	            // Position de départ pour centrer les 3 barres
	            int totalWidth = 3 * barWidth + 2 * espace;
	            int startX = padding + (graphWidth - totalWidth) / 2;
	            
	            // Dessiner les barres pour chaque méthode
	            for (int i = 0; i < 3; i++) {
	                int x = startX + i * (barWidth + espace);
	                int occ = occurrences[i];
	                int barHeight = (int)((double)occ / maxOccurrences * graphHeight);
	                
	                // Dessiner la barre
	                g2d.setColor(couleurs[i]);
	                g2d.fillRect(x, height - padding - legendeHeight - barHeight, barWidth, barHeight);
	                
	                // Valeur au-dessus de la barre
	                g2d.setColor(Color.BLACK);
	                g2d.setFont(new Font("Arial", Font.BOLD, 12));
	                g2d.drawString(occ + " occ", x + barWidth/2 - 15, height - padding - legendeHeight - barHeight - 10);
	                
	                // Nom de la méthode en dessous
	                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
	                g2d.drawString(methodes[i], x + barWidth/2 - 20, height - padding - legendeHeight + 20);
	            }
	            
	            // Légende
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
	            
	            // Titre
	            g2d.setFont(new Font("Arial", Font.BOLD, 16));
	            String nomFichier = new File(fichier).getName();
	            String titre = "Occurrences trouvées - Pattern: \"" + pattern + "\"";
	            g2d.drawString(titre, width / 2 - 200, 30);
	            
	            // Sous-titre avec info fichier
	            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
	            String sousTitre = "Fichier: " + nomFichier + " (" + (tailleFichier/1024) + " KB)";
	            g2d.drawString(sousTitre, width / 2 - 100, 55);
	            
	            // Labels des axes
	            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
	            g2d.drawString("Nombre d'occurrences", 10, height / 2);
	            g2d.drawString("Méthodes de recherche", width / 2 - 50, height - 30);
	            
	            // Sauvegarde
	            String nomFichierSafe = pattern.replaceAll("[^a-zA-Z0-9]", "_");
	            File output = new File("Result/Result_NbrMots_KMP_automate_egrep/occurrences_" + nomFichierSafe + ".png");
	            
	            // Créer le dossier si nécessaire
	            output.getParentFile().mkdirs();
	            
	            ImageIO.write(image, "png", output);
	            System.out.println("📊 Graphique généré: " + output.getName());
	            
	            g2d.dispose();
	            
	        } catch (Exception e) {
	            System.err.println("❌ Erreur génération diagramme des occurrences: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	    
}
