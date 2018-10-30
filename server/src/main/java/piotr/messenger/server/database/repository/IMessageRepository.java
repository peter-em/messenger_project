package piotr.messenger.server.database.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import piotr.messenger.server.database.model.Message;

@Repository
public interface IMessageRepository extends MongoRepository<Message,String> {
}
