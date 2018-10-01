package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import piotr.messenger.client.util.TransferData;

import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;

// HANDLE CLICKABLE BUTTONS
public abstract class MainWindowActionListener implements ActionListener{

    BlockingQueue<TransferData> mainDataQueue;

    @Autowired
    public void setMainDataQueue(BlockingQueue<TransferData> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

}
