package piotr.messenger.springclient.gui.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.api.Panel;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.Dimension;
import java.awt.Insets;

@Component
public class CenterPanel implements Panel {

    private LayoutManager layout;
    private JScrollPane scrollPane;
    private JTextField chooseUser;
    private JButton sendRequest;

    @Override
    @Autowired
    public void setLayoutManager(@Qualifier("gridBagLayout") LayoutManager layout) {
        this.layout = layout;
    }

    @Autowired
    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    @Autowired
    public void setChooseUser(@Qualifier("chooseUser") JTextField chooseUser) {
        this.chooseUser = chooseUser;
    }

    @Autowired
    public void setSendRequest(@Qualifier("sendRequestButton") JButton sendRequest) {
        this.sendRequest = sendRequest;
    }

    @Override
    public JPanel init() {
        JPanel panel = new JPanel(layout);
        panel.setMaximumSize(new Dimension(360, 650));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 2, 2, 1);
        c.weightx = 1.0;
        c.weighty = 0.5;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.FIRST_LINE_START;


        panel.add(scrollPane, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 2, 0, 1);
        c.weightx = 1.0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        
        panel.add(chooseUser, c);

        c.insets = new Insets(0, 1, 0, 0);
        c.weightx = 0.0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        panel.add(sendRequest, c);

        return panel;
    }
}
