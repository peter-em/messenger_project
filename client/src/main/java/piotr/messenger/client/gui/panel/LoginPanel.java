package piotr.messenger.client.gui.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.api.Panel;

import javax.swing.*;
import java.awt.*;

@Component
public class LoginPanel implements Panel {

    private LayoutManager layout;
    private JLabel loginLabel;
    private JLabel passwdLabel;
    private JLabel dataErrorLabel;
    private JLabel switchModeLabel;
    private JTextField loginField;
    private JPasswordField passwdField;
    private JButton logInButton;
    private JButton cancelButton;
    private JTextPane loginInfo;

    @Override
    public JPanel init() {
        JPanel panel = new JPanel(layout);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2,6,4,6);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        panel.add(loginInfo, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,6,2,2);
        c.gridwidth = 1;
        c.weighty = 0.0;
        c.gridy = 1;
        panel.add(loginLabel, c);

        c.gridy = 2;
        panel.add(passwdLabel, c);


        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(2,2,2,6);
        c.weightx = 0.0;
        c.gridx = 1;
        c.gridy = 1;
        panel.add(loginField, c);

        c.gridy = 2;
        panel.add(passwdField, c);

        c.fill = GridBagConstraints.CENTER;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(2,0,0,0);
        c.weightx = 1.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        panel.add(dataErrorLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6,14,4,0);
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.gridy = 4;
        panel.add(logInButton, c);

        c.insets = new Insets(6,40,4,24);
        c.gridx = 1;
        c.weightx = 0.5;
        panel.add(cancelButton, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_END;
        c.insets = new Insets(6,6,6,6);
        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 0.5;
        c.weighty = 0.5;
        panel.add(switchModeLabel, c);

        return panel;
    }

    @Override
    @Autowired
    public void setLayoutManager(@Qualifier("gridBagLayout") LayoutManager layout) {
        this.layout = layout;
    }

    @Autowired
    public void setLogin(@Qualifier("loginLabel") JLabel login) {
        this.loginLabel = login;
    }

    @Autowired
    public void setPasswd(@Qualifier("passwdLabel") JLabel passwd) {
        this.passwdLabel = passwd;
    }

    @Autowired
    public void setDataError(@Qualifier("dataErrorLabel") JLabel dataError) {
        this.dataErrorLabel = dataError;
    }

    @Autowired
    public void setSwitchMode(@Qualifier("switchLabel") JLabel switchMode) {
        this.switchModeLabel = switchMode;
    }

    @Autowired
    public void setLoginField(@Qualifier("loginField") JTextField loginField) {
        this.loginField = loginField;
    }

    @Autowired
    public void setPasswdField(@Qualifier("passwdField") JPasswordField passwdField) {
        this.passwdField = passwdField;
    }

    @Autowired
    public void setLogInButton(@Qualifier("logInButton") JButton logInButton) {
        this.logInButton = logInButton;
    }

    @Autowired
    public void setCancelButton(@Qualifier("cancelButton") JButton cancelButton) {
        this.cancelButton = cancelButton;
    }

    @Autowired
    public void setLoginInfo(JTextPane loginInfo) {
        this.loginInfo = loginInfo;
    }
}
