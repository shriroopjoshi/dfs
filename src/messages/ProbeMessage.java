package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProbeMessage extends Message {
	
	public String object;

	public static ProbeMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, ProbeMessage.class);
	}

	@SuppressWarnings("unused")
	private String type = "ProbeMessage";
}
