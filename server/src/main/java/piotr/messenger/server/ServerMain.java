package piotr.messenger.server;

import java.io.*;


public class ServerMain {

	public static void main(String[] args) {

		final int portNr = 6125;
		final String hostName = "127.0.0.1";

		ServerThread server = new ServerThread(hostName, portNr);
		Thread task = new Thread(server);
		task.start();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		try {
			String input;
			while ((input = stdIn.readLine()) != null) {
				System.err.println(input);
				if (input.equalsIgnoreCase("s"))
					break;
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}

		//server.stopServer();
		task.interrupt();

	}

}


