package piotr.messenger.server.database.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import piotr.messenger.server.database.model.Conversation;

import java.util.Optional;

@Repository
public interface IConversationRepository extends MongoRepository<Conversation,String> {

    Optional<Conversation> findByFirstAndSecond(String login1, String login2);

}
