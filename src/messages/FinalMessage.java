package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FinalMessage extends Message {
	
	public boolean commit;
	public String PREV_OP;

	public static FinalMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, FinalMessage.class);
	}

	@SuppressWarnings("unused")
	private String type = "FinalMessage";
}
