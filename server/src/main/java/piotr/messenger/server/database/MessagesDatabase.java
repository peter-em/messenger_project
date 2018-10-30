package piotr.messenger.server.database;

import org.springframework.stereotype.Component;
import piotr.messenger.server.database.service.MessageService;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class MessagesDatabase {

    @Resource
    private MessageService service;

    public void addMessage(String sender, String receiver, String content) {

        String convId = service.getConversationId(sender, receiver);
        service.insertMessage(convId, sender, LocalDateTime.now(), content);

    }

}
