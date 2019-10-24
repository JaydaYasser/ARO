package optimalcutting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;

import backpack.*;

public class OptimalCutting {
	/* Instance du problème. */ 
	private glp_prob prob;
	/* Données du problème. */ 
	private int rows, cols, initialWidth;
	private int[] widths, rolls;
	/* Coefficients non-nuls de la matrice stockés sous forme de pointeurs. */
	private final int SIZE = 100;
	private int nonZeroCoeffs = 0;
	private SWIGTYPE_p_int ia, ja;
	private SWIGTYPE_p_double coeffs;
	/* Stockage de la solution courante du problème. */
	private double z; 
	private double x[], y[];
	
	/* Constructeur du problème, importe les données initiales
	 * issues d'un fichier et initialise l'instance du problème. */
	public OptimalCutting(File file) throws FileNotFoundException {
		ia = GLPK.new_intArray(SIZE);
		ja = GLPK.new_intArray(SIZE);
		coeffs = GLPK.new_doubleArray(SIZE);
		
		loadFile(file);
		
		/* Création d'un problème de minimisation. */
		prob = GLPK.glp_create_prob();
		GLPK.glp_set_prob_name(prob, "Découpe de rouleaux");
		GLPK.glp_set_obj_dir(prob, GLPK.GLP_MIN);
		/* Supprime les affichages créés par GLPK dans la console. */
		GLPK.glp_term_out(GLPK.GLP_OFF);
	
		build();
	}
	
	/* Importe les données contenues dans un fichier passé en paramètre. */
	private void loadFile(File file) throws FileNotFoundException {
		Scanner sc = null;
		
		try {
			sc = new Scanner(file);
		
			initialWidth = sc.nextInt();
			rows = sc.nextInt(); cols = rows;
			widths = new int[rows];
			rolls = new int[rows];
			
			/* Lecture de la commande du client. */
			for (int row = 0; row < rows; row++) {
				widths[row] = sc.nextInt();
				rolls[row] = sc.nextInt();
			}
		} finally {
			if(sc != null)
				sc.close();
		}
	}
	
	/* Prépare un fichier .txt contenant les données d'un programme type "sac-à-dos"
	 * à partir de la solution duale optimale du programme linéaire réduit. */
	private int[] solveBackpack()  {
		Backpack backpack = null;
		ArrayList<MyObject> objects = new ArrayList<MyObject>();
		
		for (int i = 0; i <widths.length; i++)
			objects.add(new MyObject(i, widths[i], y[i]));
		
		/* Appel de l'algorithme resolvant un problème de type sac à dos.
		 * Il n'y a pas de limite sur le nombre de fois que l'on peut choisir
		 * chaque objet (ici Integer.MAX_VALUE équivaut à l'infini). */
		backpack = new Backpack(initialWidth, objects, Integer.MAX_VALUE);
		
		return backpack.solve();
	}
	
	/* Calcule et retourne le cout réduit de la colonne passée en paramètre. */
	private double computeCost(int[] column) {
		/* Affichage de la colonne sur la sortie standard. */
		System.out.print("colonne trouvée: [");
		for (int i = 0; i < column.length-1; i++)
			System.out.print(column[i] + ", ");
		System.out.print(column[column.length-1] + "] ");
		
		/* Calcul du coût réduit de la colonne. */
		double sum = 0;
		
		for (int i = 0; i < rows; i++)
			sum += y[i] * column[i];
				
		return sum;
	}
	
	/* Résout le problème linéaire réduit avec la méthode du simplex. */
	private void solveReducedLinearProgram() {
		x = new double[cols];
		y = new double[rows];
		
		GLPK.glp_simplex(prob, null);
		
		z = GLPK.glp_get_obj_val(prob);
		for (int i = 0; i < cols; i++)
			x[i] = GLPK.glp_get_col_prim(prob, i + 1);
		
		for (int i = 0; i < rows; i++)
			y[i] = GLPK.glp_get_row_dual(prob, i + 1);
		
		/* Affichage de la solution sur la sortie standard. */
		System.out.format(Locale.ENGLISH, "z = %.2f\n", z);
		
		for (int i = 0; i < cols; i++)
			if (x[i] > 0.0)
				System.out.format(Locale.ENGLISH, "x%d = %.2f; ", i + 1, x[i]);
		System.out.println();
		
		for (int i = 0; i < rows; i++)
			System.out.format(Locale.ENGLISH, "y%d = %.2f; ", i + 1, y[i]);
		System.out.println(); System.out.println();
	}
	
	/* Supprime l'instance du problème et libère la mémoire utilisée. */
	public void deleteAndFree() {
		GLPK.glp_delete_prob(prob);
		GLPK.glp_free_env();
	}
	
	/* Construit les colonnes, les lignes et la matrice du problème. */
	private void build() {
		
		GLPK.glp_add_cols(prob, cols);
		for (int i = 1; i <= cols; i++) {
			GLPK.glp_set_col_name(prob, i, "x" + i);
			GLPK.glp_set_col_bnds(prob, i, GLPK.GLP_LO, 0.0, 0.0);
			GLPK.glp_set_obj_coef(prob, i, 1);
		}
		
		GLPK.glp_add_rows(prob, rows);
		for (int i = 1; i <= rows; i++) {
			GLPK.glp_set_row_name(prob, i, "c" + i);
			GLPK.glp_set_row_bnds(prob, i, GLPK.GLP_LO, rolls[i - 1], 0.0);
		}
		
		/* Initialisation de la matrice. */
		double coeff;
		for (int col = 1; col <= cols; col++) {
			coeff = Math.floor(initialWidth/widths[col-1]);
			
			if (coeff > 0) {
				nonZeroCoeffs++;
				GLPK.intArray_setitem(ia, nonZeroCoeffs, col);
				GLPK.intArray_setitem(ja, nonZeroCoeffs, col);
				GLPK.doubleArray_setitem(coeffs, nonZeroCoeffs, coeff);
			}
		}
		
		GLPK.glp_load_matrix(prob, nonZeroCoeffs, ia, ja, coeffs);
	}
	
	/* Met à jour le problème en ajoutant une nouvelle colonne passée en paramètre. */
	private void update(int[] column) {
		double value;
		cols++;
		
		for(int i = 0; i < column.length; i++) {
			value = column[i];
			/* Stockage des coefficients non-nuls uniquement. */
			if (value > 0) {
				GLPK.intArray_setitem(ia, nonZeroCoeffs + 1, i + 1);
				GLPK.intArray_setitem(ja, nonZeroCoeffs + 1, cols);
				GLPK.doubleArray_setitem(coeffs, nonZeroCoeffs + 1, value);
				nonZeroCoeffs++;
			}
		}
	
		GLPK.glp_add_cols(prob, 1);
		GLPK.glp_set_col_name(prob, cols, "x" + cols);
		GLPK.glp_set_col_bnds(prob, cols, GLPK.GLP_LO, 0.0, 0.0);
		GLPK.glp_set_obj_coef(prob, cols, 1);
		
		GLPK.glp_load_matrix(prob, nonZeroCoeffs, ia, ja, coeffs);
	}
	
	/* Retourne la solution du programme linéaire résolu au préalable. */
	public double getSolution() {
		return z;
	}
	
	/* Retourne la colonne d'indice "columnIndex" de la matrice définie par [ia, ja et coeff]. */
	private int[] getColumn(int columnIndex) {
		int[] column = new int[rows];
		SWIGTYPE_p_int ind = GLPK.new_intArray(cols + 1);
		SWIGTYPE_p_double val = GLPK.new_doubleArray(cols + 1);
		
		/* "len" contient le nombre de coefficients non-nuls de la colonne. 
		 * "ind" contient leur position au sein de la colonne et "val" contient leur valeur. */
		int len = GLPK.glp_get_mat_col(prob, columnIndex + 1, ind, val);
		
		for (int i = 1; i <= len; i++)
			column[GLPK.intArray_getitem(ind, i) - 1] = (int) GLPK.doubleArray_getitem(val, i);
		
		return column;
	}
	
	/* Resolution du problème complet. */
	public void solve() throws FileNotFoundException {
		/* Coût positif initialement pour exécuter la boucle while. */
		double cost = 2;
		int step = 1;
		int[] column;
		
		while (cost > 1) {
			System.out.println("---------------------------");
			System.out.format("---     Etape %03d       ---\n", step);
			System.out.println("---------------------------");
			
			solveReducedLinearProgram();
			
			column = solveBackpack();
			cost = computeCost(column);
			System.out.println("avec un coût de " + cost + ".");	
			
			update(column);
			step++;
			System.out.println();
		}
		
		System.out.println("Solution optimale.");
		
		int[] c;
		for (int j = 0; j < x.length; j++) {
			
			if(x[j] > 0) {
				System.out.print(x[j] + " * [");
				c = getColumn(j);
				
				for (int i = 0; i < c.length-1; i++) {
					System.out.print(c[i] + ", ");
				}
				System.out.println(c[c.length-1] + "]");
			}
		}
	}
}