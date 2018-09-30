package piotr.messenger.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piotr.messenger.client.config.AutoConfig;
import piotr.messenger.client.core.WorkerThread;
import piotr.messenger.client.gui.MainWindow;

import javax.swing.SwingUtilities;

@SpringBootApplication
public class ClientMain {

	public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);
        WorkerThread worker = context.getBean(WorkerThread.class);
        Thread task = new Thread(worker);
        task.start();

	}
}
