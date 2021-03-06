package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import messages.DeleteMessage;
import messages.FinalMessage;
import messages.InsertMessage;
import messages.Message;
import messages.ReadMessage;
import messages.ResponseMessage;
import messages.UpdateMessage;
import objects.RepoObject;
import utils.Commons;
import utils.Constants;
import utils.MessageContainer;

/**
 * Server implementation of the FileSystem
 * @author shriroop
 *
 */
public class Server {

	ServerSocket externalServer, internalServer;
	private int id;
	File repo;
	LinkedBlockingQueue<MessageContainer> inQueue;
	LinkedBlockingQueue<MessageContainer> outQueue;

	public Server(int id, int externalPort, int internalPort) throws IOException {
		this.id = id;
		externalServer = new ServerSocket(externalPort);
		internalServer = new ServerSocket(internalPort);
		inQueue = new LinkedBlockingQueue<>();
		outQueue = new LinkedBlockingQueue<>();
		repo = new File(Constants.REPOSITORY_PATH + "server" + id);
		Commons.log("RepoPath: " + repo, id, true);
		if (!repo.exists()) {
			repo.mkdirs();
		}
	}

	/**
	 * Starts server, reads message from InputQueue, processes it, and places results in OutputQueue
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() throws IOException, InterruptedException {
		Commons.log("Server started on " + InetAddress.getLocalHost().getHostAddress() + ":" + Constants.SERVER_PORT,
				id, true);
		String address = InetAddress.getLocalHost().getHostAddress();
		addToConnectionList(address, Constants.CONNECTIONS_PATH);
		Thread receiverThread = new Thread(new ReceiverThread());
		receiverThread.setName("ReceiverThread");
		receiverThread.start();
		Thread senderThread = new Thread(new SenderThread());
		senderThread.setName("SenderThread");
		senderThread.start();
		while (true) {
			MessageContainer container = inQueue.take();
			Message msg = container.getMessage();

			if (msg instanceof ReadMessage) {
				ReadMessage rm = (ReadMessage) msg;
				Commons.log("Received READ from " + rm.sender + " to read OBJECT-" + rm.getObjectID(), id, true);
				RepoObject obj = readObject(rm);
				ResponseMessage resMsg = new ResponseMessage();
				if (obj == null) {
					Commons.log("OBJECT-" + rm.getObjectID() + " not found", id, true);
					resMsg.status = STATUS.ERROR.toString();
					resMsg.statusMessage = "Requested message not found on SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				} else {
					Commons.log("OBJECT-" + rm.getObjectID() + " retrived", id, true);
					resMsg.status = STATUS.SUCCESS.toString();
					resMsg.statusMessage = "OBJECT-" + rm.getObjectID() + " retrived at SERVER-" + id;
					resMsg.object = obj.toString();
					resMsg.objectID = -1;
				}
				resMsg.sender = "SERVER-" + id;
				resMsg.senderAddress = InetAddress.getLocalHost().getHostAddress();
				resMsg.receiver = rm.sender;
				resMsg.receiverAddress = rm.senderAddress;
				MessageContainer resContainer = new MessageContainer(resMsg, container.getClient());
				outQueue.put(resContainer);

			} else if (msg instanceof InsertMessage) {
				InsertMessage im = (InsertMessage) msg;
				Commons.log("Received INSERT from " + im.sender + " to insert OBJECT-" + im.getObjectID(), id, true);
				boolean success = insertObject(im);
				ResponseMessage resMsg = new ResponseMessage();
				if (success) {
					Commons.log("OBJECT-" + im.getObjectID() + " inserted at SERVER-" + id, id, true);
					resMsg.status = STATUS.SUCCESS.toString();
					resMsg.statusMessage = "OBJECT " + im.getObjectID() + " inserted at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				} else {
					Commons.log("OBJECT-" + im.getObjectID() + " already present at SERVER-" + id, id, true);
					resMsg.status = STATUS.ERROR.toString();
					resMsg.statusMessage = "OBJECT-" + im.getObjectID() + " already present at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				}
				resMsg.sender = "SERVER-" + id;
				resMsg.senderAddress = InetAddress.getLocalHost().getHostAddress();
				resMsg.receiver = im.sender;
				resMsg.receiverAddress = im.senderAddress;
				MessageContainer resContainer = new MessageContainer(resMsg, container.getClient());
				outQueue.put(resContainer);

			} else if (msg instanceof UpdateMessage) {
				UpdateMessage um = (UpdateMessage) msg;
				boolean success = updateObject(um);
				ResponseMessage resMsg = new ResponseMessage();
				if (success) {
					Commons.log("OBJECT-" + um.getObjectID() + " updated at SERVER-" + id, id, true);
					resMsg.status = STATUS.SUCCESS.toString();
					resMsg.statusMessage = "OBJECT " + um.getObjectID() + " updated at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				} else {
					Commons.log("OBJECT-" + um.getObjectID() + " not found at SERVER-" + id, id, true);
					resMsg.status = STATUS.ERROR.toString();
					resMsg.statusMessage = "OBJECT-" + um.getObjectID() + " not found at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				}
				resMsg.sender = "SERVER-" + id;
				resMsg.senderAddress = InetAddress.getLocalHost().getHostAddress();
				resMsg.receiver = um.sender;
				resMsg.receiverAddress = um.senderAddress;
				MessageContainer resContainer = new MessageContainer(resMsg, container.getClient());
				outQueue.put(resContainer);

			} else if (msg instanceof DeleteMessage) {
				DeleteMessage dm = (DeleteMessage) msg;
				boolean success = deleteObject(dm);
				ResponseMessage resMsg = new ResponseMessage();
				if (success) {
					Commons.log("OBJECT-" + dm.getObjectID() + " deleted at SERVER-" + id, id, true);
					resMsg.status = STATUS.SUCCESS.toString();
					resMsg.statusMessage = "OBJECT " + dm.getObjectID() + " deleted at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				} else {
					Commons.log("OBJECT-" + dm.getObjectID() + " not found at SERVER-" + id, id, true);
					resMsg.status = STATUS.ERROR.toString();
					resMsg.statusMessage = "OBJECT-" + dm.getObjectID() + " not found at SERVER-" + id;
					resMsg.object = null;
					resMsg.objectID = -1;
				}
				resMsg.sender = "SERVER-" + id;
				resMsg.senderAddress = InetAddress.getLocalHost().getHostAddress();
				resMsg.receiver = dm.sender;
				resMsg.receiverAddress = dm.senderAddress;
				MessageContainer resContainer = new MessageContainer(resMsg, container.getClient());
				outQueue.put(resContainer);

			} else if (msg instanceof FinalMessage) {
				FinalMessage fm = (FinalMessage) msg;
				boolean success = finalAction(fm);
				ResponseMessage resMsg = new ResponseMessage();
				if (success) {
					Commons.log("COMMITTED at SERVER-" + id, id, true);
					resMsg.status = STATUS.SUCCESS.toString();
					resMsg.statusMessage = "COMMITTED";
					resMsg.object = null;
					resMsg.objectID = -1;
				} else {
					Commons.log("ABORTED at SERVER-" + id, id, true);
					resMsg.status = STATUS.ERROR.toString();
					resMsg.statusMessage = "ABORTED";
					resMsg.object = null;
					resMsg.objectID = -1;
				}
				resMsg.sender = "SERVER-" + id;
				resMsg.senderAddress = InetAddress.getLocalHost().getHostAddress();
				resMsg.receiver = fm.sender;
				resMsg.receiverAddress = fm.senderAddress;
				MessageContainer resContainer = new MessageContainer(resMsg, container.getClient());
				outQueue.put(resContainer);

			} else {
				System.err.println("[ SERVER-" + id + "]: ERROR - Unknown Message received");
			}
		}
	}

	/**
	 * Decides whether to commit or abort
	 * @param fm
	 * @return true if commit, otherwise false
	 */
	private boolean finalAction(FinalMessage fm) {
		boolean ret = false;
		switch (fm.PREV_OP) {
		case "INSERT":
			File f = new File(repo.getPath() + File.separator + "tmp." + fm.objectID);
			if (fm.commit) {
				ret = f.renameTo(new File(repo.getPath() + File.separator + fm.objectID));
			} else {
				ret = f.delete();
			}
			return ret;

		case "UPDATE":
			if (fm.commit) {
				f = new File(repo.getPath() + File.separator + "tmp." + fm.objectID);
				f.renameTo(new File(repo.getPath() + File.separator + fm.objectID));
				f = new File(repo.getPath() + File.separator + "prev." + fm.objectID);
				f.delete();
				return true;
			} else {
				f = new File(repo.getPath() + File.separator + "prev." + fm.objectID);
				f.renameTo(new File(repo.getPath() + File.separator + fm.objectID));
				f = new File(repo.getPath() + File.separator + "tmp." + fm.objectID);
				f.delete();
				return true;
			}

		case "DELETE":
			if (fm.commit) {
				f = new File(repo.getPath() + File.separator + "tmp." + fm.objectID);
				f.delete();
				return true;
			} else {
				f = new File(repo.getPath() + File.separator + "tmp." + fm.objectID);
				f.renameTo(new File(repo.getPath() + File.separator + fm.objectID));
				return true;
			}
			
		default:
			return ret;
		}
	}

	/**
	 * Deletes object from filesystem
	 * @param msg
	 * @return true if success, otherwise false
	 */
	private boolean deleteObject(DeleteMessage msg) {
		boolean deleted = true;
		File[] files = repo.listFiles();
		for (File file : files) {
			if (file.getName().equals(msg.getObjectID() + "")) {
				return file.renameTo(new File(repo.getPath() + File.separator + "tmp." + msg.getObjectID()));
			}
		}
		return deleted;
	}

	/**
	 * Updates the Object in filesystem
	 * @param msg
	 * @return true if success, otherwise false
	 * @throws IOException
	 */
	private boolean updateObject(UpdateMessage msg) throws IOException {
		boolean updated = false;
		RepoObject obj = null;
		File[] files = repo.listFiles();
		for (File file : files) {
			if (file.getName().equals(msg.getObjectID() + "")) {
				String contents = "";
				List<String> lines = Files.readAllLines(file.toPath());
				for (String line : lines) {
					contents += line;
				}
				obj = RepoObject.getObjectFromString(contents);
				break;
			}
		}
		if (obj == null)
			return updated;
		OutputStreamWriter osw = new OutputStreamWriter(
				new FileOutputStream(repo.getPath() + File.separator + "tmp." + msg.getObjectID()));
		osw.write(msg.newObject + "\n");
		osw.close();
		File f = new File(repo.getPath() + File.separator + msg.getObjectID());
		f.renameTo(new File(repo.getPath() + File.separator + "prev." + msg.getObjectID()));
		f.delete();
		updated = true;
		return updated;
	}

	/**
	 * Writes an object to the filesystem
	 * @param msg
	 * @return true if written, false otherwise
	 * @throws IOException
	 */
	private boolean insertObject(InsertMessage msg) throws IOException {
		boolean inserted = false;
		File[] files = repo.listFiles();
		if (files != null && files.length != 0) {
			for (File file : files) {
				if (file.getName().equals(msg.getObjectID() + ""))
					return inserted;
			}
		}
		File f = new File(repo.getPath() + File.separator + "tmp." + msg.getObjectID());
		f.createNewFile();
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f));
		RepoObject o = RepoObject.getObjectFromString(msg.object);
		o.version = 0;
		msg.object = o.toString();
		osw.write(msg.object + "\n");
		osw.close();
		inserted = true;
		return inserted;
	}

	/**
	 * Reads object from the filesystem
	 * @param msg
	 * @return object
	 * @throws IOException
	 */
	private RepoObject readObject(ReadMessage msg) throws IOException {
		RepoObject obj = null;
		int id = msg.getObjectID();
		File[] files = repo.listFiles();
		for (File file : files) {
			if (file.getName().equals(id + "")) {
				String contents = "";
				List<String> lines = Files.readAllLines(file.toPath());
				for (String line : lines) {
					contents += line;
				}
				obj = RepoObject.getObjectFromString(contents);
				break;
			}
		}
		return obj;
	}

	/**
	 * adds the IP address to list of connections for each client
	 * @param address
	 * @param filename
	 * @throws IOException
	 */
	private void addToConnectionList(String address, String filename) throws IOException {
		for (int i = 0; i < Constants.CLIENT_NUMBER; i++) {
			Properties props = new Properties();
			File clientFile = new File(filename + "client" + i);
			File serverFile = new File(filename + "servers");
			if (!clientFile.exists()) {
				clientFile.createNewFile();
			} else {
				FileInputStream in = new FileInputStream(clientFile);
				props.load(in);
				in.close();
			}
			props.setProperty("server" + id, address);
			FileOutputStream out = new FileOutputStream(clientFile);
			props.store(out, null);
			out.close();
			props = new Properties();
			if (!serverFile.exists()) {
				serverFile.createNewFile();
			} else {
				FileInputStream in = new FileInputStream(serverFile);
				props.load(in);
				in.close();
			}
			props.setProperty("server" + id, address);
			out = new FileOutputStream(serverFile);
			props.store(out, null);
			out.close();
		}
	}

	/**
	 * Receives the incoming messages and places it in InputQueue
	 * @author shriroop
	 *
	 */
	private class ReceiverThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Commons.log("Waiting for connections", id, true);
					Socket client = externalServer.accept();
					BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String rawMessage = br.readLine();
					Commons.log("MESSAGE: " + rawMessage, id, true);
					if (rawMessage.contains("ReadMessage")) {
						ReadMessage rm = ReadMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(rm, client));
					} else if (rawMessage.contains("InsertMessage")) {
						InsertMessage im = InsertMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(im, client));
					} else if (rawMessage.contains("UpdateMessage")) {
						UpdateMessage um = UpdateMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(um, client));
					} else if (rawMessage.contains("DeleteMessage")) {
						DeleteMessage dm = DeleteMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(dm, client));
					} else if (rawMessage.contains("FinalMessage")) {
						FinalMessage fm = FinalMessage.getObjectFromString(rawMessage);
						inQueue.put(new MessageContainer(fm, client));
					} else {
						System.err.println("[ SERVER- " + id + "]: ERROR - Unknown Message received");
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads the messages from OutputQueue and sends them
	 * @author shriroop
	 *
	 */
	private class SenderThread implements Runnable {

		@Override
		public void run() {
			while (true) {
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