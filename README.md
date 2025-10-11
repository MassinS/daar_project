# Projet Egrep clone - Recherche de Motifs

## Installation et exécution

### Prérequis :
- Java 8 ou supérieur.
- Junit pour les tests unitaire.
- WSL (pour la méthode egrep sur Windows).

### Exécution rapide :
```bash
java -jar binaire.jar <methode> <pattern> <fichier>
```
### Exemple :
```bash
java -jar binaire.jar automate "Sargon" Samples/56667-0.txt
java -jar binaire.jar compare "a|b" Samples/56667-0.txt
```

### Remarques importante à considèrer : 
- Dans la méthode Automate on affiche toute les lignes où il y'a un match donc cela peut engendrer un temps supplimentaire.
- Le projet est développer à eclipse dans windows , donc pour lancer la commande Egrep on utilise WSL , si vous etes sur Linux changer la commande dans la classe Etude/benchmarkEgrep.java la ligne 95. 






