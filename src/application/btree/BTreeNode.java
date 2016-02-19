package application.btree;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import application.Consts;
import application.geometricSequence.GeometricSequence;
import application.io.DataFile;
import application.io.IndexFile;
import application.io.MetadataFile;

/**
 *	Wêze³ B-Drzewa
 */
public class BTreeNode {
	
	private Integer position;
	private Integer childs[];
	private Index keys[];
	private Integer parent;
	/**
	 * liczba zajêtych kluczy
	 */
	private Integer size;
	
	public BTreeNode(Integer position, Integer order) {
		this.position = position;
		this.childs = new Integer[2*order + 1];
		this.keys = new Index[2*order];
		this.size = 0;
	}
	
	public BTreeNode(Integer parent, Integer position, Integer order) {
		this.position = position;
		this.parent = parent;
		this.childs = new Integer[2*order + 1];
		this.keys = new Index[2*order];
		this.size = 0;
	}
	
	public BTreeNode(Integer order) {
		this(null, order);
	}
	
	//wypisywanie
	//----------------------------------------------------------------------------------------
	
	public void displayTree() throws IOException {
		if(isRoot()) {
			displayTree(0);
		} else {
			System.out.println("Nie mogê wyœwietliæ drzewa");
		}
	}
	
	private void displayTree(int level) throws IOException {
		for(int i = 0; i < level; i++) {
			System.out.print("\t");
		}
		//System.out.print("Par: " + getParent() + " || Pos: " + getPosition() + " || ");
		for(int i = 0; i < getKeys().length; i++) {
			if(getKeys()[i] != null) {
				System.out.print(getKeys()[i].getId() + " | ");
			} else {
				System.out.print("_ |");
			}
		}
		System.out.println();
		for(int i = 0; i < getSize() + 1; i++) {
			if(getChilds()[i] != null) {
				BTreeNode child = IndexFile.readNodeFromAddress(getChilds()[i]);
				child.displayTree(level + 1);
			}
		}
	}
	
	//wyszukiwanie
	//----------------------------------------------------------------------------------------
	
	public GeometricSequence search(Integer id) throws IOException{
		for(int i = 0; i < getSize(); i++) {
			if(getKeys()[i] != null && getKeys()[i].getId().equals(id)) {
				GeometricSequence founded = DataFile.readValueFromAddress(getKeys()[i].getAddress());
				return founded;
			}
		}
		if(getSize() == 0) {
			return null;
		}
		Integer nextNodeAddr = null;
		if(getKeys()[0] != null && getKeys()[0].getId() > id) {
			nextNodeAddr = getChilds()[0];
		} else if(getKeys()[getSize() - 1] != null && getKeys()[getSize() - 1].getId() < id) {
			nextNodeAddr = getChilds()[getSize()];
		} else {
			for(int i = 1; i < getKeys().length - 1; i++) {
				if(getKeys()[i] != null && getKeys()[i].getId() > id) {
					nextNodeAddr = getChilds()[i];
					break;
				}
			}
		}
		if(nextNodeAddr != null) {
			BTreeNode nextNode = IndexFile.readNodeFromAddress(nextNodeAddr);
			return nextNode.search(id);
		}
		return null;
	}
	
	/**
	 * Funkcja szuka wêz³a w którym znajduje lub powinien znajdowaæ siê rekord o podanym id
	 * Zwraca wêze³ i informacje czy szukany weze³ siê w nim znajduje
	 * @param id
	 * @return Map.Entry - klucz zawiera wêze³, wartoœæ - czy jest w nim szukany rekord
	 * @throws IOException
	 */
	public Map.Entry<BTreeNode, Boolean> searchNodeContaining(Integer id) throws IOException {
		for(int i = 0; i < getSize(); i++) {
			if(getKeys()[i] != null && getKeys()[i].getId().equals(id)) {
				Map.Entry<BTreeNode, Boolean> founded = new AbstractMap.SimpleEntry<BTreeNode, Boolean>(this, Boolean.TRUE);
				return founded;
			}
		}
		//je¿eli jesteœmy w liœciu, a poprzednia pêtla nie znalaz³a szukanego elementu zwracamy wêze³ i info o braku
		//szukanego rekordu
		if(this.isLeaf()) {
			Map.Entry<BTreeNode, Boolean> founded = new AbstractMap.SimpleEntry<BTreeNode, Boolean>(this, Boolean.FALSE);
			return founded;
		}
		if(getSize() == 0) {
			return null;
		}
		Integer nextNodeAddr = null;
		if(getKeys()[0] != null && getKeys()[0].getId() > id) {
			nextNodeAddr = getChilds()[0];
		} else if(getKeys()[getSize() - 1] != null && getKeys()[getSize() - 1].getId() < id) {
			nextNodeAddr = getChilds()[getSize()];
		} else {
			for(int i = 1; i < getKeys().length - 1; i++) {
				if(getKeys()[i] != null && getKeys()[i].getId() > id) {
					nextNodeAddr = getChilds()[i];
				}
			}
		}
		if(nextNodeAddr != null) {
			BTreeNode nextNode = IndexFile.readNodeFromAddress(nextNodeAddr);
			return nextNode.searchNodeContaining(id);
		}
		return null;
	}
	//podzia³ liœcia
	//_______________________________________________________________________________________________________________
	private void split(Index newValue) throws IOException {
		Index[] newKeys = new Index[getSize() + 1];
		for(int i = 0; i < getSize(); i++) {
			newKeys[i] = getKeys()[i];
		}
		newKeys[newKeys.length - 1] = newValue;
		Arrays.sort(newKeys);
		Index middle = null;
		BTreeNode newNode = new BTreeNode(MetadataFile.getTreeOrder());
		
		newNode.setKeys(Arrays.copyOfRange(newKeys, 0, newKeys.length / 2));
		newNode.setSize(newKeys.length / 2);
		newNode.setParent(getParent());
		
		middle = newKeys[newKeys.length / 2];
		
		setKeys(Arrays.copyOfRange(newKeys, (newKeys.length / 2) + 1, newKeys.length));
		setSize(newKeys.length / 2);
		
		IndexFile.writeNodeAt(this, getPosition());
		newNode.setPosition(IndexFile.writeNode(newNode));
		insertSplittedNode(newNode.getPosition(), middle, this.getPosition());
	}
	
	/*private void split(Index newValue) throws IOException {
		//jeœli mo¿na umieœciæ now¹ wartoœæ bez dzielenia
		if(getSize() < MetadataFile.getTreeOrder()*2) {
			getKeys()[getSize()] = newValue;
			Arrays.sort(getKeys());
			setSize(getSize() + 1);
		} else if(getParent() != null) {
			//jeœli potrzebny split
			BTreeNode nodeParent = IndexFile.readNodeFromAddress(getParent());
			int nodePos = getPositionInParent(nodeParent, getPosition());
			if(nodePos == -1) {
				System.out.println("Podzia³ niemo¿liwy");
				return;
			}
			
		} else {
			//stworzenie nowego roota
			splitRoot(newValue);
		}
	}
	
	private void split(Index newValue, Integer newRightChild) {
		
	}
	
	/**
	 * Tworzy nowego root'a. Stary root zostanie lewym dzieckiem nowego. Prawe dziecko jest tworzone.
	 * @param newValue
	 */
	/*private void splitRoot(Index newValue) throws IOException {
		BTreeNode newRoot = new BTreeNode(IndexFile.getFirstFreeAddr(), MetadataFile.getTreeOrder());
		BTreeNode newRightChild = new BTreeNode(IndexFile.getFirstFreeAddr() ,MetadataFile.getTreeOrder());
		/*newRoot.getKeys()[0] = middleValue;
		newRoot.getChilds()[0] = leftNode;
		newRoot.getChilds()[1] = rightNode;
		newRoot.setSize(1);
		int newRootPos = IndexFile.writeNode(newRoot);
		IndexFile.setNodeParent(leftNode, newRootPos);
		IndexFile.setNodeParent(rightNode, newRootPos);
		MetadataFile.setRootPosition(newRootPos);
		MetadataFile.setRootModified(true);*/
		/*Index newKeys[] = new Index[getSize() + 1];
		Integer childs[] = new Integer[getSize() + 2];
		
		for(int i = 0; i < getSize(); i++) {
			newKeys[i] = getKeys()[i];
		}
		newKeys[getSize()] = newValue;
		Arrays.sort(newKeys);
		
		//przepisanie dzieci
		for(int i = 0; i < getChilds().length + 1; i++) {
			if(newKeys[i] == newValue) {
				childs[i] = getChilds()[i];
				childs[i + 1] = 
				i++;
			} else { 
				childs[i] = getChilds()[i];
			}
		}
		
		//liczba rekordow w wêŸle + wstawiany - zawsze nieparzysta
		setKeys(Arrays.copyOfRange(newKeys, 0, newKeys.length / 2));
		setSize(newKeys.length / 2);
		
		newRightChild.setKeys(Arrays.copyOfRange(newKeys, (newKeys.length / 2) + 1, newKeys.length));
		newRightChild.setSize(newKeys.length / 2);
		
		//ustawienie nowego roota
		newRoot.getKeys()[0] = newKeys[newKeys.length / 2];
		newRoot.getChilds()[0] = this.getPosition();
		newRoot.getChilds()[1] = newRightChild.getPosition();
		
	}*/
	/**
	 * podzia³ wêz³a wewn¹trz drzewa
	 * istniejacy weze³ zostaje prawym sasiadem - lewy jest tworzony
	 * @param leftNode
	 * @param middleValue
	 * @param rightNode
	 * @throws IOException 
	 */
	private void split(Integer leftNode, Index middleValue, Integer rightNode) throws IOException {
		Index[] newKeys = new Index[getSize() + 1];
		Integer[] newChilds = new Integer[getSize() + 2];
		
		for(int i = 0; i < getSize(); i++) {
			newKeys[i] = getKeys()[i];
		}
		newKeys[getSize()] = middleValue;
		Arrays.sort(newKeys);
		
		for(int i = 0; i < getChilds().length + 1; i++) {
			if(newKeys[i] == middleValue) {
				newChilds[i] = leftNode;
				newChilds[i + 1] = rightNode;
				i++;
			} else { 
				newChilds[i] = getChilds()[i];
			}
		}
		
		BTreeNode leftChild = new BTreeNode(MetadataFile.getTreeOrder());
		
		for(int i = 0; i < newKeys.length / 2; i++) {
			leftChild.getKeys()[i] = newKeys[i];
			getKeys()[i] = newKeys[(newKeys.length / 2) + i + 1];
		}
		for(int i = 0; i < newChilds.length / 2; i++) {
			leftChild.getChilds()[i] = newChilds[i];
			getChilds()[i] = newChilds[i + (newChilds.length / 2)];
		}
		setSize(newKeys.length / 2);
		leftChild.setSize(newKeys.length / 2);
		
		leftChild.setParent(getParent());
		
		int rightChildPos = getPosition(),
				leftChildPos = IndexFile.writeNode(leftChild);
		//zmiana rodziców dla wêz³ów przypisanych do nowego dziecka
		for(int i = 0; i < leftChild.getSize() + 1; i++) {
			if(leftChild.getChilds()[i] != null) {
				IndexFile.setNodeParent(leftChild.getChilds()[i], leftChildPos);
			}
		}
		IndexFile.writeNodeAt(this, getPosition());
		
		middleValue = newKeys[newKeys.length / 2];
		
		insertSplittedNode(leftChildPos, middleValue, rightChildPos);
	}
	
/**
 * Umieszcza podzielony weze³ w drzewie, rozró¿nia sytuacje dla roota i wêz³a
 * @param leftNode - adres lewego wezla
 * @param middleValue - wartosc do wstawienia
 * @param rightNode	- adres prawego wezla
 * @throws IOException 
 */
	private void insertSplittedNode(Integer leftNode, Index middleValue, Integer rightNode) throws IOException {
		if(isRoot()) {
			//stworz nowy wezel roota
			BTreeNode newRoot = new BTreeNode(MetadataFile.getTreeOrder());
			newRoot.getKeys()[0] = middleValue;
			newRoot.getChilds()[0] = leftNode;
			newRoot.getChilds()[1] = rightNode;
			newRoot.setSize(1);
			int newRootPos = IndexFile.writeNode(newRoot);
			IndexFile.setNodeParent(leftNode, newRootPos);
			IndexFile.setNodeParent(rightNode, newRootPos);
			MetadataFile.setRootPosition(newRootPos);
			MetadataFile.setRootModified(true);
		} else {
			BTreeNode parent = IndexFile.readNodeFromAddress(getParent());
			if(!parent.isFull()) {
				parent.getKeys()[parent.getSize()] = middleValue;
				parent.setSize(parent.getSize() + 1);
				Arrays.sort(parent.getKeys(), 0, parent.getSize());
				int newValueIndex = -1;
				
				for(int i = 0; i < parent.getSize(); i++) {
					if(parent.getKeys()[i].getId() == middleValue.getId()) {
						newValueIndex = i;
						break;
					}
				}
				for(int i = parent.getSize() - 1; i > newValueIndex; i--) {
					parent.getChilds()[i + 1] = parent.getChilds()[i];
				}
				parent.getChilds()[newValueIndex] = leftNode;
				parent.getChilds()[newValueIndex + 1] = rightNode;
				
				IndexFile.writeNodeAt(parent, parent.getPosition());
				IndexFile.setNodeParent(leftNode, parent.getPosition());
				IndexFile.setNodeParent(rightNode, parent.getPosition());
				
				if(parent.isRoot()) {
					MetadataFile.setRootModified(true);
				}
			} else {
				parent.split(leftNode, middleValue, rightNode);
			}
		}
	}
	
	//_______________________________________________________________________________________________________________
	
	// Kompensacja
	//_______________________________________________________________________________________________________________
	
	/**
	 * Kompensacja wêz³ow podczas dodawania
	 * @param newValue
	 * @return
	 * @throws IOException
	 */
	private boolean compensate(Index newValue) throws IOException {
		if(this.getParent() != null) {
			BTreeNode parent = IndexFile.readNodeFromAddress(this.getParent());
			int nodeIndex = getPositionInParent(parent, getPosition());
			if(nodeIndex == -1) {
				return false;
			}
			//sprawdzenie sasiada po prawej stronie
			if(nodeIndex < MetadataFile.getTreeOrder()*2 /*Jeœli nie jest to najbardziej prawy element*/
					&& parent.getChilds()[nodeIndex + 1] != null) {
				BTreeNode rightNeighbour = IndexFile.readNodeFromAddress(parent.getChilds()[nodeIndex + 1]);
				if(rightNeighbour.getSize() + this.getSize() + (newValue != null ? 1 : 0) >= 2*MetadataFile.getTreeOrder()
						&& rightNeighbour.getSize() + this.getSize() <  4*MetadataFile.getTreeOrder()) {
					return compensate3Nodes(this, parent, rightNeighbour, nodeIndex, true, newValue);
				}
			}
			if(nodeIndex > 0 && parent.getChilds()[nodeIndex - 1] != null) {
				//sprawdzenie s¹siada po lewej
				BTreeNode leftNeighbour = IndexFile.readNodeFromAddress(parent.getChilds()[nodeIndex - 1]);
				if(leftNeighbour.getSize() + this.getSize() + (newValue != null ? 1 : 0) >= 2*MetadataFile.getTreeOrder()
						&& leftNeighbour.getSize() + this.getSize() <  4*MetadataFile.getTreeOrder()) {
					return compensate3Nodes(leftNeighbour, parent, this, nodeIndex, false, newValue);
				}
			}
		}
		return false;
	}
	
	/**
	 * podzia³ rekordów z 3 wêz³ow z dodaniem nowego wêz³a - kompensacja
	 * @param leftNode
	 * @param parent
	 * @param rightNode
	 * @param parentIndex
	 * @param compareWithRightNeigh
	 * @param newValue
	 * @return
	 * @throws IOException
	 */
	private boolean compensate3Nodes(BTreeNode leftNode, BTreeNode parent, BTreeNode rightNode,
					int parentIndex, boolean compareWithRightNeigh, Index newValue) throws IOException {
		int nodesCount = leftNode.getSize() + 1 + rightNode.getSize() + (newValue != null ? 1 : 0);
		if(nodesCount >= MetadataFile.getTreeOrder()*2 + 1) {
			Index[] allKeys = new Index[nodesCount];
			
			//przepisanie kluczy
			for(int i = 0; i < leftNode.getSize(); i++) {
				allKeys[i] = leftNode.getKeys()[i];
			}
			allKeys[leftNode.getSize()] = parent.getKeys()[compareWithRightNeigh ? parentIndex : parentIndex - 1];
			for(int i = 0; i < rightNode.getSize(); i++) {
				allKeys[i + leftNode.getSize() + 1] = rightNode.getKeys()[i];
			}
			if(newValue != null) {
				allKeys[allKeys.length - 1] = newValue;
			}
			Arrays.sort(allKeys);
			
			leftNode.setKeys(Arrays.copyOfRange(allKeys, 0, allKeys.length / 2));
			leftNode.setSize(leftNode.getKeys().length);
			
			parent.getKeys()[compareWithRightNeigh ? parentIndex : parentIndex - 1] = allKeys[allKeys.length / 2];
	
			rightNode.setKeys(Arrays.copyOfRange(allKeys, (allKeys.length / 2) + 1, allKeys.length));
			rightNode.setSize(rightNode.getKeys().length);
			
			//jeœli kompensacja przeprowadzana po usunieciu elementu - przepisz odpowiednio adresy dzieci wêz³ów
			if(newValue == null) {
				
				List<Integer> allChilds = new ArrayList<>();
				
				//przepisanie adresow dzieci kompensowanych wez³ów
				for(int i = 0; i < leftNode.getChilds().length; i++) {
					if(leftNode.getChilds()[i] != null) {
						allChilds.add(leftNode.getChilds()[i]);
						IndexFile.setNodeParent(leftNode.getChilds()[i], leftNode.getPosition());
					} else {
						break;
					}
				}
				for(int i = 0; i < rightNode.getChilds().length; i++) {
					if(rightNode.getChilds()[i] != null) {
						allChilds.add(rightNode.getChilds()[i]);
						IndexFile.setNodeParent(rightNode.getChilds()[i], rightNode.getPosition());
					} else {
						break;
					}
				}
				if(allChilds.size() > 0) {
					Integer[] childs = Arrays.copyOf(allChilds.toArray(), allChilds.size(), Integer[].class);
					leftNode.setChilds(Arrays.copyOfRange(childs, 0, leftNode.getSize() + 1));
					rightNode.setChilds(Arrays.copyOfRange(childs, leftNode.getSize() + 1, childs.length));
				}
			}
			
			IndexFile.writeNodeAt(leftNode, leftNode.getPosition());
			IndexFile.writeNodeAt(parent, parent.getPosition());
			IndexFile.writeNodeAt(rightNode, rightNode.getPosition());
			
			if(parent.isRoot()) {
				MetadataFile.setRootModified(true);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	//_______________________________________________________________________________________________________________

	public void insert(GeometricSequence data) throws IOException {
		if(isLeaf()) {
			if(!isFull()) {
				insertValue(data);
			} else {
				Index newVal = new Index(data.getId(), DataFile.writeData(data));
				if(!compensate(newVal)) {
					split(newVal);
				}
			}
		} else {
			Integer nextNodeAddr = findNextNode(data.getId());
			if(nextNodeAddr == null) {
				return;
			}
			BTreeNode nextNode = IndexFile.readNodeFromAddress(nextNodeAddr);
			nextNode.insert(data);
		}
	}
	
	private Integer findNextNode(Integer id) {
		Integer nextNodeAddr = null;
		for(int i = 0; i < getSize(); i++) {
			if(getKeys()[i].getId() > id) {
				nextNodeAddr = getChilds()[i];
				break;
			}
		}
		if(nextNodeAddr == null && getChilds()[getSize()] != null) {
			nextNodeAddr = getChilds()[getSize()];
		}
		return nextNodeAddr;
	}
	
	/**
	 * Dodaj klucz do liœcia
	 * @param data
	 * @throws IOException
	 */
	private void insertValue(GeometricSequence data) throws IOException {
		Index newIndex = new Index(data.getId(), DataFile.writeData(data));
		
		getKeys()[getSize()] = newIndex;
		setSize(getSize() + 1);
		Arrays.sort(getKeys(), 0, getSize());
		
		IndexFile.writeNodeAt(this, this.position);
	}

	public boolean isLeaf() {
		for(int i = 0; i < childs.length; i++) {
			if(childs[i] != null) {
				return false;
			}
		}
		return true;
	}
	
	//____________________________________________________________________________
	
	public void modify(Integer id, double firstTerm, double multiplier) throws IOException {
		for(int i = 0; i < getSize(); i++) {
			if(getKeys()[i].getId() == id) {
				DataFile.modifyRecord(getKeys()[i].getAddress(), firstTerm, multiplier);
				System.out.println("Zaktualizowano rekord o ID " + id);
			}
		}
		return;
	}
	
	//____________________________________________________________________________
	
	public boolean isFull() {
		if(getSize() < MetadataFile.getTreeOrder() * 2) {
			return false;
		}
		return true;
	}
	
	public boolean isUnderflow() {
		int counter = 0;
		for(int i = 0; i < getSize(); i++) {
			if(keys[i] != null) {
				counter++;
			}
		}
		if(counter < MetadataFile.getTreeOrder() / 2) {
			return true;
		}
		return false;
	}
	
	public boolean isRoot() {
		return (parent == null);
	}
	
	//----------------------------------------------------------------------------------------
	
	/**
	 * USUWANIE
	 */

	public void delete(Integer id) throws IOException {
		BTreeNode leaf = null;
		if(!isLeaf()) {
			leaf = deleteFromNonLeaf(id);
			if(leaf == null) {
				System.out.println("Nie mo¿na usun¹æ rekordu - drzewo osi¹gnê³o minimalny rozmiar");
				return;
			}
		} else {
			if(deleteFromLeaf(id)) {
				leaf = this;
			} else {
				System.out.println("Nie mo¿na usun¹æ rekordu - drzewo osi¹gnê³o minimalny rozmiar");
				return;
			}
		}
		
		if(leaf.getSize() < MetadataFile.getTreeOrder()) {
			if(!leaf.compensate(null)) {
				leaf.merge();
			}
		} else {
			IndexFile.writeNodeAt(leaf, leaf.getPosition());
		}
		System.out.println("Usuniêto rekord o ID: " + id);
	}
	
	/**
	 * Funkcja zwraca liœæ, z którego zosta³a podmieniona wartoœæ
	 * @param id
	 * @return
	 * @throws IOException 
	 */
	private BTreeNode deleteFromNonLeaf(Integer id) throws IOException {
		if(isDeletePossible()) {
			int recordIndex = -1;
			BTreeNode leaf = null;
			/**
			 * flaga oznaczaj¹ca, czy liœæ pochodzi z lewego, czy prawego poddrzewa
			 */
			Boolean isFromLSubtree = null;
			for(int i = 0; i < getSize(); i++) {
				if(getKeys()[i].getId().equals(id)) {
					recordIndex = i;
					DataFile.deleteData(getKeys()[i].getAddress());
					break;
				}
			}
			if(recordIndex == -1) {
				recordIndex = getSize();
			}
			//schodzimy do liœcia lewego\prawego poddrzewa zawierajacego najwiekszy\najmniejszy liœæ
			if(getChilds()[recordIndex] != null) {
				leaf = IndexFile.readNodeFromAddress(getChilds()[recordIndex]);
				while(!leaf.isLeaf()) {
					leaf = IndexFile.readNodeFromAddress(leaf.getChilds()[leaf.getSize()]);
				}
				isFromLSubtree = true;
			} else if(getChilds()[recordIndex + 1] != null) {
				leaf = IndexFile.readNodeFromAddress(getChilds()[recordIndex + 1]);
				while(!leaf.isLeaf()) {
					leaf = IndexFile.readNodeFromAddress(leaf.getChilds()[0]);
				}
				isFromLSubtree = false;
			}
			if(isFromLSubtree) {
				getKeys()[recordIndex] = leaf.getKeys()[leaf.getSize() - 1];
				leaf.getKeys()[leaf.getSize() - 1] = null;
				leaf.setSize(leaf.getSize() - 1);
			} else {
				getKeys()[recordIndex] = leaf.getKeys()[0];
				leaf.getKeys()[0] = null;
				leaf.setSize(leaf.getSize() - 1);
				leaf.setKeys(Arrays.copyOfRange(leaf.getKeys(), 1, leaf.getSize() + 1));
			}
			
			IndexFile.writeNodeAt(this, getPosition());
			if(isRoot()) {
				MetadataFile.setRootModified(true);
			}
			return leaf;
		}
		return null;
	}
	
	private boolean deleteFromLeaf(Integer id) throws IOException {
		if(isDeletePossible()) {
			Index[] newKeys = new Index[2* MetadataFile.getTreeOrder()];
			int j = 0;
			for(int i = 0; i < getSize(); i++) {
				if(!getKeys()[i].getId().equals(id)) {
					newKeys[j++] = getKeys()[i];
				} else {
					DataFile.deleteData(getKeys()[i].getAddress());
				}
			}
			setKeys(newKeys);
			setSize(getSize() - 1);
			return true;
		}
		return false;
	}
	
	private boolean isDeletePossible() throws IOException {
		int recordsCount = 0;
		if(isRoot()) {
			recordsCount = getSize();
			for(int i = 0; i < getSize() + 1; i++) {
				if(getChilds()[i] != null) {
					recordsCount += IndexFile.getSizeFrom(getChilds()[i]);
				}
			}
			return (recordsCount > Consts.minimalTreeSize(MetadataFile.getTreeOrder()));
		} else if(getParent() != null && getParent().equals(MetadataFile.getRootPosition())) {
			if(IndexFile.getSizeFrom(getParent()) == 1) {
				recordsCount = 1;
				BTreeNode parent = IndexFile.readNodeFromAddress(getParent());
				for(int i = 0; i < parent.getSize() + 1; i++) {
					recordsCount += IndexFile.getSizeFrom(parent.getChilds()[i]);
				}
				return (recordsCount > Consts.minimalTreeSize(MetadataFile.getTreeOrder()));
			}
		}
		return true;
	}
	
	/**
	 * Po³¹czenie wêz³ów po usuwaniu rekordu
	 * @throws IOException 
	 */
	private void merge() throws IOException {
		if(!isRoot()) {
			BTreeNode parent = IndexFile.readNodeFromAddress(getParent());
			if(parent.isRoot() && parent.getSize() == 1) {
				parent.mergeRoot(this);
				return;
			}
			int nodePosition = getPositionInParent(parent, getPosition());
			BTreeNode neighbour = null;
			boolean mergeWithRightN = false;
			
			//próba po³¹czenia z prawym s¹siadem
			if(nodePosition + 1 <= parent.getSize() && parent.getChilds()[nodePosition + 1] != null) {
				neighbour = IndexFile.readNodeFromAddress(parent.getChilds()[nodePosition + 1]);
				mergeWithRightN = true;
			} else if(nodePosition - 1 >= 0 && parent.getChilds()[nodePosition - 1] != null) {
				//po³¹czenie z lewym s¹siadem
				neighbour = IndexFile.readNodeFromAddress(parent.getChilds()[nodePosition - 1]);
			}
			
			if(mergeWithRightN) {
				merge3Nodes(this, parent, neighbour, nodePosition, mergeWithRightN);
			} else {
				merge3Nodes(neighbour, parent, this, nodePosition, mergeWithRightN);
			}
			if(parent.getSize() < MetadataFile.getTreeOrder() && !parent.compensate(null)) {
				parent.merge();
			}
			if(parent.isRoot()) {
				MetadataFile.setRootModified(true);
			}
			assert(parent.getSize() == 0);
			IndexFile.writeNodeAt(parent, parent.getPosition());
		}
		return;
	}
	
	/**
	 * Funkcja ³¹cz¹ca roota z 1 rekordem z jego dzieæmi -> wpisuje klucze dzieci do roota + zmienia rodzica dla nowych dzieci
	 * @param callingChild -> dziecko wywo³uj¹ce merge (merge idzie od liœcia do roota)
	 * @throws IOException 
	 */
	private void mergeRoot(BTreeNode callingChild) throws IOException {
		BTreeNode firstChild = null, secondChild = null;
		
		if(getChilds()[0].equals(callingChild.getPosition())) {
			firstChild = callingChild;
			secondChild = IndexFile.readNodeFromAddress(getChilds()[1]);
		} else {
			secondChild = callingChild;
			firstChild= IndexFile.readNodeFromAddress(getChilds()[0]);
		}
		
		List<Index> allKeys = new ArrayList<>(Arrays.asList(callingChild.getKeys()));
		allKeys.addAll(Arrays.asList(secondChild.getKeys()));
		allKeys.add(getKeys()[0]);
		//usuniecie null'i z listy
		allKeys.removeAll(Collections.singleton(null));
		//usuniecie powtórki klucza - wo³aj¹cy weze³ ma powtórkê klucza usuwan¹ podczas zapisu do pliku
		allKeys.remove(callingChild.getKeys()[getSize()]);
		
		Collections.sort(allKeys);
		
		//przypisanie dzieci potomków do roota
		for(int i = 0; i < firstChild.getSize() + 1; i++) {
			if(firstChild.getChilds()[i] != null) {
				getChilds()[i] = firstChild.getChilds()[i];
				IndexFile.setNodeParent(firstChild.getChilds()[i], getPosition());
			}
		}
		for(int i = 0; i < secondChild.getSize() + 1; i++) {
			if(secondChild.getChilds()[i] != null) {
				getChilds()[i + firstChild.getSize() + 1] = secondChild.getChilds()[i];
				IndexFile.setNodeParent(secondChild.getChilds()[i], getPosition());
			}
		}
		//ustawienie parametrow roota
		setKeys(Arrays.copyOf(allKeys.toArray(), allKeys.size(), Index[].class));
		setSize(getKeys().length);
		//usuniecie dzieci :)
		IndexFile.deleteNode(firstChild.getPosition());
		IndexFile.deleteNode(secondChild.getPosition());
		
		IndexFile.writeNodeAt(this, getPosition());
		
		MetadataFile.setRootModified(true);
	}
	
	/**
	 * Funkcja przeprowadzaj¹ca ³¹czenie wêz³ów - ³¹czymy w lewym wêŸle, prawy jest usuwany
	 * @param leftNode - lewy wêze³
	 * @param parent - rodzic
	 * @param rightNode - prawy wêze³
	 * @param nodePosition - pozycja mniejszego wêz³a w rodzicu
	 * @param isRightNeigh - czy ³¹czymy z prawym s¹siadem - potrzebne do wyznaczenia rekordu do usuniecia w rodzicu
	 * @throws IOException 
	 */
	private void merge3Nodes(BTreeNode leftNode, BTreeNode parent, BTreeNode rightNode, int nodePosition, boolean isRightNeigh) throws IOException {
		int nodesCount = leftNode.getSize() + 1 + rightNode.getSize();
		if(nodesCount <= MetadataFile.getTreeOrder() * 2) {
			int j = 0, k = 0;
			Index[] allKeys = new Index[nodesCount];
			Integer[] allChilds = new Integer[nodesCount + 1];
			
			//kopiowanie kluczy do jednej tablicy
			for(int i = 0; i < leftNode.getSize(); i++) {
				allKeys[j++] = leftNode.getKeys()[i];
			}
			allKeys[j++] = parent.getKeys()[isRightNeigh ? nodePosition : nodePosition - 1];
			for(int i = 0; i < rightNode.getSize(); i++) {
				allKeys[j++] = rightNode.getKeys()[i];
			}
			Arrays.sort(allKeys);
			
			//kopiowanie adresów dzieci
			for(int i = 0; i < leftNode.getSize() + 1; i++) {
				allChilds[k++] = leftNode.getChilds()[i];
			}
			for(int i = 0; i < rightNode.getSize() + 1; i++) {
				allChilds[k++] = rightNode.getChilds()[i];
				if(rightNode.getChilds()[i] != null) {
					IndexFile.setNodeParent(rightNode.getChilds()[i], leftNode.getPosition());
				}
			}
			
			leftNode.setKeys(allKeys);
			leftNode.setSize(allKeys.length);
			leftNode.setChilds(allChilds);
			
			parent.getKeys()[isRightNeigh ? nodePosition : nodePosition - 1] = null;
			for(int i = (isRightNeigh ? nodePosition : nodePosition - 1); i < parent.getSize() - 1; i++) {
				parent.getKeys()[i] = parent.getKeys()[i + 1];
			}
			parent.setSize(parent.getSize() - 1);
			Arrays.sort(parent.getKeys(), 0, parent.getSize());
			
			//usuniecie odwo³ania do dziecka w rodzicu
			for(int i = isRightNeigh ? nodePosition + 1 : nodePosition ; i < parent.getSize() + 1; i++) {
				parent.getChilds()[i] = parent.getChilds()[i + 1];
				parent.getChilds()[i + 1] = null;
			}
			
			IndexFile.deleteNode(rightNode.getPosition());
			IndexFile.writeNodeAt(leftNode, leftNode.getPosition());
		}
	}
	
	//----------------------------------------------------------------------------------------
	
	private int getPositionInParent(BTreeNode parent, int position) {
		int nodePosition = -1;
		for(int i = 0; i < parent.getSize() + 1; i++) {
			if(parent.getChilds()[i] != null && parent.getChilds()[i].equals(position)) {
				nodePosition = i;
				break;
			}
		}
		return nodePosition;
	}
	
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer parent) {
		this.position = parent;
	}

	public Integer[] getChilds() {
		return childs;
	}

	public void setChilds(Integer[] childs) {
		this.childs = childs;
	}

	public Index[] getKeys() {
		return keys;
	}

	public void setKeys(Index[] keys) {
		this.keys = keys;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
