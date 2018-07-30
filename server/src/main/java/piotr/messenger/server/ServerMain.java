package piotr.messenger.server;


public class ServerMain {

	public static void main(String[] args) {

		final int portNr = 6125;
		final String hostName = "127.0.0.1";

		ServerThread server = new ServerThread(hostName, portNr);
		Thread task = new Thread(server);
		task.setName("MainThread");
		task.start();

	}

}


