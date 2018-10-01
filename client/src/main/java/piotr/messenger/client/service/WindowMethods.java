package piotr.messenger.client.service;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class WindowMethods {

    private WindowMethods() {}

    public static void printMessage(String sender, String message, JTextArea printArea) {
        //display message in proper conversation tab
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        printArea.append(sender + ", " + dateFormat.format(calendar.getTime()) + "\n");
        printArea.append(message + "\n\n");
        printArea.setCaretPosition(printArea.getDocument().getLength());
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
