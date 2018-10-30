package piotr.messenger.server.database.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Message {

    @Id
    private String id;

//    private String convId;

    private String author;
    private LocalDateTime time;
    private String content;
}
