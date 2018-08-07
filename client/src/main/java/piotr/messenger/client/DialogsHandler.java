package piotr.messenger.client;

import javax.swing.*;

/*
* class used to display pop-up information window
* */

class DialogsHandler {
	private JPanel rootPanel;

    DialogsHandler(JPanel rootPanel) {
		this.rootPanel = rootPanel;
	}

	void hasStarted(String user) {
        JOptionPane.showMessageDialog(rootPanel, "Conversation with user " + user
                + "\nhas already started", "Request Refused", JOptionPane.INFORMATION_MESSAGE);
    }

    void refused(String user) {
        JOptionPane.showMessageDialog(rootPanel, "User '" + user + "' refused conversation",
                "Conversation canceled", JOptionPane.INFORMATION_MESSAGE);
    }

    String convInvite(String user) {
        int answr = JOptionPane.showConfirmDialog(rootPanel, "User '" + user + "' wants to talk,\n"
                + "Do You agree?", "New Conversation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (answr == JOptionPane.NO_OPTION || answr == JOptionPane.CLOSED_OPTION) {
            return "n;" + user + ";";
        } else {
            return "y;" + user + ";";
        }
    }

}
