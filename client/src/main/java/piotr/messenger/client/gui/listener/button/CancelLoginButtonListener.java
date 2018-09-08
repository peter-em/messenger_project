package piotr.messenger.client.gui.listener.button;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;

@Component
@Qualifier("cancelLoginListener")
public class CancelLoginButtonListener extends LoginDataActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        loginWindow.setLoginData(null);

    }
}
