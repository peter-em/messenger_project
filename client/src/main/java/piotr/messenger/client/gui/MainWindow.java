package piotr.messenger.client.gui;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.client.service.WindowMethods;

import javax.annotation.PostConstruct;
import javax.swing.*;

@Component
public class MainWindow {

    private @Getter JFrame mainFrame;
    private @Getter JTabbedPane appTabbs;
    private @Getter JLabel ownerName;
    private @Getter JLabel usersCount;
    private @Getter DefaultListModel<String> defListModel;


    @PostConstruct
    public void initWindow() {
        WindowMethods.centerWindow(mainFrame);
    }

    public void disposeWindow() {
        mainFrame.setVisible(false);
        mainFrame.dispose();
    }

    @Autowired
    public void setMainFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Autowired
    public void setAppTabbs(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    @Autowired
    public void setOwnerName(JLabel ownerName) {
        this.ownerName = ownerName;
    }

    @Autowired
    public void setUsersCount(JLabel usersCount) {
        this.usersCount = usersCount;
    }

    @Autowired
    public void setDefListModel(DefaultListModel<String> defListModel) {
        this.defListModel = defListModel;
    }
}

