package piotr.messenger.client.util;

import lombok.Getter;
import lombok.Setter;

public class TransferData {

    private final @Getter String type;
    private final @Getter String content;
    private @Getter @Setter int msgMode;

    public TransferData(String type, String content, int msgMode) {
        this.type = type;
        this.content = content;
        this.msgMode = msgMode;
    }
}
