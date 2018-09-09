package piotr.messenger.client.gui;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import piotr.messenger.client.config.AutoConfig;
import piotr.messenger.client.core.WorkerThread;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.GraphicsEnvironment;
import java.awt.Point;


public class MainWindow {


    private JFrame appFrame;
    private JButton closeTab;
    private JTabbedPane appTabbs;
    private JTextField chooseUser;
    private JLabel ownerName;
    private JLabel usersCount;


	public MainWindow() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AutoConfig.class);
        appFrame = context.getBean("mainFrame", JFrame.class);
        ownerName = context.getBean("ownerName", JLabel.class);
        usersCount = context.getBean("usersCount", JLabel.class);
        appTabbs = context.getBean(JTabbedPane.class);

        WorkerThread worker = context.getBean(WorkerThread.class);
        worker.init(this);
		Thread task = new Thread(worker);
		task.start();

        centerWindow(appFrame);

	}

    //centering app on the screen
    public static void centerWindow(JFrame window) {
        int width = window.getContentPane().getMinimumSize().width;
        int height = window.getContentPane().getMinimumSize().height;
        GraphicsEnvironment gE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point point = gE.getCenterPoint();
        window.setLocation(point.x - width/2, point.y - height);
    }


    // SETTERS AND GETTERS
    public JFrame getAppFrame() {
        return appFrame;
    }

    public JPanel getMainPanel() {
        return (JPanel) appFrame.getContentPane();
    }

    public JTabbedPane getAppPages() {
        return appTabbs;
    }

    public JLabel getOwnerName() {
        return ownerName;
    }

    public JLabel getUsersCount() {
        return usersCount;
    }

}

