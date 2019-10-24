package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import backpack.Backpack;
import backpack.MyObject;

import static org.junit.Assert.*;

public class TestBackpack {
	
	@Test
	public void testSac0() throws FileNotFoundException {
		File file = new File("src/benchs/sac0");
		ArrayList<MyObject> objects = getObjects(file);
		Backpack sacados = new Backpack(file);
		
		int nbrMaxChoices[] = sacados.solve();
		
		int expected = 106;
		int actual = getTotalValue (objects, nbrMaxChoices);

		assertEquals(expected, actual);
	}
	
	@Test
	public void testSac1() throws FileNotFoundException {
		File file = new File("src/benchs/sac1");
		ArrayList<MyObject> objects = getObjects(file);
		Backpack sacados = new Backpack(file);
		
		int nbrMaxChoices[] = sacados.solve();
		
		int expected = 2137434;
		int actual = getTotalValue (objects, nbrMaxChoices);

		assertEquals(expected, actual);
	}
	
	@Test
	public void testSac2() throws FileNotFoundException {
		File file = new File("src/benchs/sac2");
		ArrayList<MyObject> objects = getObjects(file);
		Backpack sacados = new Backpack(file);
		
		int nbrMaxChoices[] = sacados.solve();
		
		int expected = 2158816;
		int actual = getTotalValue (objects, nbrMaxChoices);

		assertEquals(expected, actual);
	}
	
	@Test
	public void testSac3() throws FileNotFoundException {
		File file = new File("src/benchs/sac3");
		ArrayList<MyObject> objects = getObjects(file);
		Backpack sacados = new Backpack(file);
		
		int nbrMaxChoices[] = sacados.solve();
		
		int expected = 2160709;
		int actual = getTotalValue (objects, nbrMaxChoices);

		assertEquals(expected, actual);
	}
	
	@Test
	public void testSac4() throws FileNotFoundException {
		File file = new File("src/benchs/sac4");
		ArrayList<MyObject> objects = getObjects(file);
		Backpack sacados = new Backpack(file);
		
		int nbrMaxChoices[] = sacados.solve();
		
		int expected = 2186851;
		int actual = getTotalValue (objects, nbrMaxChoices);

		assertEquals(expected, actual);
	}
	
	private int getTotalValue (ArrayList<MyObject> objects, int nbrMaxChoices[]) {
		int valeurTotale = 0;
		
		for (int i = 0; i < objects.size(); i++)
			valeurTotale += nbrMaxChoices[i] * objects.get(i).getValue();
		
		return valeurTotale;
	}
	
	
	private ArrayList<MyObject> getObjects(File file) throws FileNotFoundException {
		Scanner sc = null;
		ArrayList<MyObject> objects = new ArrayList<MyObject>();
		
		try {
			sc = new Scanner(file);
			sc.nextDouble();
			sc.nextInt();

			while (sc.hasNextDouble())
				objects.add(new MyObject(0, sc.nextDouble(), sc.nextDouble()));
		}
		finally {
			if (sc != null)
				sc.close();
		}
		
		return objects;
	}
}
