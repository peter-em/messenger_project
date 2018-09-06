package piotr.messenger.springclient.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.util.Constants;
import piotr.messenger.springclient.util.LoginData;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Component
public class LoginWindow {

    private JFrame loginFrame;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel invalidLogin;
    private LoginData loginData;
    private volatile boolean dataReady = false;


    @PostConstruct
    public void initWindow() {
        MainWindow.centerWindow(loginFrame);
    }

    public void dataInvalid(String reason) {
        dataReady = false;
        invalidLogin.setText(reason);
        invalidLogin.setVisible(true);
        loginField.selectAll();
        loginField.requestFocus();
        passwordField.setText("");
        loginData = null;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
        dataReady = true;
    }

    public boolean isLoginDataReady() {
        return dataReady;
    }

    public void showWindow() {
        loginFrame.setVisible(true);
    }

    public void disposeWindow() {
        loginFrame.setVisible(false);
        loginFrame.dispose();
    }

    public void setDataReady(boolean dataReady) {
        this.dataReady = dataReady;
    }

    @Autowired
    public void setLoginFrame(JFrame loginFrame) {
        this.loginFrame = loginFrame;
    }

    @Autowired
    public void setLoginField(@Qualifier("loginField") JTextField loginField) {
        this.loginField = loginField;
    }

    @Autowired
    public void setPasswordField(JPasswordField passwordField) {
        this.passwordField = passwordField;
    }

    @Autowired
    public void setInvalidLogin(@Qualifier("dataErrorLabel") JLabel invalidLogin) {
        this.invalidLogin = invalidLogin;
    }
}
