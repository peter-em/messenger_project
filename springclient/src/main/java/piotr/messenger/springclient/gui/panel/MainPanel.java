package piotr.messenger.springclient.gui.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import piotr.messenger.springclient.api.Panel;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.LayoutManager;

@org.springframework.stereotype.Component
public class MainPanel implements Panel {

    private LayoutManager layout;
    private JToolBar toolBar;
    private JPanel southPanel;
    private JTabbedPane tabbedPane;

    @Override
    @Autowired
    public void setLayoutManager(@Qualifier("borderLayout") LayoutManager layout) {
        this.layout = layout;
    }

    @Autowired
    public void setToolBar(JToolBar toolBar) {
        this.toolBar = toolBar;
    }

    @Autowired
    public void setSouthPanel(@Qualifier("southPanel") JPanel southPanel) {
        this.southPanel = southPanel;
    }

    @Autowired
    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }


    @Override
//    @PostConstruct
    public JPanel init() {
        JPanel panel = new JPanel(layout);
        panel.add(toolBar, BorderLayout.PAGE_START);
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.PAGE_END);

        return panel;
    }
}