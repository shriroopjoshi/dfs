package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ReadMessage extends Message {

	public static ReadMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, ReadMessage.class);
	}
	
	@SuppressWarnings("unused")
	private String type = "ReadMessage";
}
