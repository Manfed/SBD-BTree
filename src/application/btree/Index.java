package application.btree;

public class Index implements Comparable<Index>{
	
	private Integer id;
	private Integer address;
	
	public Index(Integer id, Integer address) {
		this.id = id;
		this.address = address;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAddress() {
		return address;
	}

	public void setAddress(Integer address) {
		this.address = address;
	}

	@Override
	public int compareTo(Index o) {
		return getId() < o.getId() ? -1 : getId() == o.getId() ? 0 : 1;
	}
	
	
}
