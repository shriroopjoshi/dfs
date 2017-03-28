package objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RepoObject {
	
	private static int ID_GEN = 0;
	
	private int id;
	public String contents;

	public int getId() {
		return id;
	}
	
	public void genID() {
		ID_GEN++;
		this.id = ID_GEN;
	}
	
	public String getContents() {
		return this.contents;
	}

	public static RepoObject getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, RepoObject.class);
	}
	
	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
	
	
}
