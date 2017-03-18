package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import messages.DeleteMessage;
import messages.InsertMessage;
import messages.Message;
import messages.ReadMessage;
import messages.UpdateMessage;
import utils.Commons;
import utils.Constants;
import utils.MessageContainer;

public class Server {

	ServerSocket server;
	private int id;
	LinkedBlockingQueue<MessageContainer> inQueue;
	LinkedBlockingQueue<MessageContainer> outQueue;

	public Server(int id, int port) throws IOException {
		this.id = id;
		server = new ServerSocket(port);
		inQueue = new LinkedBlockingQueue<>();
		outQueue = new LinkedBlockingQueue<>();
	}

	public void start() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Commons.log("Server started", id, true);
		String address = InetAddress.getLocalHost().getHostAddress();
		addToConnectionList(address, Constants.CONNECTIONS_PATH);
		Thread receiverThread = new Thread(new ReceiverThread());
		receiverThread.setName("ReceiverThread");
		receiverThread.start();
		Thread senderThread = new Thread(new SenderThread());
		senderThread.setName("SenderThread");
		senderThread.start();
		while(true) {
			MessageContainer container = inQueue.take();
			Message msg = container.getMessage();
			if(msg instanceof ReadMessage) {
				
			} else if(msg instanceof InsertMessage) {
				
			} else if(msg instanceof UpdateMessage) {
				
			} else if(msg instanceof DeleteMessage) {
				
			} else {
				System.err.println("[ SERVER- " + id + "]: ERROR - Unknown Message received");
			}
		}
	}

	private void addToConnectionList(String address, String filename) throws IOException {
		for (int i = 0; i < Constants.CLIENT_NUMBER; i++) {
			Properties props = new Properties();
			File f = new File(filename + "client" + i);
			if (!f.exists()) {
				f.createNewFile();
				// Files.createFile(Paths.get(filename + "client" + i));
				props.setProperty("server" + id, address);
				FileOutputStream out = new FileOutputStream(f, true);
				props.store(out, null);
				out.close();
			} else {
				FileInputStream in = new FileInputStream(f);
				props.load(in);
				in.close();
				props.setProperty("server" + id, address);
				FileOutputStream out = new FileOutputStream(f);
				props.store(out, null);
				out.close();
			}
		}
	}

	private class ReceiverThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Socket client = server.accept();
					BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String rawMessage = br.readLine();
					if(rawMessage.contains("ReadMessage")) {
						ReadMessage rm = ReadMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(rm, client));
					} else if(rawMessage.contains("InsertMessage")) {
						InsertMessage im = InsertMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(im, client));
					} else if(rawMessage.contains("UpdateMessage")) {
						UpdateMessage um = UpdateMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(um, client));
					} else if(rawMessage.contains("DeleteMessage")) {
						DeleteMessage dm = DeleteMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(dm, client));
					} else {
						System.err.println("[ SERVER- " + id + "]: ERROR - Unknown Message received");
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SenderThread implements Runnable {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				try {
					MessageContainer container = outQueue.take();
					Commons.writeToSocket(container.getClient(), container.getMessage().toString());
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
