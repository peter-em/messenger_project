package piotr.messenger.client.gui;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import piotr.messenger.client.config.AutoConfig;
import piotr.messenger.client.core.WorkerThread;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

@Component
public class MainWindow {

    private @Getter JFrame mainFrame;
    private @Getter JTabbedPane appTabbs;
    private @Getter JLabel ownerName;
    private @Getter JLabel usersCount;
    private @Getter DefaultListModel<String> defListModel;


    @PostConstruct
    public void initWindow() {
        MainWindow.centerWindow(mainFrame);
    }

    //centering app on the screen
    public static void centerWindow(JFrame window) {
        int width = window.getContentPane().getMinimumSize().width;
        int height = window.getContentPane().getMinimumSize().height;
        GraphicsEnvironment gE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point point = gE.getCenterPoint();
        window.setLocation(point.x - width/2, point.y - height);
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

