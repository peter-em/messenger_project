package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.PrintWriteAreas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;

@Component
public class TabChangeListener implements ChangeListener {

    private Map<String, JTextArea> writeAreas;
    private JTabbedPane appTabbs;

    @Override
    public void stateChanged(ChangeEvent e) {

        int idx = appTabbs.getSelectedIndex();
        if (idx > 0) {
            String tabName = appTabbs.getTitleAt(idx);
            writeAreas.get(tabName).requestFocus();
        }

    }

    @Autowired
    public void setWriteAreas(PrintWriteAreas areas) {
        this.writeAreas = areas.getWriteAreas();
    }

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

}
