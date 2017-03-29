package app;

import java.io.IOException;
import java.util.Properties;

import client.Client;
import server.Server;
import utils.Commons;
import utils.Constants;

public class Main {

	public static void main(String[] args) throws IOException, NumberFormatException, InterruptedException {
		String filename = "resources/config.properties";
		if(args.length != 2) {
			usage();
		}
		Properties configs = Commons.loadProperties(filename);
		Constants.CONNECTIONS_PATH = configs.getProperty("connections.path");
		Constants.REPOSITORY_PATH = configs.getProperty("repository.path");
		// Commons.clearConnections();
		Constants.CLIENT_NUMBER = Integer.parseInt(configs.getProperty("client.number", "5"));
		Constants.SERVER_NUMBER = Integer.parseInt(configs.getProperty("server.number", "7"));
		int id = Integer.parseInt(args[1]);
		int acceptedServerId = Integer.parseInt(configs.getProperty("server.number", "7")) - 1;
		int acceptedClientId = Integer.parseInt(configs.getProperty("client.number", "5")) - 1;
		if(args[0].equalsIgnoreCase("server")) {
			if(id <= acceptedServerId) {
				Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port", "8090"));
				Server server = new Server(id, Constants.SERVER_PORT);
				server.start();
				System.out.println("Server");
			} else {
				System.err.println("<id> should be in the range [0-" + acceptedServerId + "]");
				System.exit(1);
			}
		} else if(args[0].equalsIgnoreCase("client")) {
			if(id <= acceptedClientId) {
				Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port", "8090"));
				Commons.log("PORT: " + Constants.SERVER_PORT, id, false);
				Client client = new Client(id);
				client.start();
			} else {
				System.err.println("<id> should be in the range [0-" + acceptedClientId + "]");
				System.exit(1);
			}
		} else {
			usage();
		}
		
	}
	
	public static void usage() {
		System.err.println("USAGE: ./run.sh <client|server> <id>");
		System.exit(1);
	}

}
