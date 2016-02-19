package application.tests;

import java.io.IOException;
import java.util.Random;

import application.btree.BTree;
import application.geometricSequence.GeometricSequence;
import application.io.DataFile;
import application.io.IndexFile;

/**
 * Klasa do testowania œredniej liczby odczytów z dysku podczas wyszukiwania
 * @author Manio
 *
 */
public class Test2 {
	
	/**
	 * Zliczanie odczytów z pliku indeksów potrzebnych do wyszukiwania 1000 losowych rekordów drzewa 
	 * @param tree
	 * @param recordCount
	 * @param searchCount
	 * @throws IOException 
	 */
	public static void test(BTree tree, int recordCount, int searchCount) throws IOException{
		
		Random rand = new Random();
		int testsCount = 10;
		
		IndexFile.cleanFile();
		DataFile.cleanFile();
		
		for(int i = 0; i < recordCount; i++) {
			tree.insert(new GeometricSequence(i, 1.0, 1.0));
		}
		
		double avgReads = 0;
		
		for(int j = 0; j < testsCount; j++) {
			for(int i = 0; i < searchCount; i++) {
				int id = rand.nextInt(recordCount);
				tree.search(id);
				avgReads += IndexFile.getReadsCount() + DataFile.getReadsCount();
			}
		}
		avgReads /= (searchCount*testsCount);
		System.out.println(avgReads);
	}
	
}
