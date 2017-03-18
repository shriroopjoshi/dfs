package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DeleteMessage extends Message {

	public static DeleteMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, DeleteMessage.class);
	}
}
