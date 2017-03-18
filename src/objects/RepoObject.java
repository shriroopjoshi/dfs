package objects;

public class RepoObject {
	
	private static int ID_GEN = 0;
	
	private int id;
	
	public RepoObject() {
		this.id = ++ID_GEN;
	}

	public int getId() {
		return id;
	}
}
