package application.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MetadataFile {
	private static RandomAccessFile metaFile;
	private static List<Integer> dataFreeAddressList;
	private static List<Integer> indexFreeAddressList;
	private static int treeOrder;
	private static int rootPosition;
	private static boolean rootModified;
	
	public MetadataFile(String treeName) throws IOException {
		File metadataFile = new File(treeName + ".meta");
		try {
			metaFile = new RandomAccessFile(metadataFile, "rw");
		} catch (FileNotFoundException e) {
			System.out.println("Nie znaleziono pliku metadanych");
			e.printStackTrace();
		}
		dataFreeAddressList = getDataFreeAddrList();
		indexFreeAddressList = getIndexFreeAddrList();
		treeOrder = readTreeOrder();
		rootPosition = getRootAddress();
		rootModified = false;
	}
	
	public MetadataFile(String treeName, Integer order) throws IOException {
		File metadataFile = new File(treeName + ".meta");
		try {
			metaFile = new RandomAccessFile(metadataFile, "rw");
		} catch (FileNotFoundException e) {
			System.out.println("Nie znaleziono pliku metadanych");
			e.printStackTrace();
		}
		dataFreeAddressList = new ArrayList<>();
		indexFreeAddressList = new ArrayList<>();
		metaFile.setLength(0);
		treeOrder = order;
	}
	
	
	public static void writeRoot(Integer newRoot) throws IOException {
		metaFile.seek(0);
		metaFile.writeInt(newRoot);
	}
	
	public static void writeTreeOrder(Integer treeOrder) throws IOException {
		metaFile.seek(4);
		metaFile.writeInt(treeOrder);
	}
	
	public static int getRootAddress() throws IOException {
		metaFile.seek(0);
		int rootAddress = metaFile.readInt();
		return rootAddress;
	}
	
	public static int readTreeOrder() throws IOException {
		metaFile.seek(4);
		int order = metaFile.readInt();
		return order;
	}
	
	public static void cleanFile() throws IOException {
		metaFile.setLength(0);
		dataFreeAddressList.clear();
		indexFreeAddressList.clear();
	}
	
	private static List<Integer> getDataFreeAddrList() throws IOException {
		List<Integer> dataFreeAddrList = new ArrayList<>();
		metaFile.seek(8);
		int listSize = metaFile.readInt();
		for(int i = 0; i < listSize; i++) {
			dataFreeAddrList.add(metaFile.readInt());
		}
		return dataFreeAddrList;
	}
	
	private static List<Integer> getIndexFreeAddrList() throws IOException {
		List<Integer> indexFreeAddrList = new ArrayList<>();
		metaFile.seek(8);
		int dataListSize = metaFile.readInt();
		metaFile.seek(12 + dataListSize*4);
		int listSize = metaFile.readInt();
		for(int i = 0; i < listSize; i++) {
			indexFreeAddrList.add(metaFile.readInt());
		}
		return indexFreeAddrList;
	}
	
	private static void writeDataAddrList() throws IOException {
		metaFile.seek(8);
		metaFile.writeInt(dataFreeAddressList.size());
		for(int address : dataFreeAddressList) {
			metaFile.writeInt(address);
		}
	}
	
	private static void writeIndexAddrList() throws IOException {
		metaFile.seek(12 + indexFreeAddressList.size()*4);
		metaFile.writeInt(indexFreeAddressList.size());
		for(int address : indexFreeAddressList) {
			metaFile.writeInt(address);
		}
	}
	
	/**
	 * Zapisuje listy z wolnymi adresami i zamyka plik
	 * @return 
	 * @throws IOException 
	 */
	public void close() throws IOException {
		writeRoot(rootPosition);
		writeTreeOrder(treeOrder);
		writeDataAddrList();
		writeIndexAddrList();
		metaFile.close();
	}


	public static RandomAccessFile getMetaFile() {
		return metaFile;
	}


	public static void setMetaFile(RandomAccessFile metaFile) {
		MetadataFile.metaFile = metaFile;
	}


	public static List<Integer> getDataFreeAddressList() {
		return dataFreeAddressList;
	}

	public static List<Integer> getIndexFreeAddressList() {
		return indexFreeAddressList;
	}

	public static int getRootPosition() {
		return rootPosition;
	}

	public static void setRootPosition(int rootPosition) throws IOException {
		MetadataFile.rootPosition = rootPosition;
		writeRoot(rootPosition);
	}

	public static int getTreeOrder() {
		return treeOrder;
	}

	public static void setTreeOrder(int treeOrder) {
		MetadataFile.treeOrder = treeOrder;
	}

	public static boolean isRootModified() {
		return rootModified;
	}

	public static void setRootModified(boolean rootModified) {
		MetadataFile.rootModified = rootModified;
	}
}
