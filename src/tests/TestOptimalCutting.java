package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import backpack.Backpack;
import backpack.MyObject;
import optimalcutting.OptimalCutting;

import static org.junit.Assert.*;

public class TestOptimalCutting {
	
	@Test
	public void testDecoupe0() throws FileNotFoundException {
		File file = new File("src/benchs/decoupe0");
		OptimalCutting optCut = new OptimalCutting(file);
		
		optCut.solve();
		
		double expected = 452.25;
		double actual = optCut.getSolution();
		
		optCut.deleteAndFree();
		
		assertEquals(expected, actual, 0);
	}
}
