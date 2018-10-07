package piotr.messenger.client.gui;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class PrintWriteAreas {

    private final @Getter Map<String, JTextArea> printAreas = new HashMap<>();
    private final @Getter Map<String, JTextArea> writeAreas = new HashMap<>();

}
