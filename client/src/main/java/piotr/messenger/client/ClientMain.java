package piotr.messenger.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import piotr.messenger.client.gui.MainWindow;

import javax.swing.SwingUtilities;

@SpringBootApplication
public class ClientMain {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(MainWindow::new);

	}
}
