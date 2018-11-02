package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.ConvComponents;
import piotr.messenger.client.service.WindowMethods;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


@Component
@Qualifier("convKeyListener")
public class ConvKeyListener extends KeyAdapter {

    private Map<String, ConvComponents> convComponentsMap;
    private BlockingQueue<TransferData> messageQueue;
    private JTabbedPane appTabbs;

    // ---------- HANDLE KEY PRESSES ----------- //
    @Override
    public void keyPressed(KeyEvent e) {



        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            e.consume();
            int idx = appTabbs.getSelectedIndex();
            if (idx > 0) {
                String tabName = appTabbs.getTitleAt(idx);
                JTextArea tmpWrite = convComponentsMap.get(tabName).getWriteArea();
                if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                    tmpWrite.append("\n");
                    return;
                }
                String text = tmpWrite.getText().trim();
                if (text.length() != 0) {

                    messageQueue.add(new TransferData(tabName, text, Constants.REGULAR_MSG));
                    WindowMethods.printNewMessage(appTabbs.getName(), text, LocalTime.now(),
                            convComponentsMap.get(tabName).getPrintArea());
                    tmpWrite.setText("");
                }
            }
        }
    }

    @Autowired
    public void setConvComponentsMap(Map<String, ConvComponents> convComponentsMap) {
        this.convComponentsMap = convComponentsMap;
    }

    @Autowired
    public void setMessageQueue(@Qualifier("messageQueue") BlockingQueue<TransferData> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Autowired
    public void setAppTabbsKey(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }
}
