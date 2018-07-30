package piotr.messenger.client;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class WriterListener implements KeyListener {

    private JTabbedPane appPages;
    private Map<String, JTextArea> writeAreas;
    private static ArrayBlockingQueue<String> mainDataQueue;

    WriterListener(JTabbedPane pane, Map<String, JTextArea> areas, ArrayBlockingQueue<String> queue) {
        appPages = pane;
        writeAreas = areas;
        mainDataQueue = queue;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            e.consume();
            int idx = appPages.getSelectedIndex();
            if (idx > 0) {
                //System.out.println("Indeks: " + idx);
                String tabName = appPages.getTitleAt(idx);
                JTextArea tmpWrite = writeAreas.get(tabName);
                if (tmpWrite.getText().length() != 0) {

                    if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                        tmpWrite.append("\n");
                        return;
                    }

                    try {
                        mainDataQueue.put("s;" + tabName + ";" + tmpWrite.getText() + ";");
                        tmpWrite.setText("");
                    } catch (InterruptedException itrEx) {
                        System.err.println(itrEx.getMessage());
                    }

                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

}
