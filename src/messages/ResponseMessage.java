package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResponseMessage extends Message {

	public String status;
	public String statusMessage;
	public String object;
	
	public static ResponseMessage getObjectFromString(String object) {
		Gson gson = new GsonBuilder().setLenient().create();
		return gson.fromJson(object, ResponseMessage.class);
	}
	
	@SuppressWarnings("unused")
	private String type = "ResponseMessage";
}
