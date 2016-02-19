package application;

public class Consts {
	public static final int DATA_RECORD_SIZE = 20;
	
	public static int getNodeSize(int treeOrder) {
		//4B rodzic + 4B rozmiar + 2*(order+1)*4B adres dziecka  + 2*order* 8B na ID i adres danej
		return 24*treeOrder + 12;
	}
	
	public static int minimalTreeSize(int treeOrder) {
		return 1 + 2 * treeOrder;
	}
}