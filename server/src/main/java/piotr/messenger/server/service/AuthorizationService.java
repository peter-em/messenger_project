package piotr.messenger.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.util.ConnectionParameters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

@Component
public class AuthorizationService {

    private final ClientsConnectionService connectionService;
    private final NewClientService newClientService;
    private final ConnectionParameters parameters;
    private final Selector selector;
    private final Logger logger;
    private boolean updateClietsUserList;

    public AuthorizationService(@Qualifier("mainSelector") Selector selector,
                                NewClientService newClientService,
                                ClientsConnectionService connectionService,
                                ConnectionParameters parameters) {
        this.selector = selector;
        this.newClientService = newClientService;
        this.connectionService = connectionService;
        this.parameters = parameters;
        logger = LoggerFactory.getLogger(AuthorizationService.class);
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
        if (!connectionService.isAuthenticated(clientSocket)) {


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
            if (response == 0) {
                readBuffer.putInt(parameters.getLastUsedPort());
            }

            readBuffer.flip();
            send(clientSocket, Arrays.copyOf(readBuffer.array(), readBuffer.limit()));
        }
    }

    public void writeClientData(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : connectionService.getClientBuffers(clientSocket)) {
            clientSocket.write(buff);
        }
    }

    private void send(SocketChannel client, byte[] data) {

        connectionService.addBufferToClient(client, ByteBuffer.wrap(data));
        SelectionKey selectionKey = client.keyFor(selector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
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

}
