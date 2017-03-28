package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

import messages.DeleteMessage;
import messages.InsertMessage;
import messages.ReadMessage;
import messages.ResponseMessage;
import messages.UpdateMessage;
import objects.RepoObject;
import server.STATUS;
import utils.Commons;
import utils.Constants;

public class Client {

	private int id;

	public Client(int id) {
		this.id = id;
	}

	public void start() throws IOException {
		Commons.log("Client started", id, false);
		Scanner sc = new Scanner(System.in);
		while (true) {
			console();
			switch (sc.nextLine().charAt(0)) {
			case 'q':
				sc.close();
				Commons.log("Exiting", id, false);
				System.exit(0);
				break;

			case 'i':
				// TODO
				console("objectID: ");
				int objectID = sc.nextInt();
				int h = hash(objectID);
				boolean conn1 = checkConn(hash(h));
				boolean conn2 = checkConn(hash(h + 1));
				boolean conn3 = checkConn(hash(h + 2));
				if(conn1 & conn2 & conn3) {
					InsertMessage im = new InsertMessage();
					im.sender = "CLIENT-" + id;
					im.senderAddress = InetAddress.getLocalHost().getHostAddress();
					Socket server1 = new Socket(connect(hash(h)), Constants.SERVER_PORT);
					Socket server2 = new Socket(connect(hash(h + 1)), Constants.SERVER_PORT);
					Socket server3 = new Socket(connect(hash(h + 2)), Constants.SERVER_PORT);
					im.objectID = objectID;
					console("Contents: ");
					RepoObject obj = new RepoObject();
					obj.contents = sc.nextLine();
					im.object = obj.toString();
					Commons.writeToSocket(server1, im.toString());
					Commons.writeToSocket(server2, im.toString());
					Commons.writeToSocket(server3, im.toString());
					String rawMessage = Commons.readFromSocket(server1);
					ResponseMessage rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server2);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server3);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					server1.close();
					server2.close();
					server3.close();
					
				}
				break;
				
			case 'u':
				// TODO
				console("objectID: ");
				objectID = sc.nextInt();
				h = hash(objectID);
				conn1 = checkConn(hash(h));
				conn2 = checkConn(hash(h + 1));
				conn3 = checkConn(hash(h + 2));
				if(conn1 & conn2 & conn3) {
					ReadMessage rM = new ReadMessage();
					rM.sender = "CLIENT-" + id;
					rM.senderAddress = InetAddress.getLocalHost().getHostAddress();
					Socket server1 = new Socket(connect(hash(h)), Constants.SERVER_PORT);
					Socket server2 = new Socket(connect(hash(h + 1)), Constants.SERVER_PORT);
					Socket server3 = new Socket(connect(hash(h + 2)), Constants.SERVER_PORT);
					rM.receiver = "SERVER-" + hash(h);
					rM.receiverAddress = connect(hash(h));
					Commons.writeToSocket(server1, rM.toString());
					rM.receiver = "SERVER-" + hash(h + 1);
					rM.receiverAddress = connect(hash(h + 1));
					Commons.writeToSocket(server2, rM.toString());
					rM.receiver = "SERVER-" + hash(h + 2);
					rM.receiverAddress = connect(hash(h + 2));
					Commons.writeToSocket(server3, rM.toString());
					String rawMessage = Commons.readFromSocket(server1);
					ResponseMessage rm = ResponseMessage.getObjectFromString(rawMessage);
					if(rm.status.equals(STATUS.ERROR)) {
						Commons.log(rm.statusMessage, id, false);
						break;
					}
					rawMessage = Commons.readFromSocket(server2);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					if(rm.status.equals(STATUS.ERROR)) {
						Commons.log(rm.statusMessage, id, false);
						break;
					}
					rawMessage = Commons.readFromSocket(server3);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					if(rm.status.equals(STATUS.ERROR)) {
						Commons.log(rm.statusMessage, id, false);
						break;
					}
					UpdateMessage um = new UpdateMessage();
					um.setObjectID(objectID);
					console("Contents: ");
					RepoObject obj = new RepoObject();
					obj.contents = sc.nextLine();
					um.newObject = obj.toString();
					um.sender = "CLIENT-" + id;
					um.senderAddress = InetAddress.getLocalHost().getHostAddress();
					um.receiver = "SERVER-" + hash(h);
					um.receiverAddress = connect(hash(h));
					Commons.writeToSocket(server1, um.toString());
					um.receiver = "SERVER-" + hash(h + 1);
					um.receiverAddress = connect(hash(h + 1));
					Commons.writeToSocket(server2, um.toString());
					um.receiver = "SERVER-" + hash(h + 2);
					um.receiverAddress = connect(hash(h + 2));
					Commons.writeToSocket(server3, um.toString());
					rawMessage = Commons.readFromSocket(server1);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server2);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server3);
					rm = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(rm.statusMessage, id, false);
					server1.close();
					server2.close();
					server3.close();
				}
				break;
				
			case 'r':
				console("objectID: ");
				objectID = sc.nextInt();
				h = hash(objectID);
				conn1 = checkConn(hash(h));
				conn2 = checkConn(hash(h + 1));
				conn3 = checkConn(hash(h + 2));
				console("Servers: " + hash(h) + (conn1 ? "*" : "") + ", "
						+ hash(h + 1) + (conn2 ? "*" : "") + ", "
						+ hash(h + 2) + (conn3 ? "*" : ""));
				int serverID = sc.nextInt();
				String serverAddress = connect(serverID);
				if(serverAddress == null) {
					Commons.log("Unable to reach SERVER-" + serverID, id, false);
					break;
				}
				ReadMessage rm = new ReadMessage();
				rm.sender = "CLIENT-" + id;
				rm.senderAddress = InetAddress.getLocalHost().getHostAddress();
				rm.receiver = "SERVER-" + serverID;
				rm.receiverAddress = serverAddress;
				rm.setObjectID(objectID);
				Socket server = new Socket(serverAddress, Constants.SERVER_PORT);
				Commons.writeToSocket(server, rm.toString());
				String rawMessage = Commons.readFromSocket(server);
				ResponseMessage responseMessage = ResponseMessage.getObjectFromString(rawMessage);
				RepoObject obj = RepoObject.getObjectFromString(responseMessage.object);
				Commons.log("Object:\n" + obj.getContents(), id, false);
				server.close();
				break;
				
			case 'd':
				console("objectID: ");
				objectID = sc.nextInt();
				console("objectID (confirmation): ");
				int tempID = sc.nextInt();
				if(objectID != tempID)
					break;
				h = hash(objectID);
				if(checkConn(hash(h)) & checkConn(hash(h + 1)) & checkConn(hash(h + 2))) {
					DeleteMessage dm = new DeleteMessage();
					dm.sender = "CLIENT-" + id;
					dm.senderAddress = InetAddress.getLocalHost().getHostAddress();
					dm.setObjectID(objectID);
					dm.receiver = "SERVER-" + hash(h);
					dm.receiverAddress = connect(hash(h));
					Socket server1 = new Socket(connect(hash(h)), Constants.SERVER_PORT);
					Socket server2 = new Socket(connect(hash(h + 1)), Constants.SERVER_PORT);
					Socket server3 = new Socket(connect(hash(h + 2)), Constants.SERVER_PORT);
					Commons.writeToSocket(server1, dm.toString());
					Commons.writeToSocket(server2, dm.toString());
					Commons.writeToSocket(server3, dm.toString());
					rawMessage = Commons.readFromSocket(server1);
					responseMessage = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(responseMessage.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server2);
					responseMessage = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(responseMessage.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server3);
					responseMessage = ResponseMessage.getObjectFromString(rawMessage);
					Commons.log(responseMessage.statusMessage, id, false);
					server1.close();
					server2.close();
					server3.close();
				} else {
					Commons.log("Unable to DELETE: All server not accessible", id, false);
				}
				break;
			}
		}
	}
	
	private String connect(int serverID) throws IOException {
		Properties servers = Commons.loadProperties(Constants.CONNECTIONS_PATH + "client" + id);
		Commons.log("Server address: " + servers.getProperty("server" + serverID), id, false);
		return servers.getProperty("server" + serverID);
	}

	private boolean checkConn(int serverID) throws IOException {
		Properties servers = Commons.loadProperties(Constants.CONNECTIONS_PATH + "client" + id);
		return servers.getProperty("server" + serverID) == null ? false : true;
	}

	private int hash(int id) {
		return id % Constants.SERVER_NUMBER;
	}
	
	private void console(String message) {
		System.out.print("client> " + message);
	}
	
	private void console() {
		System.out.print("client> ");
	}
}
