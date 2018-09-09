package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.LoginWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
@Qualifier("loginWindowListener")
public class LoginWindowListener extends WindowAdapter {

    private LoginWindow loginWindow;

    @Autowired
    public void setLoginWindow(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
    }

    //handle closing ('X' button or Alt+F4)
    @Override
    public void windowClosing(WindowEvent event) {

        loginWindow.setLoginData(null);

    }
}
