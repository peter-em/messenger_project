package piotr.messenger.server;


public class ServerMain {

	public static void main(String[] args) {

		ServerThread server = new ServerThread();
		Thread task = new Thread(server);
		task.setName("MainThread");
		task.start();

	}

}


