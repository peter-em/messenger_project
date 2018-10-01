package piotr.messenger.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import piotr.messenger.client.gui.PrintWriteAreas;
import piotr.messenger.client.gui.listener.button.SendRequestButtonListener;
import piotr.messenger.client.gui.panel.CenterPanel;
import piotr.messenger.client.gui.panel.MainPanel;
import piotr.messenger.client.gui.panel.SouthPanel;
import piotr.messenger.client.gui.listener.button.CloseTabButtonListener;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.WindowListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
@Slf4j
public class MainWindowBeans {

    @Bean(name="mainFrame")
    public JFrame getFrame(@Qualifier("mainPanel") JPanel mainPanel,
                           @Qualifier("appWindowListener") WindowListener windowListener) {
        JFrame appFrame = new JFrame();
        appFrame.setTitle(Constants.APP_NAME);
        appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        appFrame.setPreferredSize(new Dimension(300, 500));
        appFrame.setResizable(false);

        appFrame.setContentPane(mainPanel);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            log.error("MainWindowBeans: {}", ex.getMessage());
        }
        SwingUtilities.updateComponentTreeUI(mainPanel);
        appFrame.pack();

        appFrame.addWindowListener(windowListener);
        return appFrame;
    }

    @Bean
    @Qualifier("mainPanel")
    public JPanel getMainPanel(MainPanel mainPanel) {
        return mainPanel.init();
    }


    @Bean
    @Qualifier("centerPanel")
    public JPanel getCenterPanel(CenterPanel centerPanel) {
        return centerPanel.init();
    }

    @Bean
    @Qualifier("southPanel")
    public JPanel getSouthPanel(SouthPanel southPanel) {
        return southPanel.init();
    }

    @Bean
    public JToolBar getToolBar(@Qualifier("closeTabButton") JButton button) {
        JToolBar toolBar = new JToolBar(SwingConstants.NORTH);
        toolBar.setBackground(new Color(62,90,49));
        toolBar.setPreferredSize(new Dimension(50,27));
        toolBar.setMinimumSize(new Dimension(46,22));
        toolBar.setMaximumSize(new Dimension(58,27));
        toolBar.setMargin(new Insets(2,0,0,0));
        toolBar.setBorder(BorderFactory.createEmptyBorder(1,4,1,4));
        toolBar.add(button);

        return toolBar;
    }

    @Bean
    public JTabbedPane getTabbedPane(@Qualifier("centerPanel") JPanel centerPanel,
                                     PrintWriteAreas areas) {
        JTabbedPane pane = new JTabbedPane();
        pane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        pane.add("Clients", centerPanel);
        pane.setFocusable(false);

        pane.addChangeListener((ChangeEvent event) -> {

                int idx = pane.getSelectedIndex();
                if (idx > 0) {
                    String tabName = pane.getTitleAt(idx);
                    areas.getWriteAreas().get(tabName).requestFocus();
                }
        });
        return pane;
    }

    @Bean
    public JScrollPane getScrollPane(JList<String> list) {
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    @Bean
    public JList<String> getUsersList(@Qualifier("chooseUser") JTextField chooseUser,
                                      DefaultListModel<String> defListModel) {
        JList<String> users = new JList<>();
        users.setFixedCellHeight(20);
        users.setFixedCellWidth(600);
        users.setFont(new Font("Consolas", Font.BOLD, 16));
        users.setLayoutOrientation(JList.VERTICAL);
        users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        users.setModel(defListModel);
        users.setForeground(Constants.LIST_ELEMENT);

        users.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            ListSelectionModel lsm = (ListSelectionModel) event.getSource();
            if (!lsm.getValueIsAdjusting()) {
                if (lsm.isSelectionEmpty()) {
                    lsm.clearSelection();
                    chooseUser.setText("");
                } else {
                    String user = defListModel.getElementAt(lsm.getLeadSelectionIndex());
                    chooseUser.setText(user);
                }
            }
        });

        return users;
    }

    @Bean
    @Qualifier("chooseUser")
    public JTextField getChooseUserField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Consolas", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(-1, 24));
        field.setEditable(false);
        return field;
    }

    @Bean
    @Qualifier("closeTabButton")
    public JButton getCloseButton(CloseTabButtonListener listener) {
        JButton button = new JButton("Close Tab");
        button.setBorderPainted(true);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(50,25));
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setRequestFocusEnabled(false);
        button.setRolloverEnabled(false);
        button.setSelected(false);
        button.setFocusPainted(true);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 12));
        button.setContentAreaFilled(false);
        button.addActionListener(listener);
        return button;
    }

    @Bean
    @Qualifier("sendRequestButton")
    public JButton getSendButton(SendRequestButtonListener listener) {
        JButton button = new JButton("Send");
        button.setOpaque(true);
        button.setSelected(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.addActionListener(listener);
        return button;
    }

    @Bean
    @Qualifier("borderLayout")
    public LayoutManager getBorderLayout() {
        return new BorderLayout();
    }

    @Bean
    @Qualifier("gridBagLayout")
    public LayoutManager getGridBagLayout() {
        return new GridBagLayout();
    }

    @Bean(name = "ownerName")
    @Qualifier("ownerName")
    public JLabel getOwnerLable() {
        JLabel owner = new JLabel("Owner");
        owner.setHorizontalTextPosition(SwingConstants.LEADING);
        owner.setFont(new Font(owner.getFont().getName(), Font.BOLD, 13));

        return owner;
    }

    @Bean(name = "usersCount")
    @Qualifier("usersCount")
    public JLabel getCountLabel() {
        JLabel count = new JLabel("Count");
        count.setHorizontalAlignment(SwingConstants.CENTER);
        Dimension size = new Dimension(90, 16);
        count.setMaximumSize(size);
        count.setMinimumSize(size);
        count.setPreferredSize(size);
        return count;
    }

    @Bean
    public BlockingQueue<TransferData> getQueue() {
        return new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE);
    }

    @Bean
    public DefaultListModel<String> getListModel() {
        return new DefaultListModel<>();
    }

}
