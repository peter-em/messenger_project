package piotr.messenger.server;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piotr.messenger.server.config.AutoConfig;
import piotr.messenger.server.core.ServerWorker;

@SpringBootApplication
public class ServerMain {

	public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);

//		ServerWorker server = new ServerWorker();
        ServerWorker server = context.getBean(ServerWorker.class);
		Thread task = new Thread(server);
		task.setName("MainThread");
		task.start();

	}

}


