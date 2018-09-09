package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import piotr.messenger.client.gui.LoginWindow;

import java.awt.event.ActionListener;

public abstract class LoginDataActionListener implements ActionListener {

//    LoginData loginData;
    LoginWindow loginWindow;

//    @Autowired
//    public void setLoginData(LoginData loginData) {
//        this.loginData = loginData;
//    }

    @Autowired
    public void setLoginWindow(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
    }

}
