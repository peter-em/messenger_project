package piotr.messenger.server;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import piotr.messenger.server.core.ServerWorker;

@SpringBootApplication
public class ServerMain {

	public static void main(String[] args) {

		ServerWorker server = new ServerWorker();
		Thread task = new Thread(server);
		task.setName("MainThread");
		task.start();

	}

}


