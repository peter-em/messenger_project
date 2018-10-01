package piotr.messenger.client.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.client.core.ConversationThread;
import piotr.messenger.client.gui.PrintWriteAreas;
import piotr.messenger.client.gui.listener.ConvKeyListener;
import piotr.messenger.client.util.ConvParameters;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


@Component
public class ConversationService {

    private final @Getter Map<String, BlockingQueue<String>> writeQueues = new HashMap<>();
    private @Getter PrintWriteAreas areas;
    private ConvKeyListener convKeyListener;


    public void stopConvThreads() {

        for (BlockingQueue<String> queue : writeQueues.values()) {
            queue.add("");
        }
    }

    // create tab and thread for new conversation
    public void createConversation(ConvParameters params, JTabbedPane appTabs) {

        createConvPage(params.getRemoteUser(), appTabs);
        ConversationThread convThread = new ConversationThread(params, appTabs, writeQueues, areas);
        new Thread(convThread).start();
    }

    //method creating new tab for conversation
    private void createConvPage(String convUser, JTabbedPane appTabbs) {

        JTextArea printArea = new JTextArea();
        printArea.setFont(Constants.AREA_FONT);
        printArea.setEditable(false);
        printArea.setWrapStyleWord(true);
        printArea.setLineWrap(true);
        printArea.setBackground(Constants.TEXT_AREA_COLOR);
        printArea.setForeground(Constants.TEXT_COLOR);

        JTextArea writeArea = new JTextArea();
        writeArea.setFont(Constants.AREA_FONT);
        writeArea.setWrapStyleWord(true);
        writeArea.setLineWrap(true);
        writeArea.setBackground(Constants.TEXT_AREA_COLOR);
        writeArea.setCaretColor(Constants.TEXT_COLOR);
        writeArea.setForeground(Constants.TEXT_COLOR);
        writeArea.addKeyListener(convKeyListener);

        JScrollPane scroll1 = new JScrollPane(printArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scroll2 = new JScrollPane(writeArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new BorderLayout());

        scroll2.setPreferredSize(new Dimension(0, 60));
        panel.add(scroll2, BorderLayout.SOUTH);
        panel.add(scroll1, BorderLayout.CENTER);


        appTabbs.addTab(convUser, panel);
        areas.getWriteAreas().put(convUser, writeArea);
        areas.getPrintAreas().put(convUser, printArea);


        if (appTabbs.getSelectedIndex() == 0)
            for (int i = 1; i < appTabbs.getTabCount(); i++) {
                if (appTabbs.getTitleAt(i).equals(convUser)) {
                    appTabbs.setSelectedIndex(i);
                    return;
                }
            }
    }

    @Autowired
    public void setAreas(PrintWriteAreas areas) {
        this.areas = areas;
    }

    @Autowired
    public void setConvKeyListener(ConvKeyListener convKeyListener) {
        this.convKeyListener = convKeyListener;
    }
}
