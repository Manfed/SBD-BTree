package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import application.btree.BTree;
import application.commands.CommandErrorException;
import application.commands.CommandParser;
import application.io.DataFile;
import application.io.IndexFile;
import application.io.MetadataFile;

/**
 * G³ówna klasa programu
 * @author Marcin Kruszyñski
 *
 */
public class App {

	public static void main(String[] args) throws NumberFormatException, IOException, CommandErrorException {
		BufferedReader in =  new BufferedReader(new InputStreamReader(System.in));
		System.out.println("1 - nowe drzewo\n2 - wczytaj z pliku");
		
		int mode = Integer.parseInt(in.readLine());
		BTree root = null;
		String treeName = null;
		
		IndexFile idxFile = null;
		DataFile dataFile = null;
		MetadataFile metaFile = null;
		
		switch (mode) {
		case 1:
			System.out.println("Nazwa drzewa: ");
			treeName = in.readLine();
			System.out.println("Stopieñ drzewa (d): ");
			Integer order = Integer.parseInt(in.readLine());
			
			idxFile = new IndexFile(treeName, true);
			dataFile = new DataFile(treeName, true);
			metaFile = new MetadataFile(treeName, order);
			
			root = new BTree(order);
			break;
		case 2:
			System.out.println("Podaj nazwê drzewa: ");
			treeName = in.readLine();
			idxFile = new IndexFile(treeName, false);
			dataFile = new DataFile(treeName, false);
			metaFile = new MetadataFile(treeName);
			root = new BTree();
			break;
		default:
			break;
		}
		
		boolean commandPhase = true;
		System.out.println("HELP - aby uzyskaæ listê poleceñ");
		while(commandPhase) {
			if(root != null) {
				String command = in.readLine();
				commandPhase = CommandParser.parseCommand(root, command);
			} else {
				System.out.println("Nie mo¿na odczytaæ korzenia drzewa");
				break;
			}
		}
		
		in.close();
		metaFile.close();
		idxFile.close();
		dataFile.close();
	}

}