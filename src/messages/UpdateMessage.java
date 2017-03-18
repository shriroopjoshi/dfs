package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UpdateMessage extends Message {

	public String newObject;
	
	public static UpdateMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, UpdateMessage.class);
	}
}
