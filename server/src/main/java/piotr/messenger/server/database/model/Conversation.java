package piotr.messenger.server.database.model;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Conversation {

    @Id
    @Getter
    private String id;

    private String first;
    private String second;

    public Conversation(String first, String second) {
        this.first = first;
        this.second = second;
    }
}
