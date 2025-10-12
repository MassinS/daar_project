# Clone de egrep avec support partiel des ERE.
- Ce projet est un clone simplifié de la commande egrep, permettant la recherche de motifs (expressions régulières) avec différentes méthodes (automate, KMP, comparateur...).

## Installation et exécution

### Prérequis :
- Ce projet nécessite Java 11 ou supérieur.
- JUnit pour les tests unitaires.
- WSL (pour la méthode egrep sur Windows).

### Règle de compatibilité :
**La version d'exécution (JRE) doit être ÉGALE ou SUPÉRIEURE à la version de compilation (JDK)**

### Exemples :
- ✅ Compilé avec Java 11 → Exécutable sur Java 11, 17, 19, 22
- ✅ Compilé avec Java 17 → Exécutable sur Java 17, 19, 22  
- ❌ Compilé avec Java 22 → NON exécutable sur Java 11, 17, 19

### ⚠ IMPORTANT :
Le dossier bin et le fichier binaire.jar ont été générés avec JDK 22.
Donc :
- Si vous exécutez directement ces fichiers → Java 22 ou supérieur OBLIGATOIRE.
- Si vous avez une version de Java inférieure (ex : 11, 17, 19) → lancez `make clean && make run`
  pour recompiler le projet avec VOTRE version de Java.

### Exécution rapide avec MakeFile :
```bash
Make clean
Make run ARG1=<Method> ARG2=<Pattern> ARG3=<Path_file>
```
### Exemple d'utilisation :
```bash
make run ARG1="automate" ARG2="Sargon" ARG3="Samples/56667-0.txt"
```

### Exécution directe du jar : 
```bash
java -jar binaire.jar <methode> <pattern> <fichier>
```

### Exemple d'utilisation :
```bash
java -jar binaire.jar automate "Sargon" Samples/56667-0.txt
java -jar binaire.jar compare "a|b" Samples/56667-0.txt
```

### Les test de performance : 
Pour lancer les tests : 
-  il faut exécuter la classe EtudeExperimental/Etude/EtudeSomePatterns.java pour le test de Benchmark de patterns simples ( Egrep , Automate , KMP).
-  il faut exécuter la classe EtudeExperimental/Etude/EtudeSomeFiles.java pour le test de Benchmark de patterns simples sur différents taille de fichier de la bibliothèque Gutenburg.
-  il faut exécuter la classe EtudeExperimental/Etude/EtudeAutomateEgrep.java pour le test de Benchmark de patterns complexe pour comparer Egrep et Automate. 
-  il faut exécuter la classe EtudeExperimental/Etude/EtudeNombredeMots.java pour le test de Benchamrk de patterns simple pour comparer le nombre d'occurence pour chaque méthode.

### Les résulats des tests : 
les resultats de chaque test se trouve dans le dossier Result sous format PNG.


### Remarques importante à considèrer : 
- 1- La méthode automate affiche toutes les lignes contenant un motif trouvé, ce qui peut augmenter le temps d'exécution sur de gros fichiers.
- 2- Le projet a été développé sous Windows avec Eclipse. Pour exécuter la commande egrep, WSL est utilisé par défaut. Si vous êtes sous Linux, modifiez la ligne 95 du fichier Etude/benchmarkEgrep.java :
-  Remplacer :

```bash
ProcessBuilder pb = new ProcessBuilder("wsl", "time", "egrep", "-o", patternAvecGuillemets, fichier);
```
- Par :

```bash
ProcessBuilder pb = new ProcessBuilder("time", "egrep", "-o", patternAvecGuillemets, fichier);
```

