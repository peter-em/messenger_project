package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.ConvComponents;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Component
public class LoadMessagesButtonListener implements ActionListener {

    private JTabbedPane appTabbs;
    private BlockingQueue<TransferData> messageQueue;
    private Map<String, ConvComponents> convComponentsMap;

    @Override
    public void actionPerformed(ActionEvent e) {

        int idx = appTabbs.getSelectedIndex();
        if (idx > 0) {
            String tabName = appTabbs.getTitleAt(idx);
            messageQueue.add(new TransferData(tabName, convComponentsMap.get(tabName).getOldestMessage().toString(), Constants.ARCHIVED_MSG));

        }
    }

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    @Autowired
    public void setMessageQueue(@Qualifier("messageQueue") BlockingQueue<TransferData> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Autowired
    public void setConvComponentsMap(Map<String, ConvComponents> convComponentsMap) {
        this.convComponentsMap = convComponentsMap;
    }
}
