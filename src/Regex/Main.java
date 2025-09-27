package Regex;

public class Main {

	public static void main(String[] args) {
		try {
			RegexArbre liste=RegexParseur.parseur("(ac)|(b*)");
			System.out.println(liste.root);
			System.out.println(liste.getFilsGauche().root);
			System.out.println(liste.getFilsDroite().getFilsGauche().root);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
