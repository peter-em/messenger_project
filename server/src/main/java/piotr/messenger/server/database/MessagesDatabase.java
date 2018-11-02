package piotr.messenger.server.database;

import org.springframework.stereotype.Component;
import piotr.messenger.server.database.model.Message;
import piotr.messenger.server.database.service.MessageService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class MessagesDatabase {

    @Resource
    private MessageService service;

    public List<Message> archiveMessage(String sender, String receiver, String content) {

        LocalDateTime time = LocalDateTime.now();
        Message message = Message.builder().author(sender).time(time).content(content).build();
        addMessage(sender, receiver, message);
        return Collections.singletonList(message);
    }

    public List<Message> getArchivedMessages(String sender, String receiver, LocalDateTime oldestMessage) {
        String convId = service.getConversationId(sender, receiver);

        return service.readArchivedMessages(convId, oldestMessage);
    }

    private void addMessage(String sender, String receiver, Message message) {

        String convId = service.getConversationId(sender, receiver);
        service.insertMessage(convId, message);
    }

}
