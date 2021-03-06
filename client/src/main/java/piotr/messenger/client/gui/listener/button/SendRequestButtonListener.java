package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

@Component
public class SendRequestButtonListener implements ActionListener {

    private JTextField chooseUser;
    private BlockingQueue<TransferData> messageQueue;

    @Override
    public void actionPerformed(ActionEvent e) {

        //send conversation request button
        String user = chooseUser.getText();
        if (user.length() > 0) {
            messageQueue.add(new TransferData(user, LocalDateTime.now().toString(), Constants.C_REQUEST));
        }
    }

    @Autowired
    public void setChooseUser(@Qualifier("chooseUser") JTextField chooseUser) {
        this.chooseUser = chooseUser;
    }

    @Autowired
    public void setMessageQueue(@Qualifier("messageQueue") BlockingQueue<TransferData> messageQueue) {
        this.messageQueue = messageQueue;
    }
}
