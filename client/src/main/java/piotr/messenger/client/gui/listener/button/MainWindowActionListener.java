package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import piotr.messenger.client.util.TransferData;

import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;

// HANDLE CLICKABLE BUTTONS
public abstract class MainWindowActionListener implements ActionListener{

    BlockingQueue<TransferData> mainDataQueue;

    @Autowired
    public void setMainDataQueue(@Qualifier("mainQueue") BlockingQueue<TransferData> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

}
