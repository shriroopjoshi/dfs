package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;

public class Commons {

	public static Properties loadProperties(String filename) {
        InputStream configs;
        Properties prop = new Properties();
        try {
            configs = new FileInputStream(filename);
            prop.load(configs);
        } catch (FileNotFoundException ex) {
            System.err.println("CONFIG file not found\n" + ex);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Unable to read CONFIG file");
            System.exit(1);
        }
        return prop;
    }
	
	public static void log(String message, int id, boolean server) {
		if(server) {
			System.out.println("[ SERVER-" + id + " ]: " + message);
		} else {
			System.out.println("[ CLIENT-" + id + " ]: " + message);
		}
	}
	
	public static void clearConnections() {
		File connections = new File(Constants.CONNECTIONS_PATH);
		if(connections.exists()) {
			File[] files = connections.listFiles();
			for (File file : files) {
				file.delete();
			}
		} else {
			connections.mkdirs();
		}
	}
	
    public static void writeToSocket(Socket s, String message) throws IOException {
    	OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream(), "UTF-8");
        osw.append(message).append("\n");
        osw.flush();
    }
    
    public static String readFromSocket(Socket s) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    	return br.readLine();
    }
}
