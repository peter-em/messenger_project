package piotr.messenger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.util.ConnectionParameters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;


@Component
@Slf4j
public class AuthorizationService {

    private final ClientsConnectionService connectionService;
    private final NewClientService newClientService;
    private final ConnectionParameters parameters;
    private boolean updateClietsUserList;

    public AuthorizationService(NewClientService newClientService,
                                ClientsConnectionService connectionService,
                                ConnectionParameters parameters) {
        this.newClientService = newClientService;
        this.connectionService = connectionService;
        this.parameters = parameters;
    }


    public void acceptClient(SocketChannel newClient, Selector selector) throws IOException {

        //accept incoming connection, set it in non-blocking mode
        newClient.configureBlocking(false);
        //register new client with selector, prepared for reading incoming data
        newClient.register(selector, SelectionKey.OP_READ);
        connectionService.addWritingClient(newClient);
    }

    public void readClientData(SocketChannel clientSocket, Selector selector) throws IOException {

        ByteBuffer readBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        int bytesRead;
        try {
            //try to read from client's channel
            bytesRead = clientSocket.read(readBuffer);
        } catch (IOException ioEx) {
            //exception caused by client disconnecting 'unproperly'
            bytesRead = -1;
        }

        if (bytesRead < 0) {
            // client ended connection, server closes corresponding channel
            // update other connected users if this client has been already authenticated
            if (connectionService.removeClient(clientSocket)) {
                updateClietsUserList = true;
            }
            return;
        }

        readBuffer.flip();
        if (!connectionService.isAuthenticated(clientSocket)) {

            // send client a response to veryfication/registration request
            // success (0) or failure (-1 when verification failed, -2 if such client has already logged in)
            int response = newClientService.handleData(readBuffer, clientSocket);
            if (response == -4) {
                log.info("Invalid application connected, dropping it from server.");
                connectionService.removeClient(clientSocket);
                return;
            }
            readBuffer.clear();
            readBuffer.putInt(response);
            if (response == 0) {
                updateClietsUserList = true;
                readBuffer.putInt(parameters.getLastUsedPort());
            }

            readBuffer.flip();
            send(selector, clientSocket, Arrays.copyOf(readBuffer.array(), readBuffer.limit()));
        }
    }

    public void writeClientData(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : connectionService.getClientBuffers(clientSocket)) {
            clientSocket.write(buff);
        }
    }

    public void updateClients(Selector selector) {
        if (updateClietsUserList) {
            // update all authenticated clients
            // with logins of active users
            ByteBuffer buffer = connectionService.prepareUserList();
            buffer.flip();
            connectionService.getChannels().forEach(channel -> send(selector, channel, buffer.array()));
            updateClietsUserList = false;
        }
    }

    private void send(Selector selector, SocketChannel client, byte[] data) {

        connectionService.addBufferToClient(client, ByteBuffer.wrap(data));
        client.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
    }
}
