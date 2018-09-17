package piotr.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.util.ConversationPair;
import piotr.messenger.server.database.UsersDatabase;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


@Component
public class ConversationService {

    private ConversationsExecutor executor;
    private UsersDatabase usersDatabase;
    private ConnectionParameters parameters;
    private DataFlowService service;

    public void setDataFlowService(DataFlowService service) {
        this.service = service;
    }

    public void handleData(ByteBuffer readBuffer, SocketChannel clientRead) {

        //Veryfied client tries to create new conversation
        String[] userData = (new String(readBuffer.array(), Constants.CHARSET)).split(";");
        readBuffer.clear();
        //userData[0] holds info about type of message (ask for, refuse, confirm conversation)
        //userData[1] holds info about receiver of this request
        //userData[2] holds login of request sender
        ConversationPair pair;
        switch (userData[0]) {
            case Constants.C_ASK:
//				    logger.debug("ASK");
                if (usersDatabase.hasUser(userData[1])) {
                    SocketChannel askedChannel = usersDatabase.getChannel(userData[1]);

                    String askingUser = userData[2];

                    pair = new ConversationPair(clientRead, askedChannel);

                    if (!executor.hasPair(pair)) {
                        //asked user available, no such conversation started
//                        pendingPairs.add(convPair);
                        executor.addPendingPair(pair);
                        readBuffer.putInt(-10);
                        readBuffer.put((askingUser.concat(";")).getBytes(Constants.CHARSET));
                        readBuffer.flip();
                        //forward this request to asked client
                        service.send(askedChannel, readBuffer.array());
                    } else {
                        //conversation between this users has already started
                        readBuffer.putInt(-20);
                        readBuffer.put((userData[1].concat(";")).getBytes(Constants.CHARSET));
                        readBuffer.flip();
                        //send response about active conv
                        service.send(clientRead, readBuffer.array());
                    }
                }
                break;

            case Constants.C_REFUSE:
//				    logger.debug("REFUSE");
                //user refused, inform client sending request
                pair = new ConversationPair(clientRead, usersDatabase.getChannel(userData[1]));

//                pendingPairs.remove(pair);
                if (pair.hasNullClient()) {
//                        logger.debug("REFUSE - has null");
                    return;
                }
                executor.removePendingPair(pair);
                readBuffer.putInt(-30);
                readBuffer.put((userData[2] + ";").getBytes(Constants.CHARSET));
                readBuffer.flip();
                service.send(pair.getClient2(), readBuffer.array());
                break;

            case Constants.C_ACCEPT:
//				    logger.debug("ACCEPT");

                pair = new ConversationPair(clientRead, usersDatabase.getChannel(userData[1]));
                if (pair.hasNullClient()) {
//                        logger.debug("ACCEPT - has null");
                    return;
                }

                if (executor.createNewWorker(pair)) {

                    readBuffer.putInt(-40);
                    readBuffer.put((parameters.getLastUsedPort() + ";" + parameters.getHostAddress() + ";"
                            + userData[1] + ";" + userData[2] + ";").getBytes(Constants.CHARSET));
                    readBuffer.flip();
                    service.send(pair.getClient1(), readBuffer.array());
                    service.send(pair.getClient2(), readBuffer.array());
                }
        }

    }

    @Autowired
    public void setExecutor(ConversationsExecutor executor) {
        this.executor = executor;
    }

    @Autowired
    public void setUsersDatabase(UsersDatabase usersDatabase) {
        this.usersDatabase = usersDatabase;
    }

    @Autowired
    public void setParameters(ConnectionParameters parameters) {
        this.parameters = parameters;
    }
}
