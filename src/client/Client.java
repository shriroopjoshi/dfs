package client;

import java.util.Scanner;

import utils.Commons;

public class Client {

	private int id;

	public Client(int id) {
		this.id = id;
	}

	public void start() {
		Commons.log("Client started", id, false);
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.print("client> ");
			switch (sc.nextLine().charAt(0)) {
			case 'q':
				sc.close();
				System.exit(0);
				break;

			case 'i':
				System.out.println("'i' pressed");
				break;
				
			case 'u':
				System.out.println("'u' pressed");
				break;
				
			case 'r':
				System.out.println("'r' pressed");
				break;
				
			case 'd':
				System.out.println("'d' pressed");
				break;
			}
		}
	}
}
