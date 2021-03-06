package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import messages.DeleteMessage;
import messages.FinalMessage;
import messages.InsertMessage;
import messages.ReadMessage;
import messages.ResponseMessage;
import messages.UpdateMessage;
import objects.RepoObject;
import server.STATUS;
import utils.Commons;
import utils.Constants;

/**
 * Creates a client interface for File System
 * 
 * @author shriroop
 *
 */
public class Client {

	// CLIENT-ID
	private int id;

	public Client(int id) {
		this.id = id;
	}

	/**
	 * Starts client interface on an interactive console
	 * 
	 * @throws IOException
	 */
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

			/*
			 * INSERT object to the FileSystem
			 */
			case 'i':
				console("objectID: ");
				int objectID = readConsoleInt();
				int h = hash(objectID);
				ArrayList<String> conn = checkConn(hash(h), hash(h + 1), hash(h + 2));
				if (conn.size() >= 2) {
					// PHASE-I
					boolean commit = true;
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
						if (rm.status.equals(STATUS.ERROR)) {
							commit = false;
							Commons.log(rm.statusMessage, id, false);
						}
					}
					for (int i = 0; i < sockets.length; i++)
						sockets[i].close();
					// PHASE-II
					FinalMessage fm = new FinalMessage();
					fm.PREV_OP = "INSERT";
					fm.objectID = objectID;
					fm.commit = commit;
					for (int i = 0; i < sockets.length; i++)
						sockets[i] = new Socket(conn.get(i), Constants.SERVER_PORT);
					for (int i = 0; i < sockets.length; i++) {
						fm.receiverAddress = conn.get(i);
						Commons.writeToSocket(sockets[i], fm.toString());
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

			/*
			 * Make UPDATEs to the existing object in the FileSystem
			 */
			case 'u':
				console("objectID: ");
				objectID = readConsoleInt();
				h = hash(objectID);
				conn = checkConn(hash(h), hash(h + 1), hash(h + 2));
				if (conn.size() >= 2) {
					// PHASE-I
					boolean commit = true;
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
						if (rm.status.equals(STATUS.ERROR)) {
							commit = false;
							Commons.log(rm.statusMessage, id, false);
						}
					}
					for (int i = 0; i < sockets.length; i++)
						sockets[i].close();
					// PHASE-II
					FinalMessage fm = new FinalMessage();
					fm.PREV_OP = "UPDATE";
					fm.objectID = objectID;
					fm.commit = commit;
					for (int i = 0; i < sockets.length; i++)
						sockets[i] = new Socket(conn.get(i), Constants.SERVER_PORT);
					for (int i = 0; i < sockets.length; i++) {
						fm.receiverAddress = conn.get(i);
						Commons.writeToSocket(sockets[i], fm.toString());
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

			/*
			 * READ an object from the FileSystem
			 */
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
					boolean commit = true;
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
					if (responseMessage.status.equals(STATUS.ERROR))
						commit = false;
					Commons.log(responseMessage.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server2);
					responseMessage = ResponseMessage.getObjectFromString(rawMessage);
					if (responseMessage.status.equals(STATUS.ERROR))
						commit = false;
					Commons.log(responseMessage.statusMessage, id, false);
					rawMessage = Commons.readFromSocket(server3);
					responseMessage = ResponseMessage.getObjectFromString(rawMessage);
					if (responseMessage.status.equals(STATUS.ERROR))
						commit = false;
					Commons.log(responseMessage.statusMessage, id, false);
					server1.close();
					server2.close();
					server3.close();
					// PHASE-II
					FinalMessage fm = new FinalMessage();
					fm.PREV_OP = "DELETE";
					fm.objectID = objectID;
					fm.commit = commit;
					server1 = new Socket(connect(hash(h)), Constants.SERVER_PORT);
					server2 = new Socket(connect(hash(h + 1)), Constants.SERVER_PORT);
					server3 = new Socket(connect(hash(h + 2)), Constants.SERVER_PORT);
					Commons.writeToSocket(server1, fm.toString());
					Commons.writeToSocket(server2, fm.toString());
					Commons.writeToSocket(server3, fm.toString());
					rawMessage = Commons.readFromSocket(server1);
					rawMessage = Commons.readFromSocket(server2);
					rawMessage = Commons.readFromSocket(server3);
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

	/**
	 * Checks if the client is able to connect to any server from given ids
	 * @param SERVER-ID1
	 * @param SERVER-ID2
	 * @param SERVER-ID3
	 * @return ArrayList of IP address of accessible servers
	 */
	private ArrayList<String> checkConn(int hash, int hash2, int hash3) {
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

	/**
	 * Checks if client can connect to server with ID = serverID
	 * @param serverID
	 * @return IP address of server, null if not able to connect
	 * @throws IOException
	 */
	private String connect(int serverID) throws IOException {
		Properties servers = Commons.loadProperties(Constants.CONNECTIONS_PATH + "client" + id);
		return servers.getProperty("server" + serverID);
	}

	/**
	 * Checks if client can connect to server with ID = serverID
	 * @param serverID
	 * @return true if able to connect, otherwise false
	 * @throws IOException
	 */
	private boolean checkConn(int serverID) throws IOException {
		Properties servers = Commons.loadProperties(Constants.CONNECTIONS_PATH + "client" + id);
		return servers.getProperty("server" + serverID) == null ? false : true;
	}

	/**
	 * Computes the SERVER-ID from OBJECT-ID.
	 * Uses mod to calculate hash
	 * @param id - OBJECT-ID
	 * @return
	 */
	private int hash(int id) {
		return id % Constants.SERVER_NUMBER;
	}

	/**
	 * Prints message as console prompt
	 * @param message
	 */
	private void console(String message) {
		System.out.print("client> " + message);
	}

	/**
	 * Prints console prompt
	 */
	private void console() {
		System.out.print("client> ");
	}

	/**
	 * Reads from console till EOF
	 * @return read text in String
	 * @throws IOException
	 */
	private String readConsole() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String contents = "";
		String temp;
		while ((temp = br.readLine()) != null)
			contents += temp + "\n";
		return contents;
	}

	/**
	 * Reads integer from console
	 * @throws IOException
	 */
	private int readConsoleInt() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int i = Integer.parseInt(br.readLine());
		return i;
	}

	/**
	 * Reads a single line from console
	 * @throws IOException
	 */
	private String readLineConsole() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String string = br.readLine();
		return string;
	}
}
