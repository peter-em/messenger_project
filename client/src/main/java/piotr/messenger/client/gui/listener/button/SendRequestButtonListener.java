package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class SendRequestButtonListener extends MainWindowActionListener {

    private JTextField chooseUser;

    @Override
    public void actionPerformed(ActionEvent e) {

        //send conversation request button
        String user = chooseUser.getText();
        if (user.length() > 0) {
            mainDataQueue.add(new TransferData(Constants.C_REQUEST, user));
        }
    }

    @Autowired
    public void setChooseUser(@Qualifier("chooseUser") JTextField chooseUser) {
        this.chooseUser = chooseUser;
    }
}
