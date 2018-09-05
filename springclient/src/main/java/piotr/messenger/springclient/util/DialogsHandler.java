package piotr.messenger.springclient.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.JPanel;
import javax.swing.JOptionPane;

/*
* class used to display pop-up information window
* */

@Component
public class DialogsHandler {
	private JPanel rootPanel;

	@Autowired
    public DialogsHandler(@Qualifier("mainPanel") JPanel rootPanel) {
		this.rootPanel = rootPanel;
	}

    public void hasStarted(String user) {
        JOptionPane.showMessageDialog(rootPanel, "Conversation with user " + user
                + "\nhas already started", "Request Refused", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refused(String user) {
        JOptionPane.showMessageDialog(rootPanel, "User '" + user + "' refused conversation",
                "Conversation canceled", JOptionPane.INFORMATION_MESSAGE);
    }

    public String convInvite(String user) {
        int answr = JOptionPane.showConfirmDialog(rootPanel, "User '" + user + "' wants to talk,\n"
                + "Do You agree?", "New Conversation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (answr == JOptionPane.NO_OPTION || answr == JOptionPane.CLOSED_OPTION) {
            return "n;" + user + ";";
        } else {
            return "y;" + user + ";";
        }
    }

}
