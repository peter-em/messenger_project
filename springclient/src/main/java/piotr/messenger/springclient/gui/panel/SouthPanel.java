package piotr.messenger.springclient.gui.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import piotr.messenger.springclient.api.Panel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;


@org.springframework.stereotype.Component
public class SouthPanel implements Panel {

    private LayoutManager layout;
    private JLabel usersCount;
    private JLabel ownerName;

    @Override
    @Autowired
    public void setLayoutManager(@Qualifier("gridBagLayout") LayoutManager layout) {
        this.layout = layout;
    }

    @Autowired
    public void setUsersCount(@Qualifier("usersCount") JLabel usersCount) {
        this.usersCount = usersCount;
    }

    @Autowired
    public void setOwnerName(@Qualifier("ownerName") JLabel ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public JPanel init() {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 74, 12)));

        GridBagConstraints c = new GridBagConstraints();

        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1,20));

        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(0,4,0,2);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ownerName, c);

        c = new GridBagConstraints();
        panel.add(separator);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(0,2,0,2);
        panel.add(usersCount, c);

        return panel;
    }
}
