package piotr.messenger.springclient.gui.panel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import piotr.messenger.springclient.api.Panel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AppPanel implements Panel {

    private LayoutManager layout;
    private java.util.List components;

    @Override
    public void setLayoutManager(LayoutManager layout) {
        this.layout = layout;
    }

//    @Override
    public void setComponents(List components) {
        this.components = components;
    }

    @Override
    public JPanel init() {
        JPanel panel = new JPanel();
        for (Object component : components) {
            panel.add((Component) component);
        }
        return panel;
    }
}
