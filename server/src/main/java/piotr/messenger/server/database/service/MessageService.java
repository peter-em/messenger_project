package piotr.messenger.server.database.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piotr.messenger.library.Constants;
import piotr.messenger.server.database.model.Conversation;
import piotr.messenger.server.database.model.Message;
import piotr.messenger.server.database.repository.IConversationRepository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
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

    public void insertMessage(String convId, Message message) {

        msgRepo.save(message, convId);
    }

    public List<Message> readArchivedMessages(String convId, LocalDateTime time) {
        if (!msgRepo.collectionExists(convId))
            return Collections.emptyList();

        Query query = new Query(Criteria.where("time").lt(time));
        long count = msgRepo.count(query, convId);
        query.skip(count - Constants.ARCHIVED_COUNT);

        List<Message> list = msgRepo.find(query, Message.class, convId);
        Collections.reverse(list);
        return list;
    }

}
