package piotr.messenger.client;

import piotr.messenger.client.gui.MainWindow;

import javax.swing.SwingUtilities;

public class ClientMain {

//    public boolean isItTrue

	public static void main(String[] args) {

		SwingUtilities.invokeLater(MainWindow::new);

	}
}
