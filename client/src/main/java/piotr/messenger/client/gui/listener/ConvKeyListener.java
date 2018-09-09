package piotr.messenger.client.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.core.WorkerThread;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;


@Component
@Qualifier("convKeyListener")
public class ConvKeyListener extends KeyAdapter {

    private Map<String, JTextArea> writeAreas;
    private Map<String, JTextArea> printAreas;
    private ArrayBlockingQueue<String> mainDataQueue;
    private JTabbedPane appTabbs;


    @Autowired
    public void setPrintAreas(@Qualifier("printAreas") Map<String, JTextArea> printAreas) {
        this.printAreas = printAreas;
    }

    @Autowired
    public void setWriteAreas(@Qualifier("writeAreas") Map<String, JTextArea> writeAreas) {
        this.writeAreas = writeAreas;
    }

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<String> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

    @Autowired
    public void setAppTabbsKey(JTabbedPane appTabbs) {
        this.appTabbs = appTabbs;
    }

    // ---------- HANDLE KEY PRESSES ----------- //
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            e.consume();
            int idx = appTabbs.getSelectedIndex();
            if (idx > 0) {
                String tabName = appTabbs.getTitleAt(idx);
                JTextArea tmpWrite = writeAreas.get(tabName);
                if (tmpWrite.getText().length() != 0) {

                    if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                        tmpWrite.append("\n");
                        return;
                    }

                    mainDataQueue.add(tabName + ";" + tmpWrite.getText());
                    WorkerThread.printMessage("me", tabName, tmpWrite.getText(), printAreas.get(tabName));
                    tmpWrite.setText("");
                }
            }
        }
    }
}
