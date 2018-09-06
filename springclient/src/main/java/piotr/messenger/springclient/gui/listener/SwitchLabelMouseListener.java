package piotr.messenger.springclient.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.util.Constants;

import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class SwitchLabelMouseListener extends MouseAdapter {

    private JLabel switchLabel;
    private JButton loginDataButton;
    private JLabel dataErrorLabel;


    @Autowired
    public void setSwitchLabel(@Qualifier("rawSwitchLabel") JLabel switchLabel) {
        this.switchLabel = switchLabel;
    }

    @Autowired
    public void setLoginDataButton(@Qualifier("logInButton") JButton loginDataButton) {
        this.loginDataButton = loginDataButton;
    }

    @Autowired
    public void setInvalidDataLabel(@Qualifier("dataErrorLabel") JLabel invalidDataLabel) {
        this.dataErrorLabel = invalidDataLabel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (switchLabel.getText().equals(Constants.SWITCH_TO_LOGIN)) {
            loginDataButton.setText(Constants.LOGIN_BUTTON);
            switchLabel.setText(Constants.SWITCH_TO_REGISTER);
        } else {
            loginDataButton.setText(Constants.REGISTER_BUTTON);
            switchLabel.setText(Constants.SWITCH_TO_LOGIN);
        }
        dataErrorLabel.setVisible(false);

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        switchLabel.setForeground(Constants.DARK_BLUE);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        switchLabel.setForeground(Constants.BLACK);
    }
}
