package piotr.messenger.server.database.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import piotr.messenger.server.database.model.Conversation;
import piotr.messenger.server.database.model.Message;
import piotr.messenger.server.database.repository.IConversationRepository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class MessageService {

    @Resource
    private IConversationRepository convRepo;

    @Resource
    private MongoTemplate msgRepo;

    public String getConversationId(String login1, String login2) {
        SortedSet<String> clients = new TreeSet<>();
        clients.add(login1);
        clients.add(login2);

        Conversation conversation = convRepo.findByFirstAndSecond(clients.first(), clients.last())
                .orElse(new Conversation(clients.first(), clients.last()));
        if (conversation.getId() == null) {
            conversation = convRepo.save(conversation);
        }
        return conversation.getId();
    }

    public String insertMessage(String convId, String author,
                             LocalDateTime time, String content) {
        Message message = Message.builder().author(author)
                .time(time).content(content).build();

        msgRepo.save(message, convId);
        return message.getId();
    }

//    TODO implement reading messages from collection
//    TO GET LAST MESSAGE FROM COLLECTION
//    Document msg = msgRepo.getCollection(convId).find().sort(new BasicDBObject("_id", -1)).limit(1).first();
}
