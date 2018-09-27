package piotr.messenger.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Component
public class DataFlowService {

    private ClientsConnectionService connectionService;
    private NewClientService newClientService;
    private ConversationService conversationService;
    private ConversationsExecutor executor;
    private Selector selector;
    private Logger logger;
    private boolean updateClietsUserList;

    public DataFlowService(Selector selector) {
        this.selector = selector;
    }

    @PostConstruct
    private void initServices() {
        conversationService.setDataFlowService(this);
        logger = LoggerFactory.getLogger(DataFlowService.class);
    }

    public void acceptClient(ServerSocketChannel serverSocket) throws IOException {

        //accept incoming connection, set it in non-blocking mode
        SocketChannel newClient = serverSocket.accept();
        newClient.configureBlocking(false);
        //register new client with selector, prepared for reading incoming data
        newClient.register(selector, SelectionKey.OP_READ);
        connectionService.addWritingClient(newClient);
    }

    public void readClientData(SocketChannel clientSocket) throws IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        int bytesRead = -1;
        try {
            //try to read from client's channel
            bytesRead = clientSocket.read(readBuffer);
        } catch (IOException ioEx) {
            //exception caused by client disconnecting 'unproperly'
            logger.info("Unexpected end of connection ({}).", ioEx.getMessage());
        }

        if (bytesRead <= 0) {
            // client ended connection, server closes corresponding channel
            // update other connected users if this client has been already authenticated
            if (connectionService.removeClient(clientSocket)) {
                toggleUpdateClients();
            }
            return;
        }

        readBuffer.flip();
        if (connectionService.isAuthenticated(clientSocket)) {
            conversationService.handleData(readBuffer, clientSocket);
        } else {
            // send client a response to veryfication/registration request
            // success (0) or failure (-1 when verification failed, -2 if such client has already logged in)
            int response = newClientService.handleData(readBuffer, clientSocket);
            if (response == 0) {
                toggleUpdateClients();
            } else if (response == -4) {
                logger.info("Invalid application connected, dropping it from server.");
                connectionService.removeClient(clientSocket);
                return;
            }
            readBuffer.clear();
            readBuffer.putInt(response);
            readBuffer.flip();
            send(clientSocket, readBuffer.array());
        }
    }

    public void writeClientData(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : connectionService.getClientBuffers(clientSocket)) {
            clientSocket.write(buff);
        }
    }

    public void send(SocketChannel client, byte[] data) {

        connectionService.addBufferToClient(client, ByteBuffer.wrap(data));
        SelectionKey selectionKey = client.keyFor(selector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
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
        // update all authenticated clients
        // with logins of active users
        ByteBuffer buffer = connectionService.prepareUserList();
        buffer.flip();
        connectionService.getChannels().forEach(channel -> send(channel, buffer.array()));
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
    public void setConnectionService(ClientsConnectionService connectionService) {
        this.connectionService = connectionService;
    }
}
