
package Etude;

 public class ResultatBenchmark {
 		
	    String pattern;
	    String methode;
	    double tempsMoyen;
	    double ecartType;
	    double tempsMin;
	    double tempsMax;
	    int nbMatches;
	    int iterations;
    
 public ResultatBenchmark(String pattern, String methode, double tempsMoyen, 
                           double ecartType, double tempsMin, double tempsMax, 
                           int nbMatches, int iterations) {
    	
        this.pattern = pattern;
        this.methode = methode;
        this.tempsMoyen = tempsMoyen;
        this.ecartType = ecartType;
        this.tempsMin = tempsMin;
        this.tempsMax = tempsMax;
        this.nbMatches = nbMatches;
        this.iterations = iterations;
    }
    
    
}