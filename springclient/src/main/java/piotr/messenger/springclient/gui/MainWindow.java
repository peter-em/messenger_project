package piotr.messenger.springclient.gui;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piotr.messenger.springclient.config.AutoConfig;
import piotr.messenger.springclient.core.WorkerThread;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;


public class MainWindow {


    private JFrame appFrame;
    private JButton closeTab;
    private JTabbedPane appPages;
    private JTextField chooseUser;
    private JLabel ownerName;
    private JLabel usersCount;


	public MainWindow() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);
        appFrame = context.getBean(JFrame.class);
        ownerName = context.getBean("ownerName", JLabel.class);
        usersCount = context.getBean("usersCount", JLabel.class);

        WorkerThread worker = context.getBean(WorkerThread.class);
        worker.init(this);
		Thread task = new Thread(worker);
		task.start();

        centerFrame();

	}


    //centering app on the screen
    private void centerFrame() {
        Dimension windowSize = appFrame.getPreferredSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();
        int dx = centerPoint.x - windowSize.width/2;
        int dy = centerPoint.y - windowSize.height/2;
        appFrame.setLocation(dx, dy);
    }


    // SETTERS AND GETTERS
    public JFrame getAppFrame() {
        return appFrame;
    }

    public JPanel getMainPanel() {
        return (JPanel) appFrame.getContentPane();
    }

    public JTabbedPane getAppPages() {
        return appPages;
    }

    public JLabel getOwnerName() {
        return ownerName;
    }

    public JLabel getUsersCount() {
        return usersCount;
    }

}

