package piotr.messenger.client.service;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MessagePrinter {

    private MessagePrinter() {}

    public static void printMessage(String sender, String message, JTextArea printArea) {
        //display message in proper conversation tab
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        printArea.append(sender + ", " + dateFormat.format(calendar.getTime()) + "\n");
        printArea.append(message + "\n\n");
        printArea.setCaretPosition(printArea.getDocument().getLength());
    }

}
