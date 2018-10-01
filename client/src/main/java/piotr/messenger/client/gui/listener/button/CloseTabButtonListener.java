package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class CloseTabButtonListener extends MainWindowActionListener {

    private JTabbedPane appTabbs;

    @Override
    public void actionPerformed(ActionEvent e) {

        //close conversation button
        int idx = appTabbs.getSelectedIndex();
        if (idx > 0) {
            String tabName = appTabbs.getTitleAt(idx);
            mainDataQueue.add(new TransferData(Constants.C_TERMINATE, tabName));
            appTabbs.removeTabAt(idx);

        }
    }

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }
}
