package piotr.messenger.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.util.ClientState;
import piotr.messenger.server.util.UsersDatabase;

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
    private Map<SocketChannel, ClientState> clientsState;
    private boolean updateClietsUserList;
    private UsersDatabase usersDatabase;
    //mapowanie socketChannel clienta do listy instancji ByteBufforow
    private Map<SocketChannel, List<ByteBuffer>> pendingData;
    private Selector selector;
    private ConversationsExecutor executor;
    private Logger logger;

    public DataFlowService(Selector selector) {
        this.selector = selector;
        clientsState = new HashMap<>();
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

        logger.debug("dropRegistered - {}", usersDatabase.getUser(client));
        usersDatabase.dropUser(client);
        updateClietsUserList = true;

    }

    public void acceptClient(ServerSocketChannel serverSocket) throws IOException {

        //accept incoming connection, set it in non-blocking mode
        SocketChannel newClient = serverSocket.accept();
        newClient.configureBlocking(false);
        //register new client with selector, prepared for reading incoming data
        newClient.register(selector, SelectionKey.OP_READ);
        clientsState.put(newClient, new ClientState());
        pendingData.put(newClient, new LinkedList<>());

    }

    public void readClientData(SocketChannel clientSocket) throws IOException {

        ClientState state = clientsState.get(clientSocket);

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
            if (state.getState() > ClientState.WAITFORUID) {
                dropRegisteredClient(clientSocket);
            }
            clientsState.remove(clientSocket);
//            key.cancel();
            clientSocket.close();
            logger.debug("client dropping");
            readBuffer.clear();
            return;
        }

        readBuffer.flip();

        if (state.getState() == ClientState.WAITFORUID) {
            if (newClientService.handleData(readBuffer, clientSocket)) {
                state.setState(ClientState.SERVECLIENT);
            }
        } else if (state.getState() == ClientState.SERVECLIENT) {
            conversationService.handleData(readBuffer, clientSocket);
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

    public void terminaterHandlers() {
//        handlersExecutor.shutdown();
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
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        StringBuilder usersList = new StringBuilder();
        for (String str : usersDatabase.getUsers()) {
            usersList.append(str);
            usersList.append(';');
        }
        buffer.putInt(usersDatabase.usersCount());
        buffer.put(usersList.toString().getBytes(Constants.CHARSET));

        //update all connected usersDatabase with information
        //about quantity and logins of active users
        buffer.flip();
        for (SocketChannel clnt : usersDatabase.getChannels()) {
            send(clnt, buffer.array());
        }

        updateClietsUserList = false;
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
