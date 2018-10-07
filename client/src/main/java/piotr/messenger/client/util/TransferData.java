package piotr.messenger.client.util;

import lombok.Getter;

public class TransferData {

    private final @Getter String type;
    private final @Getter String content;

    public TransferData(String type, String content) {
        this.type = type;
        this.content = content;
    }
}
