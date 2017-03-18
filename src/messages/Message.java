package messages;

import com.google.gson.GsonBuilder;

public abstract class Message {
	
	public String sender;
	public String senderAddress;
	public String receiver;
	public String receiverAddress;
	
	public int objectID;
	
	public int getObjectID() {
		return objectID;
	}


	public void setObjectID(int objectID) {
		this.objectID = objectID;
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
	
}
