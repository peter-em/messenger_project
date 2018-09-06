package piotr.messenger.springclient.gui.listener.button;

import org.springframework.beans.factory.annotation.Autowired;
import piotr.messenger.springclient.gui.LoginWindow;
import piotr.messenger.springclient.util.LoginData;

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
