package piotr.messenger.springclient.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class SendButtonListener extends ButtonListener {

    private JTextField chooseUser;

    @Autowired
    public void setChooseUser(JTextField chooseUser) {
        this.chooseUser = chooseUser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //send conversation request button
        String user = chooseUser.getText();
        if (user.length() > 0) {
            mainDataQueue.add("a;" + user + ";");
        }
    }
}
