package piotr.messenger.server.database.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Conversation {

    @Id
    @Getter
    private String id;

    @Getter
    private final String first;
    @Getter
    private final String second;

    public Conversation(String first, String second) {
        this.first = first;
        this.second = second;
    }
}
