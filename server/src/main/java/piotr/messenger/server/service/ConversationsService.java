package piotr.messenger.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.server.database.MessagesDatabase;
import piotr.messenger.server.database.model.Message;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ConversationsService {

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

        int messageMode = buffer.getInt();

        // non empty list contains elements:
        // 0 - sender of a message
        // 1 - receiver of a message
        List<String> logins = readLogins(2);
        if (logins.isEmpty()) {
            removeChannel(channel);
            return;
        }

        String content = new String(Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit()));
        buffer.clear();

        SocketChannel destination;
        List<Message> messages;
        if (messageMode == Constants.REGULAR_MSG) {
            destination = channelMap.get(logins.get(1));

            messages = messagesDB.archiveMessage(logins.get(0), logins.get(1), content);
            logins.set(1, "");
            if (destination == null)
                return;

        } else {
            destination = channel;
            messages = messagesDB.getArchivedMessages(logins.get(0), logins.get(1), LocalDateTime.parse(content));
        }

        ByteBuffer sendBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        sendBuffer.putInt(messageMode);     // insert message type
        sendBuffer.putInt(messages.size()); // insert number of messages in buffer
        sendBuffer.putInt(logins.get(1).length());                  // insert size and login of conv partner
        sendBuffer.put(logins.get(1).getBytes(Constants.CHARSET));  // (for archived messages request)
        for (Message message : messages) {
            writeMessageToBuffer(message, sendBuffer);
        }

        sendBuffer.flip();
        writingBuffers.get(destination).add(sendBuffer);
        SelectionKey selectionKey = destination.keyFor(convSelector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }


    private void writeMessageToBuffer(Message message, ByteBuffer buffer) {
        byte[] data = message.getAuthor().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.putInt(Constants.DATE_LENGTH);
        buffer.put(message.getTime().toString().getBytes(Constants.CHARSET));
        data = message.getContent().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
    }

    // TODO clients validation (sender and receiver) eventually

    private List<String> readLogins(int size) {
        try {
            return ClientDataConverter.decodeBufferToList(size, buffer);
        } catch (BufferUnderflowException | IndexOutOfBoundsException ex) {
            return Collections.emptyList();
        }
    }

    // handler closes corresponding channel
    // remove client mappings
    private void removeChannel(SocketChannel channel) throws IOException {
        channel.close();
        removeMappings(channel);
    }

}
