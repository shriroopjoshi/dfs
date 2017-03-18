package utils;

import java.net.Socket;

import messages.Message;

public class MessageContainer {

	Message message;
	Socket client;
	
	public MessageContainer(Message msg, Socket client) {
		this.message = msg;
		this.client = client;
	}
	
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public Socket getClient() {
		return client;
	}
	public void setClient(Socket client) {
		this.client = client;
	}
}
