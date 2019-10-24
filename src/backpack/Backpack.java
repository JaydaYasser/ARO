package backpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Backpack {
	private ArrayList<MyObject> objects = new ArrayList<MyObject>();
	private double capacity; // capacité du sac
	private double bestTotalValue;
	private int[] bestChoices;
	private int nbrMaxChoices;

	public Backpack(int capacity, ArrayList<MyObject> objects, int nbrMaxChoices) {
		this.capacity = capacity;			// Capacité maximale du sac à dos.
		this.objects = objects;				// Objets à placer (position initiale, poids, valeur).
		this.nbrMaxChoices = nbrMaxChoices; // Nombre Maximum de fois que l'on peut choisir un même objet.
		this.bestTotalValue = 0;
	}
	
	public Backpack(File file) throws FileNotFoundException {
		this.bestTotalValue = 0;
		int currentPos = 0;			// Permet de retenir dans quel ordre les objets ont été transmits
									// même après un tri de ces derniers.
		Scanner sc = null;

		try {
			sc = new Scanner(file);
			capacity = sc.nextDouble(); 	// Capacité maximale du sac à dos.
			nbrMaxChoices = sc.nextInt(); 	// Nombre Maximum de fois que l'on peut choisir un même objet.
		
			/* Lecture des objets (couples poids-valeur). */
			while (sc.hasNextDouble()) { 
				objects.add(new MyObject(currentPos, sc.nextDouble(), sc.nextDouble()));
				currentPos++;
			}
		}
		/* Précaution pour fermer le scanner quoi qu'il arrive. */
		finally {
			if (sc != null)
				sc.close();
		}
	}
	
	/* Algorithme glouton permettant d'optimiser le calcul d'une solution par branchAndBound.
	 * Il calcule la borne maximale d'un remplissage de sac à dos en coupant le dernier objet
	 * s'il ne rentre pas. 
	 * */
	private double getUpperBound(int currentObj, double totalValue, double remainingCapacity) {
		double choice, weight, value;
		/* Pour chaque objet et tant que le sac n'est pas plein, le mettre autant de fois que possible
		 * et couper le dernier.
		 * */
		while (currentObj < objects.size() && remainingCapacity > 0) {
			
			weight = objects.get(currentObj).getWeight();
			value = objects.get(currentObj).getValue();
			choice = (remainingCapacity/weight);

			/* On doit éviter de mettre trop de fois l'objet. */
			if (choice > nbrMaxChoices)
				choice = nbrMaxChoices;
				
			remainingCapacity -= weight * choice;
			totalValue += value * choice;
			currentObj++;
		}

		return totalValue;
	}
	
	/* Résout un problème de type sac à dos en explorant toutes les comibinaisons de remplissage possible
	 * de manière optimisée. Stocke dans meilleureValeurTotale la somme des valeurs des objets placés 
	 * dans le sac et stocke dans meilleurChoix le nombre de fois que chaque objet a été choisi.
	 */
	private void branchAndBound(int currentObj, double totalValue, double remainingCapacity, int[] choices) {
		int position;
		double weight, value;
		
		/* Condition d'arrêt de la récursivité, lorsque tous les objets
		 * ont été choisis un certain nombre de fois.
		 */
		if (currentObj == objects.size()) {
			if (totalValue > bestTotalValue) {
				bestTotalValue = totalValue;
				bestChoices = Arrays.copyOf(choices, choices.length);
			}
		}
		else {
			/* Appel de l'algorithme glouton avec l'état courant de l'exploration. Si la borne supérieure
			 * calculée est plus grande que la meilleure valeur totale précédemment calculée alors il est
			 * possible que l'agorithme trouve une valeur totale plus intéressante dans cette direction. 
			 * Sinon inutile de continuer l'exploration de l'ensemble des solution par ici.
			 */
			if (getUpperBound(currentObj, totalValue, remainingCapacity) > bestTotalValue) {
				position = objects.get(currentObj).getInitialPosition();
				weight = objects.get(currentObj).getWeight();
				value = objects.get(currentObj).getValue();
				
				/* Exploration des différentes possibilités de choix de l'objet courant dans l'ordre 
				 * décroissant pour trouver la solution optimale plus rapidement.
				 */
				for (int choice = (int) (remainingCapacity/weight); choice >= 0; choice--) {
					choices[position] = choice;
					branchAndBound(currentObj + 1, totalValue + value*choice,
								remainingCapacity - weight*choice, choices);
				}
			}
		}
	}
	
	/* Résout un problème de type sac à dos.
	 * Retourne le meilleur remplissage possible du sac, i.e. celui qui a la plus grande valeur totale. 
	 */
	public int[] solve() {
		/* Tri des objets par ordre décroissant et en fonction de leur ratio valeur/poids. */
		Collections.sort(objects, Collections.reverseOrder());
		branchAndBound(0, 0, capacity, new int[objects.size()]);

		return bestChoices;
	}
}

	