package piotr.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.util.ConversationPair;
import piotr.messenger.server.util.UsersDatabase;

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
//        ConversationPair pair;
        String[] userData = (new String(readBuffer.array(), Constants.CHARSET)).split(";");

        //userData[0] holds info about type of message (ask for, refuse, confirm conversation)
        //userData[1] holds info about receiver of this request
        switch (userData[0]) {
            case Constants.C_ASK:
//				    logger.debug("ASK");
                if (usersDatabase.hasUser(userData[1])) {
                    SocketChannel askedChannel = usersDatabase.getChannel(userData[1]);

                    String askingUser = usersDatabase.getUser(clientRead);

                    ConversationPair convPair = new ConversationPair(clientRead, askedChannel);

                    readBuffer.clear();
                    if (!executor.hasPair(convPair)) {
                        //asked user available, no such conversation started
//                        pendingPairs.add(convPair);
                        executor.addPendingPair(convPair);
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
                ConversationPair pair = new ConversationPair(clientRead, usersDatabase.getChannel(userData[1]));

//                pendingPairs.remove(pair);
                executor.removePendingPair(pair);
                readBuffer.putInt(-30);
                readBuffer.put((userData[2] + ";").getBytes(Constants.CHARSET));
                readBuffer.flip();
                service.send(pair.getClient2(), readBuffer.array());
                break;

            case Constants.C_ACCEPT:
//				    logger.debug("ACCEPT");

                /*ConversationPair */pair = new ConversationPair(clientRead, usersDatabase.getChannel(userData[1]));
                if (pair.hasNullClient()) {
//                        logger.debug("ACCEPT - has null");
                    return;
                }

                if (executor.createNewWorker(pair)) {
                    readBuffer.clear();
                    readBuffer.putInt(-40);
                    readBuffer.put((parameters.getLastUsedPort() + ";" + parameters.getHostAddress() + ";"
                            + userData[1] + ";" + userData[2] + ";").getBytes(Constants.CHARSET));
                    readBuffer.flip();
                    service.send(pair.getClient1(), readBuffer.array());
                    service.send(pair.getClient2(), readBuffer.array());
                }
//                pair = new ConversationPair(clientRead, usersDatabase.getChannel(userData[1]));
//                if (pair.hasNullClient()) {
////                        logger.debug("ACCEPT - has null");
//                    return;
//                }
//                //conversation request accepted
//                pendingPairs.remove(pair);
//                activePairs.add(pair);
//
//                if (activeWorkersCounter < Constants.CONV_MAX) {
//
//                    //create new handler and send usersDatabase connection data
//                    //if limit of conversations was not reached
//                    Integer createPort = listenPort + 1;
//                    if (!handlersPorts.isEmpty()) {
//                        createPort = handlersPorts.get(handlersPorts.size() - 1) + 1;
//                    }
//                    handlersPorts.add(createPort);
//
//                    ConversationWorker worker = new ConversationWorker(hostName, createPort, handlersEndData, pair);
//                    activeWorkersCounter++;
//                    handlersExecutor.execute(worker);
//
//                    readBuffer.clear();
//                    readBuffer.putInt(-40);
//                    readBuffer.put((createPort + ";" + hostName + ";"
//                            + userData[1] + ";" + userData[2] + ";").getBytes(Constants.CHARSET));
//                    readBuffer.flip();
//                    send(pair.getClient1(), readBuffer.array());
//                    send(pair.getClient2(), readBuffer.array());
//
//                }
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
