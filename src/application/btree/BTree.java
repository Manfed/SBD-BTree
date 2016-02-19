package application.btree;

import java.io.IOException;
import java.util.Map;

import application.geometricSequence.GeometricSequence;
import application.io.DataFile;
import application.io.IndexFile;
import application.io.MetadataFile;

/**
 * B - Drzewo
 */
public class BTree {

	/**
	 * Korzeñ drzewa
	 */
	private BTreeNode root;

	/**
	 * Rozmiar drzewa ( d )
	 */
	private Integer order;

	public BTree(Integer order) throws IOException {
		this.order = order;
		this.root = new BTreeNode(order);

		// zapis nowego roota do pliku
		int rootPos = IndexFile.writeNode(this.root);
		MetadataFile.writeRoot(rootPos);
		MetadataFile.writeTreeOrder(this.order);
		
		IndexFile.cleanFile();
		DataFile.cleanFile();
		MetadataFile.cleanFile();
		
		this.root.setPosition(rootPos);
	}

	public BTree() throws IOException {
		int rootAddr = MetadataFile.getRootAddress();
		this.order = MetadataFile.getTreeOrder();
		this.root = IndexFile.readNodeFromAddress(rootAddr);
	}

	public void insert(GeometricSequence newKey) throws IOException {
		IndexFile.resetCounters();
		DataFile.resetCounters();
		
		Map.Entry<BTreeNode, Boolean> foundedNode = this.root.searchNodeContaining(newKey.getId());
		
		if (foundedNode != null && !foundedNode.getValue()) {
			foundedNode.getKey().insert(newKey);
			System.out.println("Dodano rekord o ID " + newKey.getId());
		} else {
			System.out.println("Istnieje ju¿ rekord o ID " + newKey.getId());
		}
		
		checkRoot();
		
		System.out.println("Odczyty: " + (IndexFile.getReadsCount() + DataFile.getReadsCount()) + "\tZapisy: " 
				+ (IndexFile.getSavesCount() + DataFile.getSavesCount()));
	}

	public GeometricSequence search(Integer id) throws IOException {
		IndexFile.resetCounters();
		DataFile.resetCounters();
		checkRoot();
		
		GeometricSequence foundedData = getRoot().search(id);
		
		System.out.println("Odczyty: " + (IndexFile.getReadsCount() + DataFile.getReadsCount()) + "\tZapisy: " 
				+ (IndexFile.getSavesCount() + DataFile.getSavesCount()));
		return foundedData;
	}

	public void delete(Integer id) throws IOException {
		IndexFile.resetCounters();
		DataFile.resetCounters();
		
		checkRoot();
		
		Map.Entry<BTreeNode, Boolean> founded = root.searchNodeContaining(id);
		if(founded != null && founded.getValue()) {
			founded.getKey().delete(id);
		} else {
			displayTree();
			System.out.println("Nie mo¿na znaleŸæ rekordu o Id " + id);
		}
		
		System.out.println("Odczyty: " + (IndexFile.getReadsCount() + DataFile.getReadsCount()) + "\tZapisy: " 
				+ (IndexFile.getSavesCount() + DataFile.getSavesCount()));
		return;
	}

	public void modify(Integer id, Double firstTerm, Double multiplier) throws IOException {
		IndexFile.resetCounters();
		DataFile.resetCounters();
		
		Map.Entry<BTreeNode, Boolean> foundedNode = getRoot().searchNodeContaining(id);
		
		if(foundedNode != null && foundedNode.getValue()) {
			foundedNode.getKey().modify(id, firstTerm, multiplier);
		} else {
			System.out.println("Nie istnieje rekord o ID " + id);
		}
		
		checkRoot();
		System.out.println("Odczyty: " + (IndexFile.getReadsCount() + DataFile.getReadsCount()) + "\tZapisy: " 
				+ (IndexFile.getSavesCount() + DataFile.getSavesCount()));
		return;
	}
	
	public void displayTree() throws IOException {
		checkRoot();
		getRoot().displayTree();
	}

	private void checkRoot() throws IOException {
		if(MetadataFile.isRootModified()) {
			this.root = IndexFile.readNodeFromAddress(MetadataFile.getRootAddress());
			MetadataFile.setRootModified(false);
		}
	}

	public BTreeNode getRoot() {
		return root;
	}

	public void setRoot(BTreeNode root) {
		this.root = root;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}