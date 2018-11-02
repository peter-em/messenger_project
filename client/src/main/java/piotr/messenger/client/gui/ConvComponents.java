package piotr.messenger.client.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.time.LocalDateTime;

//@Component
@AllArgsConstructor
public class ConvComponents {

    private final @Getter JTextArea printArea;
    private final @Getter JTextArea writeArea;
    private @Getter @Setter LocalDateTime oldestMessage;
//    private boolean isMoreMessages;

}
