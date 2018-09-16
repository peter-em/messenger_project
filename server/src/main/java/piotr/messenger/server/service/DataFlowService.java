package piotr.messenger.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.database.UsersDatabase;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class DataFlowService {

    private ConversationService conversationService;
    private NewClientService newClientService;
    private Map<SocketChannel, Boolean> clientsAuthenticationMap;
    private boolean updateClietsUserList;
    private UsersDatabase usersDatabase;
    // mapping socketChannels to List of ByteBuffers
    private Map<SocketChannel, List<ByteBuffer>> pendingData;
    private Selector selector;
    private ConversationsExecutor executor;
    private Logger logger;

    public DataFlowService(Selector selector) {
        this.selector = selector;
        clientsAuthenticationMap = new HashMap<>();
        pendingData = new HashMap<>();
    }

    @PostConstruct
    private void initServices() {
        conversationService.setDataFlowService(this);
        newClientService.setDataFlowService(this);
        logger = LoggerFactory.getLogger(DataFlowService.class);
    }

    public void send(SocketChannel client, byte[] data) {

        pendingData.get(client).add(ByteBuffer.wrap(data));

        SelectionKey selectionKey = client.keyFor(selector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void dropRegisteredClient(SocketChannel client) {

        usersDatabase.dropUser(client);
        clientsAuthenticationMap.remove(client);
        updateClietsUserList = true;

    }

    public void acceptClient(ServerSocketChannel serverSocket) throws IOException {

        //accept incoming connection, set it in non-blocking mode
        SocketChannel newClient = serverSocket.accept();
        newClient.configureBlocking(false);
        //register new client with selector, prepared for reading incoming data
        newClient.register(selector, SelectionKey.OP_READ);
        clientsAuthenticationMap.put(newClient, false);
        pendingData.put(newClient, new LinkedList<>());

    }

    private boolean isAuthenticated(SocketChannel client) {
        return clientsAuthenticationMap.get(client);
    }

    public void readClientData(SocketChannel clientSocket) throws IOException {

        //create read buffer for incoming data
        ByteBuffer readBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        int bytesRead = -1;
        //try to read from client's channel
        try {
            bytesRead = clientSocket.read(readBuffer);
        } catch (IOException ioEx) {
            //exception caused by client disconnecting 'unproperly'
            //cancel 'SelectionKey key' and close corresponding channel
            logger.info("Unexpected end of connection ({}).", ioEx.getMessage());
        }

        if (bytesRead <= 0) {
            //client ended connection clearly, server closes corresponding channel
            if (isAuthenticated(clientSocket)) {
                dropRegisteredClient(clientSocket);
            }
            clientsAuthenticationMap.remove(clientSocket);
//            key.cancel();
            clientSocket.close();
            logger.debug("client dropping");
            readBuffer.clear();
            return;
        }

//        readBuffer.flip();
        if (isAuthenticated(clientSocket)) {
            conversationService.handleData(readBuffer, clientSocket);
        } else {
            if (newClientService.handleData(readBuffer, clientSocket)) {
                clientsAuthenticationMap.put(clientSocket, true);
                toggleUpdateClients();
            }
        }
    }

    public void writeClientData(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : pendingData.get(clientSocket)) {
            clientSocket.write(buff);
        }
        pendingData.get(clientSocket).clear();


//        if (buffer.remaining() > 0) {
//            logger.error("!!! buffer was not fully emptied !!!");
//        }


    }

    public void cleanupClosedConversations() {
        //clear leftovers from terminated conversation handler
        try {
            executor.cleanAfterWorker();
        } catch (InterruptedException abqEx) {
            logger.error("ArrayBlockingQueue error - " + abqEx.getMessage());
        }
    }

    public void terminateHandlers() {
        executor.terminateExecutor();
    }

    public void toggleUpdateClients() {
        updateClietsUserList = true;
    }

    public void updateClients() {
        if (updateClietsUserList) {
            sendUserListToClients();
        }
    }

    private void sendUserListToClients() {
        logger.info("sendUserList");
        ByteBuffer buffer = prepareUserList();

        //update all connected usersDatabase with information
        //about quantity and logins of active users
        buffer.flip();



        clientsAuthenticationMap.forEach((key, value) -> {
            logger.info("value: {}", value);
            if (value) send(key, buffer.array());
        });
//        for (SocketChannel clnt : usersDatabase.getChannels()) {
//            send(clnt, buffer.array());
//        }
        updateClietsUserList = false;
    }

    private ByteBuffer prepareUserList() {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

        buffer.putInt(usersDatabase.getUsers().size());
        usersDatabase.getUsers().forEach(user -> {
            buffer.putInt(user.length());
            buffer.put(user.getBytes(Constants.CHARSET));
        });
        return buffer;
    }

    @Autowired
    public void setExecutor(ConversationsExecutor executor) {
        this.executor = executor;
    }

    @Autowired
    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Autowired
    public void setNewClientService(NewClientService newClientService) {
        this.newClientService = newClientService;
    }

    @Autowired
    public void setUsersDatabase(UsersDatabase usersDatabase) {
        this.usersDatabase = usersDatabase;
    }
}
