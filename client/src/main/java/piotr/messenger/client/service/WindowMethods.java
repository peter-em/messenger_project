package piotr.messenger.client.service;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class WindowMethods {

    private WindowMethods() {}

    public static void printNewMessage(String sender, String content, LocalTime time, JTextArea printArea) {
        //display new message in proper conversation tab
        printArea.append(buildMessage(sender, content, time));
        printArea.setCaretPosition(printArea.getDocument().getLength());
    }

    public static void printArchivedMessage(String sender, String content, LocalTime time, JTextArea printArea) {
        //display archived message in proper conversation tab
        printArea.insert(buildMessage(sender, content, time), 0);
    }

    private static String buildMessage(String sender, String content, LocalTime time) {
        return sender + ", " + time.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" + content + "\n\n";

    }

    //centering window (JFrame) on the screen
    public static void centerWindow(JFrame window) {
        int width = window.getContentPane().getMinimumSize().width;
        int height = window.getContentPane().getMinimumSize().height;
        GraphicsEnvironment gE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point point = gE.getCenterPoint();
        window.setLocation(point.x - width/2, point.y - height);
    }
}
