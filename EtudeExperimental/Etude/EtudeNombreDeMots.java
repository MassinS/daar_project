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
import java.util.List;

import javax.imageio.ImageIO;

public class EtudeNombreDeMots {

    public static void main(String[] args) {
        try {
            String fichier = "Samples/56667-0.txt";
            List<String> patterns = Files.readAllLines(Paths.get("Samples/patterns.txt"));

            if (!Files.exists(Paths.get(fichier))) {
                System.err.println(" Fichier manquant: " + fichier);
                return;
            }

            List<PatternResult> tousResultats = new ArrayList<>();

            for (String pattern : patterns) {
                System.out.println(" Pattern: " + pattern + " - Fichier: " + fichier);

                String text = RechercheDFA.chargerTexte(fichier);

                ResultatBenchmark resAutomate = EtudeBenchmark.benchmarkAutomate(pattern, text);
                ResultatBenchmark resKMP = EtudeBenchmark.benchmarkKMP(pattern, text);
                ResultatBenchmark resEgrep = EtudeBenchmark.benchmarkEgrep(pattern, fichier);

                tousResultats.add(new PatternResult(pattern, 
                    resAutomate.nbMatches, 
                    resKMP.nbMatches, 
                    resEgrep.nbMatches));

                System.out.println(" Résultats pour le pattern: " + pattern);
                System.out.println(" Automate: " + resAutomate.nbMatches + " occurrences");
                System.out.println(" KMP: " + resKMP.nbMatches + " occurrences");
                System.out.println(" Egrep: " + resEgrep.nbMatches + " occurrences");
                System.out.println();
            }

            // Générer un seul graphique avec tous les patterns
            genererGraphiqueUnique(tousResultats, fichier);

        } catch (Exception e) {
            System.err.println(" Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class PatternResult {
        String pattern;
        int occurrencesAutomate;
        int occurrencesKMP;
        int occurrencesEgrep;

        public PatternResult(String pattern, int auto, int kmp, int egrep) {
            this.pattern = pattern;
            this.occurrencesAutomate = auto;
            this.occurrencesKMP = kmp;
            this.occurrencesEgrep = egrep;
        }
    }

    private static void genererGraphiqueUnique(List<PatternResult> resultats, String fichier) {
        try {
            int nbPatterns = resultats.size();
            int width = 1000 + nbPatterns * 100;
            int height = 800;
            int padding = 100;
            int legendeHeight = 80;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int graphWidth = width - 2 * padding;
            int graphHeight = height - 2 * padding - legendeHeight;

            // Trouver le maximum d'occurrences pour l'échelle Y
            int maxOccurrences = 0;
            for (PatternResult res : resultats) {
                maxOccurrences = Math.max(maxOccurrences, res.occurrencesAutomate);
                maxOccurrences = Math.max(maxOccurrences, res.occurrencesKMP);
                maxOccurrences = Math.max(maxOccurrences, res.occurrencesEgrep);
            }
            maxOccurrences = Math.max(maxOccurrences, 10);

            // Couleurs
            Color[] couleurs = {new Color(255, 99, 132), new Color(54, 162, 235), new Color(75, 192, 192)};
            String[] methodes = {"Automate", "KMP", "Egrep"};

            // Dessiner les axes
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(padding, height - padding - legendeHeight, width - padding, height - padding - legendeHeight);
            g2d.drawLine(padding, padding, padding, height - padding - legendeHeight);

            // Échelle Y
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i <= 10; i++) {
                int y = height - padding - legendeHeight - (i * graphHeight / 10);
                int value = i * maxOccurrences / 10;
                g2d.drawString(value + " occ", padding - 50, y + 5);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawLine(padding, y, width - padding, y);
                g2d.setColor(Color.BLACK);
            }

            // Configuration des barres
            int groupeWidth = graphWidth / nbPatterns;
            int barWidth = groupeWidth / 4; // 3 barres + espace
            int espace = barWidth / 3;

            // Dessiner les barres pour chaque pattern
            for (int i = 0; i < nbPatterns; i++) {
                PatternResult res = resultats.get(i);
                int baseX = padding + i * groupeWidth + espace;

                // Barre Automate
                int barHeightAuto = (int)((double)res.occurrencesAutomate / maxOccurrences * graphHeight);
                g2d.setColor(couleurs[0]);
                g2d.fillRect(baseX, height - padding - legendeHeight - barHeightAuto, barWidth, barHeightAuto);

                // Barre KMP
                int barHeightKMP = (int)((double)res.occurrencesKMP / maxOccurrences * graphHeight);
                g2d.setColor(couleurs[1]);
                g2d.fillRect(baseX + barWidth + espace, height - padding - legendeHeight - barHeightKMP, barWidth, barHeightKMP);

                // Barre Egrep
                int barHeightEgrep = (int)((double)res.occurrencesEgrep / maxOccurrences * graphHeight);
                g2d.setColor(couleurs[2]);
                g2d.fillRect(baseX + 2 * (barWidth + espace), height - padding - legendeHeight - barHeightEgrep, barWidth, barHeightEgrep);

                // Valeurs sur les barres
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));

                // Automate
                g2d.drawString(res.occurrencesAutomate + " occ", 
                    baseX - 15, height - padding - legendeHeight - barHeightAuto - 5);
                // KMP
                g2d.drawString(res.occurrencesKMP + " occ", 
                    baseX + barWidth + espace - 15, height - padding - legendeHeight - barHeightKMP - 5);
                // Egrep
                g2d.drawString(res.occurrencesEgrep + " occ", 
                    baseX + 2 * (barWidth + espace) - 15, height - padding - legendeHeight - barHeightEgrep - 5);

                // Nom du pattern (tronqué si trop long)
                String patternCourt = res.pattern.length() > 10 ? res.pattern.substring(0, 10) + "..." : res.pattern;
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString(patternCourt, baseX - 10, height - padding - legendeHeight + 20);
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
            String titre = "Comparaison des occurrences pour tous les patterns - Fichier: " + nomFichier;
            g2d.drawString(titre, width / 2 - 250, 40);

            // Labels des axes
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString("Nombre d'occurrences", 10, height / 2);
            g2d.drawString("Patterns", width / 2 - 30, height - 30);

            // Sauvegarde
            File output = new File("Result/Result_Occurence_KMP_automate_egrep/Comparaison_occurrences__KMP_automate_Egrep.png");
            output.getParentFile().mkdirs();

            ImageIO.write(image, "png", output);
            System.out.println(" GRAPHIQUE UNIQUE GÉNÉRÉ: " + output.getName());

            g2d.dispose();

        } catch (Exception e) {
            System.err.println(" Erreur génération graphique unique: " + e.getMessage());
            e.printStackTrace();
        }
    }
}