package piotr.messenger.springclient.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class AppWindowListener extends WindowAdapter {

    private JFrame appFrame;
    private JTabbedPane appTabbs;
    private ArrayBlockingQueue<String> mainDataQueue;

    @Autowired
    public void setAppFrame(JFrame appFrame) {
        this.appFrame = appFrame;
    }

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
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

        int nOption;
        int tabCount = appTabbs.getTabCount();
        if (tabCount > 1) {

            nOption = JOptionPane.showConfirmDialog(appFrame.getContentPane(), "You have open conversations: " +
                            (tabCount - 1) + "\nClose anyway?", "Closing Programm",
                    JOptionPane.OK_CANCEL_OPTION);

            if (nOption == JOptionPane.CANCEL_OPTION || nOption == JOptionPane.CLOSED_OPTION)
                return;

        }
        mainDataQueue.add("q;");
        appFrame.setVisible(false);
//        appFrame.dispose();
    }
    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - END ------------//

}
