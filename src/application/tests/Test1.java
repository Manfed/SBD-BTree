package application.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import application.Consts;
import application.btree.BTree;
import application.btree.BTreeNode;
import application.geometricSequence.GeometricSequence;
import application.io.DataFile;
import application.io.IndexFile;
import application.io.MetadataFile;
/**
 * Klasa do testowania wykorzystania pamiêci przez b-drzewo
 * @author Manio
 *
 */
public class Test1 {
	
	public static void test(BTree tree, int recordCount, double insertChance) throws IOException {
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		
		List<Double> stagesIdxAverage = new ArrayList<>(), stagesDataAverage = new ArrayList<>();
		double tmpAverageIndex = 0, tmpAverageData = 0;
		Random rand = new Random();
		PrintWriter out = new PrintWriter("test1" + insertChance + df.format(Calendar.getInstance().getTime()) +".txt");
		
		for(int i = 0; i < 2; i++) {
			int lastAddedId = 0;
			int lastDeletedId = 0;
			tree = new BTree(MetadataFile.getTreeOrder());
			
			//dodanie 100 liczb, aby zbytnio nie opró¿niæ drzewa
			for(int j = 0; j < 100; j++) {
				tree.insert(new GeometricSequence(lastAddedId++, 1.0, 1.0));
			}

			double stageAvgIdx = 0, stageAvgData = 0;
			
			for(int j = 0; j < recordCount - 100; j++) {
				double chance = rand.nextDouble();
				if(chance <= insertChance) {
					tree.insert(new GeometricSequence(lastAddedId++, 1.0, 1.0));
				} else {
					tree.delete(lastDeletedId++);
				}
				tmpAverageIndex = ((getTreeFreeMemory(tree.getRoot()) + MetadataFile.getIndexFreeAddressList().size()*Consts.getNodeSize(MetadataFile.getTreeOrder()))
						/ IndexFile.getIndexFile().length())*100.0;
				tmpAverageData = (getDataFreeMemory() / DataFile.getDataFile().length())*100.0;
				
				out.write(j+100 + ";" + (100-tmpAverageIndex) + ";" + (100-tmpAverageData) + ";\r\n");
				
				stageAvgData += tmpAverageData;
				stageAvgIdx += tmpAverageIndex;
				
			}
			
			stagesIdxAverage.add(stageAvgIdx / (recordCount-100));
			stagesDataAverage.add(stageAvgData / (recordCount-100));
			out.write("\r\n\r\n");
		}
		
		double avgIdx = 0, avgData = 0;
		
		for(int i = 0; i < 2; i++) {
			avgIdx += stagesIdxAverage.get(i);
			avgData += stagesDataAverage.get(i);
		}
		
		out.write("Œrednia zajêtoœæ:\r\nIndeksy: " + (avgIdx/2) + "\tDane: " + (avgData/2) + "\r\n");
		out.close();
	}
	 
	/**
	 * wyliczenie nieuzywanego miejsca w pliku indeksów - szukanie w pliku pól z wartoœci¹ -1(tak zapisywany jest null)
	 *  + dodanie rozmiaru usunietych wez³ów
	 * @param root
	 * @return
	 * @throws IOException
	 */
	private static double getTreeFreeMemory(BTreeNode root) throws IOException {
		double freeNodesMem = 0;
		
		IndexFile.getIndexFile().seek(0);
		for(int i = 0; i < IndexFile.getIndexFile().length(); i += Integer.BYTES) {
			if(!MetadataFile.getIndexFreeAddressList().contains(i)) {
				int val = IndexFile.getIndexFile().readInt();
				if(val == -1) {
					freeNodesMem += 4;
				}
			} else {
				i += Consts.getNodeSize(MetadataFile.getTreeOrder());
				freeNodesMem += Consts.getNodeSize(MetadataFile.getTreeOrder());
				IndexFile.getIndexFile().seek(i);
			}
		}
		
		return freeNodesMem;
	}
	
	private static double getDataFreeMemory() {
		return MetadataFile.getDataFreeAddressList().size() * Consts.DATA_RECORD_SIZE;
	}
}
