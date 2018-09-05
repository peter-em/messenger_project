package piotr.messenger.springclient.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.ActionListener;
import java.util.concurrent.ArrayBlockingQueue;

// HANDLE CLICKABLE BUTTONS
public abstract class ButtonListener implements ActionListener{

    ArrayBlockingQueue<String> mainDataQueue;

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<String> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

}