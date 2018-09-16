package piotr.messenger.client.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.util.LoginData;
import piotr.messenger.library.Constants;

import javax.annotation.PostConstruct;
import javax.swing.*;

@Component
public class LoginWindow {

    private JFrame loginFrame;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel invalidLogin;
    private JButton sendButton;
    private LoginData loginData;
    private volatile boolean dataReady = false;


    @PostConstruct
    public void initWindow() {
        MainWindow.centerWindow(loginFrame);
    }

    public void dataInvalid() {
        dataReady = false;
        if (sendButton.getText().equals(Constants.LOGIN_BUTTON)) {
            invalidLogin.setText(Constants.LOGIN_ERROR);
        } else {
            invalidLogin.setText(Constants.REGISTER_ERROR);
        }
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

    @Autowired
    public void setSendButton(@Qualifier("logInButton") JButton sendButton) {
        this.sendButton = sendButton;
    }
}
