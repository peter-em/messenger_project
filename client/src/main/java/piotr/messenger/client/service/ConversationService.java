package piotr.messenger.client.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.core.ConversationThread;
import piotr.messenger.client.gui.listener.ConvKeyListener;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class ConversationService {

    private final Logger logger;
    private final Map<String, ArrayBlockingQueue<String>> readQueues;
    private final @Getter Map<String, ArrayBlockingQueue<String>> writeQueues;
    private @Getter Map<String, JTextArea> writeAreas;
    private @Getter Map<String, JTextArea> printAreas;
    private ConvKeyListener convKeyListener;

    public ConversationService() {
        logger = LoggerFactory.getLogger(ConversationService.class);
        readQueues = new HashMap<>();
        writeQueues = new HashMap<>();
    }

    //check queues if there are any data from readers to display
    public void readDataQueues(JTabbedPane appTabbs) throws InterruptedException {
        String input;
        for (String convKey : readQueues.keySet()) {
            ArrayBlockingQueue queue = readQueues.get(convKey);
            if (!queue.isEmpty()) {
                input = (String) queue.take();

                //special case, due to inability to send empty string,
                //only conv reader can input it to signal EoC (End of Conversation)
                if (input.length() == 0) {

                    int index = getConvTabIndex(convKey, appTabbs);
                    appTabbs.removeTabAt(index);
                    removeMapings(convKey);

                } else {
                    // process received message
                    MessagePrinter.printMessage(convKey, input, printAreas.get(convKey));
                }
            }
        }
    }

    private int getConvTabIndex(String convKey, JTabbedPane appTabbs) {
        for (int i = 1; i < appTabbs.getTabCount(); i++) {
            if (appTabbs.getTitleAt(i).equals(convKey)) {
                return i;
            }
        }
        return 0;
    }

    public void stopConvThreads() {

        for (ArrayBlockingQueue<String> queue : writeQueues.values()) {
            queue.add("");
        }
    }


    //creating reader and writer for new conv
    public boolean doConnect(String host, int port, String convUser) {

        SocketChannel convChannel;
        try {
            convChannel = SocketChannel.open();
            convChannel.configureBlocking(false);
            convChannel.connect(new InetSocketAddress(host, port));

            int time = 0;
            while (!convChannel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(10);
                if (++time > 10) {
                    throw new IOException("Connection could not be finalized");
                }
            }

            readQueues.put(convUser, new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));
            writeQueues.put(convUser, new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));

            ConversationThread convThread = new ConversationThread(convChannel,
                                            readQueues.get(convUser), writeQueues.get(convUser));
            new Thread(convThread).start();

        } catch (IOException ioEx) {
            logger.error("Failed to create new conversation ({})!", ioEx.getMessage());
            return false;
        } catch (InterruptedException itrEx) {
            logger.error(itrEx.getMessage());
            Thread.currentThread().interrupt();
        }
        return true;
    }

    //removing blocking queues mapped to conversation after closing it
    public void removeMapings(String convUser) {
        writeQueues.remove(convUser);
        readQueues.remove(convUser);
        writeAreas.remove(convUser);
        printAreas.remove(convUser);
    }

    //method creating new tab for conversation
    public void createConvPage(String convUser, JTabbedPane appTabbs) {

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
        writeAreas.put(convUser, writeArea);
        printAreas.put(convUser, printArea);

        if (appTabbs.getSelectedIndex() == 0)
            for (int i = 1; i < appTabbs.getTabCount(); i++) {
                if (appTabbs.getTitleAt(i).equals(convUser)) {
                    appTabbs.setSelectedIndex(i);
                    return;
                }
            }
    }


    @Autowired
    public void setPrintAreas(@Qualifier("printAreas") Map<String, JTextArea> printAreas) {
        this.printAreas = printAreas;
    }

    @Autowired
    public void setWriteAreas(@Qualifier("writeAreas") Map<String, JTextArea> writeAreas) {
        this.writeAreas = writeAreas;
    }

    @Autowired
    public void setConvKeyListener(ConvKeyListener convKeyListener) {
        this.convKeyListener = convKeyListener;
    }

}
