package messages;

import com.google.gson.GsonBuilder;

public abstract class Message {
	
	protected String sender;
	protected String senderAddress;
	protected String receiver;
	protected String receiverAddress;
	
	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
}
