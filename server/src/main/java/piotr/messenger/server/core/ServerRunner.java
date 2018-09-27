package piotr.messenger.server.core;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piotr.messenger.server.config.AutoConfig;

public class ServerRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);

        ServerWorker server = context.getBean(ServerWorker.class);
        Thread task = new Thread(server);
        task.setName("MainThread");
        task.start();
    }

}
