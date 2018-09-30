package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import piotr.messenger.client.util.TransferData;

import java.awt.event.ActionListener;
import java.util.concurrent.ArrayBlockingQueue;

// HANDLE CLICKABLE BUTTONS
public abstract class MainWindowActionListener implements ActionListener{

    ArrayBlockingQueue<TransferData> mainDataQueue;

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<TransferData> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

}
