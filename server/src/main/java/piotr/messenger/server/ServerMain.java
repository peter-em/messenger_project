package piotr.messenger.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import piotr.messenger.server.core.ServerWorker;

import javax.annotation.Resource;

@SpringBootApplication
@EnableJpaAuditing
public class ServerMain implements CommandLineRunner {

    @Resource
    private ServerWorker server;

	public static void main(String[] args) {

        SpringApplication.run(ServerMain.class, args);

	}

    @Override
    public void run(String... args) {

        Thread task = new Thread(server);
        task.setName("MainThread");
        task.start();
    }
}


