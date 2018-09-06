package piotr.messenger.springclient.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piotr.messenger.springclient.gui.listener.button.CancelLoginButtonListener;
import piotr.messenger.springclient.gui.listener.button.SendLoginDataButtonListener;
import piotr.messenger.springclient.gui.panel.LoginPanel;
import piotr.messenger.springclient.util.Constants;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseListener;

@Configuration
public class LoginWindowConfig {

    @Bean(name="loginFrame")
    public JFrame getFrame(@Qualifier("loginPanel") JPanel loginPanel) {

        JFrame frame = new JFrame("LOGIN");

        frame.setMinimumSize(new Dimension(240, 340));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setContentPane(loginPanel);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(loginPanel);

        return frame;
    }

    @Bean
    @Qualifier("loginPanel")
    public JPanel getLoginPanel(LoginPanel loginPanel) {
        return loginPanel.init();
    }

    @Bean
    @Qualifier("loginLabel")
    public JLabel getLoginLabel() {
        JLabel login = new JLabel();
        login.setFont(getFont());
        login.setText("Username:");
        return login;
    }

    @Bean
    @Qualifier("passwdLabel")
    public JLabel getPasswdLabel() {
        JLabel passwd = new JLabel();
        passwd.setFont(getFont());
        passwd.setText("Password:");
        return passwd;
    }

    @Bean
    @Qualifier("dataErrorLabel")
    public JLabel getDataErrorLabel() {
        JLabel dataError = new JLabel();
        dataError.setFont(new Font(dataError.getFont().getName(), Font.BOLD, 12));
        dataError.setHorizontalTextPosition(SwingConstants.CENTER);
        dataError.setForeground(Constants.DATA_ERROR);
        dataError.setVisible(false);
        return dataError;
    }

    @Bean
    @Qualifier("rawSwitchLabel")
    public JLabel getRawSwitchLabel() {
        JLabel switchLabel = new JLabel();
        switchLabel.setText(Constants.SWITCH_TO_REGISTER);
        switchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return switchLabel;
    }

    @Bean
    @Qualifier("switchLabel")
    public JLabel getSwitchLabel(@Qualifier("rawSwitchLabel") JLabel rawSwitchLabel,
                                 MouseListener mouseListener) {
        rawSwitchLabel.addMouseListener(mouseListener);
        return rawSwitchLabel;
    }

    @Bean
    public Dimension getFieldSize() {
        return new Dimension(130,24);
    }

    @Bean
    @Qualifier("loginField")
    public JTextField getLoginField(Dimension dimension) {
        JTextField login = new JTextField();
        login.setPreferredSize(dimension);
        login.setFont(getFont());

        return login;
    }

    @Bean
    @Qualifier("passwdField")
    public JPasswordField getPasswordField(Dimension dimension) {
        JPasswordField passwd = new JPasswordField();
        passwd.setPreferredSize(dimension);
        return passwd;
    }

    private Insets getButtonMargin() {
        return new Insets(2,6,2,6);
    }

    private Font getFont() {
        return new Font("Consolas", Font.BOLD, 12);
    }

    @Bean
    @Qualifier("logInButton")
    public JButton getLogInButton(SendLoginDataButtonListener listener) {
        JButton logIn = new JButton(Constants.LOGIN_BUTTON);
        logIn.setRequestFocusEnabled(false);
        logIn.setMargin(getButtonMargin());
        logIn.addActionListener(listener);
        return logIn;
    }

    @Bean
    @Qualifier("cancelButton")
    public JButton getCancelButton(CancelLoginButtonListener listener) {
        JButton cancel = new JButton("Cancel");
        cancel.setRequestFocusEnabled(false);
        cancel.setMargin(getButtonMargin());
        cancel.addActionListener(listener);
        return cancel;
    }

    @Bean
    public JTextPane getLoginInfoField() {

        JTextPane loginInfoField = new JTextPane();
        StyledDocument doc = loginInfoField.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        loginInfoField.setText(Constants.APP_INFO);
        loginInfoField.setFont(getFont());
        loginInfoField.setMargin(new Insets(4,4,4,4));
        loginInfoField.setEditable(false);
        loginInfoField.setFocusable(false);
        loginInfoField.setOpaque(false);
        return loginInfoField;
    }

}
