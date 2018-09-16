package piotr.messenger.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import piotr.messenger.server.core.ServerRunner;

@SpringBootApplication
public class ServerMain {

	public static void main(String[] args) {

        SpringApplication.run(ServerRunner.class, args);

	}

}


