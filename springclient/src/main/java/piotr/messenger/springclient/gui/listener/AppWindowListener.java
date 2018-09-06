package piotr.messenger.springclient.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class AppWindowListener extends WindowAdapter {

    private JTabbedPane appTabbs;
    private ArrayBlockingQueue<String> mainDataQueue;
    private JPanel mainPanel;

    @Autowired
    public void setMainPanel(@Qualifier("mainPanel") JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Autowired
    public void setAppTabbsWindow(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<String> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - BEGIN ------------//
    //handle closing ('X' button or Alt+F4)
    @Override
    public void windowClosing(WindowEvent we) {

        if (appTabbs == null)
            return;
        int nOption;
        int tabCount = appTabbs.getTabCount();

        if (tabCount > 1) {

            nOption = JOptionPane.showConfirmDialog(/*appFrame.getContentPane()*/mainPanel, "You have open conversations: " +
                            (tabCount - 1) + "\nClose anyway?", "Closing Programm",
                    JOptionPane.OK_CANCEL_OPTION);

            if (nOption == JOptionPane.CANCEL_OPTION || nOption == JOptionPane.CLOSED_OPTION)
                return;

        }
        mainDataQueue.add("q;");
    }
    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - END ------------//

}
