package application.commands;

import java.io.IOException;

import application.btree.BTree;
import application.geometricSequence.GeometricSequence;
import application.io.DataFile;
import application.tests.Test1;
import application.tests.Test2;

public class CommandParser {
	
	public static boolean parseCommand(BTree btree, String command) throws IOException, CommandErrorException {
		if(command != null) {
			String[] commandWords = command.split(" ");
			if(commandWords.length == 0) {
				return true;
			} else {
				if(commandWords[0].equals("+") && commandWords.length == 4) {
					//dodawanie
					Integer id = Integer.parseInt(commandWords[1]);
					if(id >= 0) {
						Double firstTerm, multiplier;
						firstTerm = Double.parseDouble(commandWords[2]);
						multiplier = Double.parseDouble(commandWords[3]);
						btree.insert(new GeometricSequence(id, firstTerm, multiplier));
					} else {
						throw new CommandErrorException(command);
					}
				} else if (commandWords[0].equals("-") && commandWords.length == 2) {
					//usuwanie
					Integer id = Integer.parseInt(commandWords[1]);
					btree.delete(id);
				} else if(commandWords.length == 1 && commandWords[0].toLowerCase().equals("exit")) {
					//wyjœcie
					return false;
				} else if(commandWords[0].toLowerCase().equals("f") && commandWords.length == 2) {
					//wyszukiwanie
					Integer id = Integer.parseInt(commandWords[1]);
					GeometricSequence founded = btree.search(id);
					if(founded != null) {
						System.out.println("Id: " + founded.getId() + "\nPierwszy wyraz ci¹gu: " + founded.getFirstTerm() 
								+ "\nIloraz: " + founded.getMultiplier());
					} else {
						System.out.println("Nie ma rekordu o Id " + id);
					}
				} else if(commandWords[0].toLowerCase().equals("showt")) {
					//wyœwietlanie drzewa
					btree.displayTree();
				} else if(commandWords[0].toLowerCase().equals("showd")) {
					//wyœwietlanie danych
					DataFile.printAllData();
				} else if(commandWords[0].equals("*") && commandWords.length == 4) {
					Integer id = Integer.parseInt(commandWords[1]);
					Double firstTerm = Double.parseDouble(commandWords[2]),
							multiplier = Double.parseDouble(commandWords[3]);
					if(id > 0) {
						btree.modify(id, firstTerm, multiplier);
					} else {
						throw new CommandErrorException(command);
					}
				} else if(commandWords[0].toLowerCase().equals("help") && commandWords.length == 1) {
					System.out.println("Dostêpne polecenia:\nDodanie rekordu:\t+ <id> <1. wyraz ci¹gu> <iloraz ci¹gu>\nUsuwanie rekordu:\t- <id>"
							+ "\nModyfikacja rekordu:\t* <id> <nowy 1. wyraz ci¹gu> <nowy iloraz ci¹gu>\nWyszukiwanie rekordu:\tf <id>\n"
							+ "Wydruk drzewa:\t\tshowT (wielkoœæ liter nie ma znaczenia)\nWydruk danych:\t\tshowD (wielkoœæ liter nie ma znaczenia)"
							+ "\nPomoc:\t\t\thelp (wielkoœæ liter nie ma znaczenia)\nWyjœcie z programu\texit (wielkoœæ liter nie ma znaczenia)");
				} else if(commandWords[0].toLowerCase().equals("test1") && commandWords.length == 3) {
					int recordCount = Integer.parseInt(commandWords[1]);
					double chanceForInsert = Double.parseDouble(commandWords[2]);
					
					Test1.test(btree, recordCount, chanceForInsert);
				} else if(commandWords[0].toLowerCase().equals("test2") && commandWords.length == 3) {
					int recordCount = Integer.parseInt(commandWords[1]), searchCount = Integer.parseInt(commandWords[2]);
					Test2.test(btree, recordCount, searchCount);
				}
			}
			return true;
		}
		return false;
	}
	
}
