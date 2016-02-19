package application.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import application.Consts;
import application.geometricSequence.GeometricSequence;

public class DataFile {
	
	private static RandomAccessFile dataFile;
	private static int readsCount;
	private static int savesCount;
	
	public DataFile(String treeName, boolean isNewTree) throws IOException {
		File dataFileObj = new File(treeName + ".data");
		try {
			dataFile = new RandomAccessFile(dataFileObj, "rw");
			if(isNewTree) {
				dataFile.setLength(0);
			}
			savesCount = 0;
			readsCount = 0;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
		}
	}
	
	public static GeometricSequence readValueFromAddress(int address) throws IOException {
		
		byte[] data = new byte[Consts.DATA_RECORD_SIZE];
		dataFile.seek(address);
		dataFile.read(data);
		ByteBuffer wrapper = ByteBuffer.wrap(data);
		GeometricSequence readedData = new GeometricSequence();
		readedData.setId(wrapper.getInt(0));
		readedData.setFirstTerm(wrapper.getDouble(4));
		readedData.setMultiplier(wrapper.getDouble(12));
		
		readsCount++;
		return readedData;
	}
	
	/**
	 * Zapisuje dane i zwraca adres pod który zapisano
	 * @param data
	 * @return
	 * @throws IOException 
	 */
	public static int writeData(GeometricSequence data) throws IOException {
		byte[] dataBytes = new byte[Consts.DATA_RECORD_SIZE];
		ByteBuffer wrapper = ByteBuffer.wrap(dataBytes);
		
		//wrzuc dane do bufora
		wrapper.putInt(data.getId());
		wrapper.putDouble(data.getFirstTerm());
		wrapper.putDouble(data.getMultiplier());
		
		int address;
		if(!MetadataFile.getDataFreeAddressList().isEmpty()) {
			dataFile.seek(MetadataFile.getDataFreeAddressList().get(0));
			dataFile.write(dataBytes);
			address = MetadataFile.getDataFreeAddressList().get(0);
			MetadataFile.getDataFreeAddressList().remove(0);
		} else {
			dataFile.seek(dataFile.length());
			dataFile.write(dataBytes);
			address = (int) dataFile.getFilePointer() - Consts.DATA_RECORD_SIZE;
		}
		savesCount++;
		return address;
	}

	/**
	 * Modyfikacja wartosci pod podanym adresie, nie zmienia indeksu
	 * @param address
	 * @param firstTerm
	 * @param multiplier
	 * @throws IOException
	 */
	public static void modifyRecord(int address, double firstTerm, double multiplier) throws IOException {
		byte[] dataBytes = new byte[Consts.DATA_RECORD_SIZE - Integer.BYTES];
		ByteBuffer wrapper = ByteBuffer.wrap(dataBytes);
		
		wrapper.putDouble(firstTerm);
		wrapper.putDouble(multiplier);
		
		dataFile.seek(address + 4);
		dataFile.write(dataBytes);
		return;
	}
	
	public static void printAllData() throws IOException {
		
		for(int i = 0; i < dataFile.length(); i += Consts.DATA_RECORD_SIZE) {
			if(MetadataFile.getDataFreeAddressList().contains(i)) {
				System.out.print("!");
			}
			GeometricSequence data = readValueFromAddress(i);
			System.out.println("ID: " + data.getId() + "\tPierwszy wyraz: " + data.getFirstTerm() + "\tIloraz: " + data.getMultiplier());
		}
	}
	
	public static void deleteData(Integer address) throws IOException {
		//jeœli wêze³ znajduje sie na koncu pliku - zmniejsz plik
		if(address == dataFile.length() - Consts.DATA_RECORD_SIZE) {
			dataFile.setLength(dataFile.length() - Consts.DATA_RECORD_SIZE);
		} else {
			MetadataFile.getDataFreeAddressList().add(address);
		}
	}

	public static void resetCounters() {
		readsCount = 0;
		savesCount = 0;
	}
	
	public static void cleanFile() throws IOException {
		dataFile.setLength(0);
	}
	
	public static int getReadsCount() {
		return readsCount;
	}
	
	public void close() throws IOException {
		dataFile.close();
	}

	public static int getSavesCount() {
		return savesCount;
	}

	public static RandomAccessFile getDataFile() {
		return dataFile;
	}
}
