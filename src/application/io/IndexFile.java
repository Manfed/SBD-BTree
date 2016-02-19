package application.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;


import application.Consts;
import application.btree.BTreeNode;
import application.btree.Index;

public class IndexFile {
	
	private static RandomAccessFile indexFile;
	private static int savesCount;
	private static int readsCount;
	
	public IndexFile(String name, boolean isNewTree) throws IOException {
		File idxFile = new File(name + ".idx");
		try {
			indexFile = new RandomAccessFile(idxFile, "rw");
			if(isNewTree) {
				indexFile.setLength(0);
			}
			savesCount = 0;
			readsCount = 0;
		} catch (FileNotFoundException e) {
			System.out.println("Nie znaleziono pliku indeksów\n");
			e.printStackTrace();
		}
	}
	
	public IndexFile(String name, Integer order) throws IOException {
		this(name, false);
		MetadataFile.writeTreeOrder(order);
	}
	
	public static int writeNode(BTreeNode node) throws IOException {
		int address = 0;
		
		byte[] nodeBytes = new byte[Consts.getNodeSize(MetadataFile.getTreeOrder())];
		ByteBuffer wrapper = ByteBuffer.wrap(nodeBytes);
		wrapper.putInt(node.getSize());
		if(node.getParent() != null) {
			wrapper.putInt(node.getParent());
		} else {
			wrapper.putInt(-1);
		}
		
		for(int i = 0; i < node.getSize(); i++) {
			if(node.getKeys()[i] != null) {
				wrapper.putInt(node.getKeys()[i].getId());
				wrapper.putInt(node.getKeys()[i].getAddress());
			} else {
				wrapper.putInt(-1);
				wrapper.putInt(-1);
			}
		}
		for(int i = 0; i < node.getSize() + 1; i++) {
			if(node.getChilds()[i] != null) {
				wrapper.putInt(node.getChilds()[i]);
			} else {
				wrapper.putInt(-1);
			}
		}
		if(MetadataFile.getIndexFreeAddressList().isEmpty()) {
			indexFile.seek(indexFile.length());
			indexFile.write(nodeBytes);
			address = (int) (indexFile.getFilePointer() - Consts.getNodeSize(MetadataFile.getTreeOrder()));
		} else {
			indexFile.seek(MetadataFile.getIndexFreeAddressList().get(0));
			indexFile.write(nodeBytes);
			address = MetadataFile.getIndexFreeAddressList().get(0);
			MetadataFile.getIndexFreeAddressList().remove(0);
		}
		savesCount++;
		return address;
	}
	
	/**
	 * Jeœli address == null -> wstawianie roota
	 * @param node
	 * @param address
	 * @throws IOException
	 */
	public static void writeNodeAt(BTreeNode node, Integer address) throws IOException {
		if(address == null) {
			int rootAddr = writeNode(node);
			MetadataFile.setRootPosition(rootAddr);;
			return;
		} else {
		int treeOrder = MetadataFile.getTreeOrder();
		
		byte[] nodeBytes = new byte[Consts.getNodeSize(treeOrder)];
		ByteBuffer wrapper = ByteBuffer.wrap(nodeBytes);
		wrapper.putInt(node.getSize());
		wrapper.putInt(node.getParent() == null ? -1 : node.getParent());
		
		for(int i = 0; i < node.getSize(); i++) {
			if(node.getKeys()[i] != null) {
				wrapper.putInt(node.getKeys()[i].getId());
				wrapper.putInt(node.getKeys()[i].getAddress());
			}
		}
		for(int i = 0; i < node.getSize() + 1; i++) {
			if(node.getChilds()[i] != null) {
				wrapper.putInt(node.getChilds()[i]);
			} else {
				wrapper.putInt(-1);
			}
		}
		indexFile.seek(address);
		indexFile.write(nodeBytes);
		savesCount++;
		}
	}
	
	public static BTreeNode readNodeFromAddress(int address) throws IOException {
		BTreeNode readedNode = new BTreeNode(MetadataFile.getTreeOrder());
		byte[] nodeBytes = new byte[Consts.getNodeSize(MetadataFile.getTreeOrder())];
		
		indexFile.seek(address);
		indexFile.read(nodeBytes);
		
		ByteBuffer wrapper = ByteBuffer.wrap(nodeBytes);
		readedNode.setSize(wrapper.getInt());
		readedNode.setParent(wrapper.getInt() == -1 ? null : wrapper.getInt(4)); 
		
		for(int i = 0; i < readedNode.getSize(); i++) {
			int id = wrapper.getInt(), addr = wrapper.getInt();
			if(id != -1 && addr != -1) {
				readedNode.getKeys()[i] = new Index(id, addr);
			} else {
				readedNode.getKeys()[i] = null;
			}
		}
		for(int i = 0; i < readedNode.getSize() + 1; i++) {
			int child = wrapper.getInt();
			readedNode.getChilds()[i] = (child == -1) ? null : child;
		}
		readedNode.setPosition(address);
		readsCount++;
		return readedNode;
	}
	
	public static void deleteNode(Integer address) throws IOException {
		//jeœli wêze³ znajduje sie na koncu pliku - zmniejsz plik
		if(address == indexFile.length() - Consts.getNodeSize(MetadataFile.getTreeOrder())) {
			indexFile.setLength(indexFile.length() - Consts.getNodeSize(MetadataFile.getTreeOrder()));
		} else {
			MetadataFile.getIndexFreeAddressList().add(address);
		}
	}
	
	public static void setNodeParent(Integer address, Integer newParent) throws IOException {
		indexFile.seek(address + 4);
		indexFile.writeInt(newParent);
	}
	
	public static int getSizeFrom(Integer address) throws IOException {
		indexFile.seek(address);
		return indexFile.readInt();
	}
	
	public static void resetCounters() {
		readsCount = 0;
		savesCount = 0;
	}
	
	public static void cleanFile() throws IOException {
		indexFile.setLength(0);
	}
	

	public void close() throws IOException {
		indexFile.close();
	}

	public static int getSavesCount() {
		return savesCount;
	}

	public static int getReadsCount() {
		return readsCount;
	}

	public static RandomAccessFile getIndexFile() {
		return indexFile;
	}
}
