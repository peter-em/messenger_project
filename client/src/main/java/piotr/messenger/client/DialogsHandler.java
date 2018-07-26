package piotr.messenger.client;

import javax.swing.*;

/*
* class used to display pop-up information window
* */

public class DialogsHandler implements Runnable{
	public static final int CONV_STARTED = 1;
	public static final int CONV_REQUEST = 2;
	public static final int CONV_REFUSED = 3;
	private String convUser;
	private int dialogType;
	private JPanel rootPanel;

	DialogsHandler(int dialogType, String convUser, JPanel rootPanel) {
		this.dialogType = dialogType;
		this.convUser = convUser;
		this.rootPanel = rootPanel;
	}

	@Override
	public void run() {
		if (dialogType == 1) {
			JOptionPane.showMessageDialog(rootPanel, "Conversation with user " + convUser
					  + "\nhas already started", "Request Refused", JOptionPane.INFORMATION_MESSAGE);
		} else if (dialogType == 2) {
			int answr = JOptionPane.showConfirmDialog(rootPanel, "User '" + convUser + "' wants to talk,\n"
					  + "Do You agree?", "New Conversation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (answr == JOptionPane.NO_OPTION || answr == JOptionPane.CLOSED_OPTION) {
				System.out.println("Conversation refused");
				ClientGUI.mainDataQueue.add(("n;" + convUser + ";"));
			} else {
				System.out.println("Conversation accepted");
				ClientGUI.mainDataQueue.add(("y;" + convUser + ";"));
			}
		} else if (dialogType == 3) {
			JOptionPane.showMessageDialog(rootPanel, "User '" + convUser + "' refused conversation",
					  "Conversation canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
