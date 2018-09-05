package piotr.messenger.springclient.gui.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.core.WorkerThread;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;


@Component
public class ConvKeyListener extends KeyAdapter {

    private WorkerThread worker;
    private Map<String, JTextArea> writeAreas;
    private ArrayBlockingQueue<String> mainDataQueue;
    private JTabbedPane appTabbs;

    @Autowired
    public void setWorker(WorkerThread worker) {
        this.worker = worker;
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
    public void setAppTabbs(JTabbedPane appTabbs) {
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
//                JTextArea tmpWrite = worker.getWriteAreas().get(tabName);
                JTextArea tmpWrite = writeAreas.get(tabName);
                if (tmpWrite.getText().length() != 0) {

                    if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                        tmpWrite.append("\n");
                        return;
                    }

//                    mainDataQueue.add(tabName + ";" + tmpWrite.getText());
                    mainDataQueue.add(tabName + ";" + tmpWrite.getToolTipText());
                    worker.printMessage("me", tabName, tmpWrite.getText());
                    tmpWrite.setText("");
                }
            }
        }
    }
}
