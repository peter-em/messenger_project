package piotr.messenger.client.gui;

import piotr.messenger.client.util.Constants;
import piotr.messenger.client.util.LoginData;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow implements ActionListener {

    private JFrame frame;
    private JTextField loginField;
    private JPanel loginPanel;
    private JPasswordField passwordField;
    private JButton sendButton;
    private JButton cancelButton;
    private JLabel loginLabel;
    private JLabel passwordLabel;
    private JTextPane loginInfo;
    private JLabel invalidLogin;
    private JLabel switchSignupLogin;
    private LoginData loginData;
    private volatile boolean dataReady = false;


    public LoginWindow() {

        frame = new JFrame("LOGIN WINDOW");

        frame.setMinimumSize(new Dimension(240, 340));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        frame.setContentPane(loginPanel);
        initComponents();

        centerWindow();

    }

    public void dataInvalid(String reason) {
        dataReady = false;
        invalidLogin.setText(reason);
        invalidLogin.setVisible(true);
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public boolean isLoginDataReady() {
        return dataReady;
    }

    public void showWindow() {
        frame.setVisible(true);
    }

    public void disposeWindow() {
        frame.setVisible(false);
        frame.dispose();
    }

    private void centerWindow() {
        int width = loginPanel.getMinimumSize().width;
        int height = loginPanel.getMinimumSize().height;
        GraphicsEnvironment gE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point point = gE.getCenterPoint();
        frame.setLocation(point.x - width/2, point.y - height);
    }

    private void initComponents() {

        loginInfo.setText(Constants.APP_INFO);
        StyledDocument doc = loginInfo.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);



        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(loginPanel);

        sendButton.addActionListener(this);
        cancelButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == sendButton) {

            String username = loginField.getText();
            String password = new String(passwordField.getPassword());

            System.out.println("username: " + "'" + username + "'");
            System.out.println("password: " + "'" + password + "'");

            if (username.length() < 3) {
                invalidLogin.setText(Constants.TOO_SHORT);
                invalidLogin.setVisible(true);
            } else if (password.isEmpty()) {
                invalidLogin.setText(Constants.PSWD_EMPTY);
                invalidLogin.setVisible(true);
            } else {
                loginData = new LoginData();
                loginData.setLogin(username.trim());
                loginData.setPassword(password);
                dataReady = true;
                invalidLogin.setVisible(false);
            }


        } else if (source == cancelButton) {
            loginData = null;
            dataReady = true;
            frame.setVisible(false);
            frame.dispose();
        }
    }

}
