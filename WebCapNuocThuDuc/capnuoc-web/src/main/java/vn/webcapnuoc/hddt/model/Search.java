package vn.webcapnuoc.hddt.model;

public class Search {

	private String id;
	private String Name;
	public Search() {
		super();
	}
	public Search(String id, String name) {
		super();
		this.id = id;
		Name = name;
	}
	public Search(String name) {
		super();
		Name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	

	
}
