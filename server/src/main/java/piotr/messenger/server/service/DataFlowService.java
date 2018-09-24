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
    private boolean updateClietsUserList;
    private UsersDatabase usersDatabase;
    // mapping socketChannels to List of ByteBuffers
    private Map<SocketChannel, List<ByteBuffer>> pendingData;
    private Selector selector;
    private ConversationsExecutor executor;
    private Logger logger;

    public DataFlowService(Selector selector) {
        this.selector = selector;
        pendingData = new HashMap<>();
    }

    @PostConstruct
    private void initServices() {
        conversationService.setDataFlowService(this);
        logger = LoggerFactory.getLogger(DataFlowService.class);
    }

    public void send(SocketChannel client, byte[] data) {

        pendingData.get(client).add(ByteBuffer.wrap(data));

        SelectionKey selectionKey = client.keyFor(selector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void dropRegisteredClient(SocketChannel client) {

        if (isAuthenticated(client)) {
            usersDatabase.dropUser(client);
            updateClietsUserList = true;
        }
    }

    public void acceptClient(ServerSocketChannel serverSocket) throws IOException {

        //accept incoming connection, set it in non-blocking mode
        SocketChannel newClient = serverSocket.accept();
        newClient.configureBlocking(false);
        //register new client with selector, prepared for reading incoming data
        newClient.register(selector, SelectionKey.OP_READ);
        pendingData.put(newClient, new LinkedList<>());
    }

    private boolean isAuthenticated(SocketChannel client) {
        return usersDatabase.hasUser(client);
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
            logger.info("Unexpected end of connection ({}).", ioEx.getMessage());
        }

        if (bytesRead <= 0) {
            //client ended connection, server closes corresponding channel
            dropRegisteredClient(clientSocket);
            clientSocket.close();
            return;
        }

        readBuffer.flip();
        if (isAuthenticated(clientSocket)) {
            conversationService.handleData(readBuffer, clientSocket);
        } else {
            // send client a response to veryfication/registration request
            // success (0) or failure (-1 when verification failed, -2 if such client has already logged in)
            int response = newClientService.handleData(readBuffer, clientSocket);

            if (response == 0) {
                toggleUpdateClients();
            }
            readBuffer.clear();
            readBuffer.putInt(response);
            readBuffer.flip();
            send(clientSocket, readBuffer.array());
        }
    }

    public void writeClientData(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : pendingData.get(clientSocket)) {
            clientSocket.write(buff);
        }
        pendingData.get(clientSocket).clear();

    }

    public void cleanupClosedConversations() throws InterruptedException {
        //clear leftovers from terminated conversation handler
            executor.cleanAfterWorker();
    }

    public void terminateHandlers() {
        executor.terminateExecutor();
    }

    private void toggleUpdateClients() {
        updateClietsUserList = true;
    }

    public void updateClients() {
        if (updateClietsUserList) {
            sendUserListToClients();
        }
    }

    private void sendUserListToClients() {

        //update all connected usersDatabase with information
        //about quantity and logins of active users

        ByteBuffer buffer = prepareUserList();
        buffer.flip();
        usersDatabase.getChannels().forEach(channel -> send(channel, buffer.array()));
        updateClietsUserList = false;
    }

    private ByteBuffer prepareUserList() {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

        buffer.putInt(usersDatabase.connectedSize());
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
