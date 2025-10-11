
package Etude;

 /*
  * Cette classe est le resultat de l'étude où on stocke le pattern testé , la méthode utilisé ( egrep , Automate ou KMP )
  * le temps pris pour la méthode et le nombre de nbrOccurence trouvé pour le pattern testé dans chaque méthode
  */
 public class ResultatBenchmark {
 		
	    String pattern;
	    String methode;
	    double temps;    
	    int nbrOccurence;
	
 public ResultatBenchmark(String pattern, String methode, double temps, int nbrOccurence) {
    	
        this.pattern = pattern;
        this.methode = methode;
        this.temps = temps;
        this.nbrOccurence = nbrOccurence;

    }
    
    
}