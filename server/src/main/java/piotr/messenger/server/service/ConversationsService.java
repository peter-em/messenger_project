package piotr.messenger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.server.database.MessagesDatabase;
import piotr.messenger.server.database.UsersDatabase;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

@Component
@Slf4j
public class ConversationsService {

//    @Resource private UsersDatabase usersDB;
    @Resource private MessagesDatabase messagesDB;
    @Resource private ClientsConnectionService connections;
    private Map<SocketChannel, Boolean> hasIntroduced = new HashMap<>();
    private Map<String, SocketChannel> channelMap = new HashMap<>();
    private final Map<SocketChannel, List<ByteBuffer>> writingBuffers = new HashMap<>();
    private ByteBuffer buffer;

    @PostConstruct
    private void init() {
        buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    }


    public void acceptUser(ServerSocketChannel handler, Selector convSelector) throws IOException {

        //accept connection, set it to non-blocking mode
        SocketChannel newClient = handler.accept();
        newClient.configureBlocking(false);

        newClient.register(convSelector, SelectionKey.OP_READ);
        hasIntroduced.put(newClient, false);
    }

    public void read(SocketChannel channel, Selector convSelector) throws IOException {

        buffer.clear();
        int bytesRead = -1;
        try {
            //try to read from client's channel
            bytesRead = channel.read(buffer);
        } catch (IOException ioEx) {
            //exception caused by client disconnecting 'unproperly'
            log.info("End of connection ({}).", ioEx.getMessage());
        }

        if (bytesRead <= 0) {
            // client ended connection
            removeChannel(channel);
            return;
        }

        buffer.flip();
        if (hasIntroduced.get(channel)) {
            handleMessage(channel, convSelector);
            return;
        }

        List<String> list = readLogins(1);
        String login = list.isEmpty()?null:list.get(0);
        if (connections.isAuthenticated(login) || !channelMap.containsKey(login)) {
            channelMap.put(login, channel);
            hasIntroduced.put(channel, true);
            writingBuffers.put(channel, new LinkedList<>());
        } else {
            removeChannel(channel);
        }
    }

    public void write(SocketChannel clientSocket) throws IOException {

        for (ByteBuffer buff : writingBuffers.get(clientSocket)) {
            clientSocket.write(buff);
        }
        writingBuffers.get(clientSocket).clear();
    }

    private void removeMappings(SocketChannel channel) {
        hasIntroduced.remove(channel);
        channelMap.values().remove(channel);
        writingBuffers.remove(channel);
        log.info("hasIntroduced.size: {}", hasIntroduced.size());
        log.info("channelMap.size: {}", channelMap.size());
    }

    private void handleMessage(SocketChannel channel, Selector convSelector) throws IOException {
        List<String> list = readLogins(2);
        if (list.isEmpty()) {
            removeChannel(channel);
            return;
        }
        // non empty list contains elements:
        // 0 - sender of a message
        // 1 - receiver of a message
        SocketChannel receiver = channelMap.get(list.get(1));

        if (receiver != null) {

            byte[] data = Arrays.copyOf(buffer.array(), buffer.limit());
            archiveMessage(list.get(0), list.get(1));

            writingBuffers.get(receiver).add(ByteBuffer.wrap(data));
            SelectionKey selectionKey = receiver.keyFor(convSelector);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private List<String> readLogins(int size) {
        try {
            return ClientDataConverter.decodeBufferToList(size, buffer);
        } catch (BufferUnderflowException | IndexOutOfBoundsException ex) {
            return Collections.emptyList();
        }
    }

    private void archiveMessage(String login1, String login2) {
        String content = new String(Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit()));
        messagesDB.addMessage(login1, login2, content);
    }

    // handler closes corresponding channel
    // remove client mappings
    private void removeChannel(SocketChannel channel) throws IOException {
        channel.close();
        removeMappings(channel);
    }

}
