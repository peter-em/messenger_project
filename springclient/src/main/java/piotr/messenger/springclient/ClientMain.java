package piotr.messenger.springclient;

import piotr.messenger.springclient.gui.MainWindow;

import javax.swing.SwingUtilities;

public class ClientMain {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(MainWindow::new);

	}
}
