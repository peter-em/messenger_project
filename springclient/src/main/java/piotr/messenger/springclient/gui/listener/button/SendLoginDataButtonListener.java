package piotr.messenger.springclient.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.util.Constants;
import piotr.messenger.springclient.util.LoginData;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;

@Component
@Qualifier("sendLoginListener")
public class SendLoginDataButtonListener extends LoginDataActionListener {

    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel invalidData;

    @Autowired
    public void setLoginField(@Qualifier("loginField") JTextField loginField) {
        this.loginField = loginField;
    }

    @Autowired
    public void setPasswordField(JPasswordField passwordField) {
        this.passwordField = passwordField;
    }

    @Autowired
    public void setInvalidData(@Qualifier("dataErrorLabel") JLabel invalidData) {
        this.invalidData = invalidData;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        String username = loginField.getText();
        String password = new String(passwordField.getPassword());

        if (username.length() < 3) {
            invalidData.setText(Constants.TOO_SHORT);
            invalidData.setVisible(true);
        } else if (password.isEmpty()) {
            invalidData.setText(Constants.PSWD_EMPTY);
            invalidData.setVisible(true);
        } else {
            LoginData loginData = new LoginData();
            loginData.setLogin(username.trim());
            loginData.setPassword(password);
            int mode = (e.getActionCommand().equals(Constants.LOGIN_BUTTON)?Constants.LOGIN_MODE:Constants.REGISTER_MODE);
            loginData.setMode(mode);
            loginWindow.setLoginData(loginData);
            invalidData.setVisible(false);
        }
    }
}
