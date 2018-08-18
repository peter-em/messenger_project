package piotr.messenger.server;


import piotr.messenger.server.core.ServerWorker;

public class ServerMain {

	public static void main(String[] args) {

		ServerWorker server = new ServerWorker();
		Thread task = new Thread(server);
		task.setName("MainThread");
		task.start();

	}

}


