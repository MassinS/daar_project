package KMP;

import java.util.ArrayList;
import java.util.List;

public class KmpAlgorithm {

    // Retourne la liste des indices de début où 'mot' apparaît dans 'text'
    public static List<Integer> KmpImplementation(String text, String mot) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || mot == null) return matches;
        int n = text.length();
        int m = mot.length();
        if (m == 0) {
            // option : motif vide => toutes les positions
            for (int i = 0; i <= n; i++) matches.add(i);
            return matches;
        }

        // étape 1 : CO (next) avec sentinelle -1, taille m+1
        int[] CO = creerCO(mot);         // next[0] = -1, next[1..m] valides

        // étape 2 : CO1 (transforme certains 0 en -1 si caractère == premier caractère)
        int[] CO1 = creerTableauCo1(CO, mot);

        // étape 3 : CO2 (optimisation : si mot[i] == mot[CO1[i]] alors CO1[i] = CO1[CO1[i]])
        int[] CO2 = creerTableauCo2(CO1, mot);

        // recherche utilisant la table avec sentinelle
        int i = 0;   // index sur text
        int j = 0;   // index sur motif
        while (i < n) {
            // l'expression (j == -1 || text.charAt(i) == mot.charAt(j)) est sûre
            if (j == -1 || text.charAt(i) == mot.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    matches.add(i - m); // match trouvé
                    j = CO2[j];         // continuer pour chercher chevauchements
                }
            } else {
                j = CO2[j]; // fallback via table
            }
        }

        return matches;
    }

    // -------------------------
    // creerCO : version classique 'next' avec sentinelle -1
    // next length = m + 1, next[0] = -1, next[i] = longueur du plus long préfixe-propre
    // -------------------------
    public static int[] creerCO(String mot) {
        int m = mot.length();
        int[] next = new int[m + 1];
        next[0] = -1;
        int i = 0;
        int j = -1;
        // invariant: on remplit next[i+1] = j+1 lorsque characters match
        while (i < m) {
            if (j == -1 || mot.charAt(i) == mot.charAt(j)) {
                i++;
                j++;
                next[i] = j;
            } else {
                j = next[j]; // reculer via table
            }
        }
        return next;
    }

    // -------------------------
    // creerTableauCo1 : étape où on remplace certains 0 par -1 si besoin
    // (reproduit la logique de ton ami : si mot[i] == mot[0] && next[i] == 0 => next[i] = -1)
    // NOTE: next provient de creerCO et a taille m+1 ; on modifie indices 1..m-1
    // -------------------------
    public static int[] creerTableauCo1(int[] CO, String mot) {
        int m = mot.length();
        // on clone pour ne pas modifier l'original si tu veux conserver CO
        int[] t = new int[CO.length];
        System.arraycopy(CO, 0, t, 0, CO.length);

        for (int i = 1; i < m; i++) { // positions correspondant aux caractères 1..m-1
            if (t[i] == 0 && mot.charAt(i) == mot.charAt(0)) {
                t[i] = -1;
            }
        }
        return t;
    }

    // -------------------------
    // creerTableauCo2 : optimisation finale
    // Si mot[i] == mot[CO[i]] et CO[CO[i]] != -1, on met CO[i] = CO[CO[i]]
    // Attention aux bornes : on parcourt i de 1 à m (mais on compare i<m pour mot.charAt(i))
    // -------------------------
    public static int[] creerTableauCo2(int[] CO1, String mot) {
        int m = mot.length();
        int[] t = new int[CO1.length];
        System.arraycopy(CO1, 0, t, 0, CO1.length);

        for (int i = 1; i <= m; i++) {
            // on n'accède à mot.charAt(i) que si i < m
            if (t[i] != -1 && i < m) {
                int j = t[i];
                if (j >= 0 && j < t.length && t[j] != -1) {
                    // si chars égaux -> on saute
                    if (mot.charAt(i) == mot.charAt(j)) {
                        t[i] = t[j];
                    }
                }
            }
        }
        return t;
    }

    // -------------------------
    // test rapide
    // -------------------------
    public static void main(String[] args) {
        String text = "abababcabababcababc";
        String motif = "abababc";
        List<Integer> res = KmpAlgorithm.KmpImplementation(text, motif);
        System.out.println("Matches at: " + res); // attendu: [0, 7]
        
        // test avec chevauchement
        text = "aaaaa";
        motif = "aaa";
        System.out.println("Matches at: " + KmpAlgorithm.KmpImplementation(text, motif)); // attendu [0,1,2]
    }
}
