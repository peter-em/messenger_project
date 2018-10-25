package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.core.MainThread;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
@Qualifier("appWindowListener")
public class MainWindowListener extends WindowAdapter {

    private JTabbedPane appTabbs;
    private JPanel mainPanel;
    private MainThread mainThread;

    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - BEGIN ------------//
    //handle closing ('X' button or Alt+F4)
    @Override
    public void windowClosing(WindowEvent we) {

        int tabCount = appTabbs.getTabCount();
        if (tabCount > 1) {

            int nOption = JOptionPane.showConfirmDialog(/*appFrame.getContentPane()*/mainPanel, "You have open conversations: " +
                            (tabCount - 1) + "\nClose anyway?", "Closing Programm",
                    JOptionPane.OK_CANCEL_OPTION);

            if (nOption == JOptionPane.CANCEL_OPTION || nOption == JOptionPane.CLOSED_OPTION)
                return;

        }
        mainThread.stopWorker();
    }
    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - END ------------//

    @Autowired
    public void setMainPanel(@Qualifier("mainPanel") JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Autowired
    public void setAppTabbsWindow(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    @Autowired
    public void setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
    }
}