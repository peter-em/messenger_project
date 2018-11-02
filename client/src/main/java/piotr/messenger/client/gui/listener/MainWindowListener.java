package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.core.MainThread;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
@Qualifier("appWindowListener")
public class MainWindowListener extends WindowAdapter {

    private MainThread mainThread;

    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - BEGIN ------------//
    //handle closing ('X' button or Alt+F4)
    @Override
    public void windowClosing(WindowEvent we) {


        mainThread.stopWorker();
    }
    //------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - END ------------//

    @Autowired
    public void setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
    }
}
