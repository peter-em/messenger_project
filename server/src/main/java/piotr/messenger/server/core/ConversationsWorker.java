package piotr.messenger.server.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.server.service.ClientsConnectionService;
import piotr.messenger.server.util.ConnectionParameters;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;


@Component
@Slf4j
public class ConversationsWorker implements Runnable {

    @Resource private ClientsConnectionService connections;
    @Resource private ConnectionParameters parameters;
    private volatile boolean isRunning;
    private String handlerAddress;
    private int handlerPort;
    private Map<SocketChannel, Boolean> hasIntroduced = new HashMap<>();
    private Map<String, SocketChannel> channelMap = new HashMap<>();
    private final Map<SocketChannel, List<ByteBuffer>> writingBuffers = new HashMap<>();
    private ByteBuffer buffer;


    @PostConstruct
    private void init() {
        handlerAddress = parameters.getHostAddress();
        handlerPort = parameters.getWorkerPort();
        System.setProperty("sun.net.useExclusiveBind", "false");
        buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
    }

    @Override
    public void run() {

        Thread.currentThread().setName("Worker (" + handlerPort + ")");

        try (ServerSocketChannel handler = ServerSocketChannel.open();
             Selector convSelector = SelectorProvider.provider().openSelector()) {

            openSocket(handler, convSelector);
            log.info("Worker started");

            while (isRunning) {

                convSelector.select();

                SelectionKey key;
                Iterator<SelectionKey> selectedKeys;
                selectedKeys = convSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                    } else if (key.isAcceptable()) {
                        acceptUser(handler, convSelector);

                    } else if (key.isReadable()) {
                        read((SocketChannel) key.channel(), convSelector);

                    } else if (key.isWritable()) {
                        write((SocketChannel) key.channel());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException ioEx) {
            log.error(ioEx.getMessage());
        }
        log.info("Closing worker");

    }

    private void openSocket(ServerSocketChannel handler, Selector convSelector) throws IOException {

        log.error("port: {}", handlerPort);
        handler.configureBlocking(false);
        handler.socket().bind(new InetSocketAddress(handlerAddress, handlerPort));
        handler.register(convSelector, SelectionKey.OP_ACCEPT);
        isRunning = true;
    }

    private void acceptUser(ServerSocketChannel handler, Selector convSelector) throws IOException {

        //accept connection, set it to non-blocking mode
        SocketChannel newClient = handler.accept();
        newClient.configureBlocking(false);

        newClient.register(convSelector, SelectionKey.OP_READ);
        hasIntroduced.put(newClient, false);
    }

    private void read(SocketChannel channel, Selector convSelector) throws IOException {

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

    private void write(SocketChannel clientSocket) throws IOException {

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

    // handler closes corresponding channel
    // remove client mappings
    private void removeChannel(SocketChannel channel) throws IOException {
        channel.close();
        removeMappings(channel);
    }

    boolean isRunning() {
        return isRunning;
    }

    void stopWorker() {
        isRunning = false;
    }
}