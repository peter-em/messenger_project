package piotr.messenger.springclient.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Component
public class CloseTabButtonListener extends MainWindowActionListener {

    private JTabbedPane appTabbs;

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //close conversation button
        int idx = appTabbs.getSelectedIndex();
        if (idx > 0) {
            String tabName = appTabbs.getTitleAt(idx);
            mainDataQueue.add("t;" + tabName + ";");
            appTabbs.removeTabAt(idx);

        }
    }
}
