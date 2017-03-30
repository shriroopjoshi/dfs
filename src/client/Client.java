package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import messages.DeleteMessage;
import messages.InsertMessage;
import messages.ReadMessage;
import messages.ResponseMessage;
import messages.UpdateMessage;
import objects.RepoObject;
import utils.Commons;
import utils.Constants;

public class Client {

	private int id;

	public Client(int id) {
		this.id = id;
	}

	public void start() throws IOException {
		Commons.log("Client started", id, false);
		while (true) {
			String op = "";
			while (op.length() <= 0) {
				console();
				op = readLineConsole();
			}
			switch (op.charAt(0)) {
			case 'q':
				Commons.log("Exiting", id, false);
				System.exit(0);
				break;

			case 'i':
				// TODO Implement 2-phase commit
				// TODO Receiver field empty in message
				console("objectID: ");
				int objectID = readConsoleInt();
				int h = hash(objectID);
				ArrayList<String> conn = checkConn(hash(h), hash(h + 1), hash(h + 2));
				if (conn.size() >= 2) {
					Socket sockets[] = new Socket[conn.size()];
					InsertMessage im = new InsertMessage();
					im.sender = "CLIENT-" + id;
					im.senderAddress = InetAddress.getLocalHost().getHostAddress();
					for (int i = 0; i < sockets.length; i++)
						sockets[i] = new Socket(conn.get(i), Constants.SERVER_PORT);
					im.objectID = objectID;
					console("Contents:\n");
					RepoObject obj = new RepoObject();
					obj.contents = readConsole();
					im.object = obj.toString();
					for (int i = 0; i < sockets.length; i++) {
						im.receiverAddress = conn.get(i);
						Commons.writeToSocket(sockets[i], im.toString());
					}
					for (int i = 0; i < sockets.length; i++) {
						String rawMessage = Commons.readFromSocket(sockets[i]);
						ResponseMessage rm = ResponseMessage.getObjectFromString(rawMessage);
						Commons.log(rm.statusMessage, id, false);
					}
					for (int i = 0; i < sockets.length; i++)
						sockets[i].close();

				} else {
					Commons.log("Unable to reach at least two servers", id, false);
				}
				break;

			case 'u':
				// TODO Implement 2-phase commit
				// TODO Receiver field empty in message
				console("objectID: ");
				objectID = readConsoleInt();
				h = hash(objectID);
				conn = checkConn(hash(h), hash(h + 1), hash(h + 2));
				if (conn.size() >= 2) {
					Socket sockets[] = new Socket[conn.size()];
					UpdateMessage um = new UpdateMessage();
					um.sender = "CLIENT-" + id;
					um.senderAddress = InetAddress.getLocalHost().getHostAddress();
					for (int i = 0; i < sockets.length; i++)
						sockets[i] = new Socket(conn.get(i), Constants.SERVER_PORT);
					um.objectID = objectID;
					console("Contents:\n");
					RepoObject obj = new RepoObject();
					obj.contents = readConsole();
					um.newObject = obj.toString();
					for (int i = 0; i < sockets.length; i++) {
						um.receiverAddress = conn.get(i);
						Commons.writeToSocket(sockets[i], um.toString());
					}
					for (int i = 0; i < sockets.length; i++) {
						String rawMessage = Commons.readFromSocket(sockets[i]);
						ResponseMessage rm = ResponseMessage.getObjectFromString(rawMessage);
						Commons.log(rm.statusMessage, id, false);
					}
					for (int i = 0; i < sockets.length; i++)
						sockets[i].close();

				} else {
					Commons.log("Unable to reach at least two servers", id, false);
				}
				break;

			case 'r':
				console("objectID: ");
				objectID = readConsoleInt();
				h = hash(objectID);
				boolean conn1 = checkConn(hash(h));
				boolean conn2 = checkConn(hash(h + 1));
				boolean conn3 = checkConn(hash(h + 2));
				console("Servers: " + hash(h) + (conn1 ? "*" : "") + ", " + hash(h + 1) + (conn2 ? "*" : "") + ", "
						+ hash(h + 2) + (conn3 ? "*" : "") + ": ");
				int serverID = readConsoleInt();
				String serverAddress = connect(serverID);
				if (serverAddress == null) {
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
				if (obj == null) {
					Commons.log(responseMessage.statusMessage, id, false);
				} else {
					Commons.log("Object:\n" + obj.getContents(), id, false);
				}
				server.close();
				break;

			case 'd':
				console("objectID: ");
				objectID = readConsoleInt();
				console("objectID (confirmation): ");
				int tempID = readConsoleInt();
				if (objectID != tempID)
					break;
				h = hash(objectID);
				if (checkConn(hash(h)) & checkConn(hash(h + 1)) & checkConn(hash(h + 2))) {
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

	private ArrayList<String> checkConn(int hash, int hash2, int hash3) {
		// TODO Auto-generated method stub
		ArrayList<String> conns = new ArrayList<>();
		String conn;
		Properties servers = Commons.loadProperties(Constants.CONNECTIONS_PATH + "client" + id);
		if ((conn = servers.getProperty("server" + hash)) != null)
			conns.add(conn);
		if ((conn = servers.getProperty("server" + hash2)) != null)
			conns.add(conn);
		if ((conn = servers.getProperty("server" + hash3)) != null)
			conns.add(conn);
		return conns;
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

	private String readConsole() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String contents = "";
		String temp;
		while ((temp = br.readLine()) != null)
			contents += temp + "\n";
		return contents;
	}

	private int readConsoleInt() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int i = Integer.parseInt(br.readLine());
		return i;
	}

	private String readLineConsole() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String string = br.readLine();
		return string;
	}
}
