package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InsertMessage extends Message {
	
	public String object;
	
	public static InsertMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, InsertMessage.class);
	}
}
