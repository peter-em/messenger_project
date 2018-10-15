package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.PrintWriteAreas;
import piotr.messenger.client.service.WindowMethods;
import piotr.messenger.client.util.TransferData;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


@Component
@Qualifier("convKeyListener")
public class ConvKeyListener extends KeyAdapter {

    private Map<String, JTextArea> writeAreas;
    private Map<String, JTextArea> printAreas;
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
                JTextArea tmpWrite = writeAreas.get(tabName);
                if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                    tmpWrite.append("\n");
                    return;
                }
                String text = tmpWrite.getText().trim();
                if (text.length() != 0) {

                    messageQueue.add(new TransferData(tabName, text));
                    WindowMethods.printMessage("me", text, printAreas.get(tabName));
                    tmpWrite.setText("");
                }
            }
        }
    }

    @Autowired
    public void setPrintAreas(PrintWriteAreas areas) {
        this.printAreas = areas.getPrintAreas();
    }

    @Autowired
    public void setWriteAreas(PrintWriteAreas areas) {
        this.writeAreas = areas.getWriteAreas();
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
