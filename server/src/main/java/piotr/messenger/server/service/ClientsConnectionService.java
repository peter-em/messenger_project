package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

@Component
public class ClientsConnectionService {

    final Map<String, SocketChannel> connectedClients = new HashMap<>();
    final Map<SocketChannel, List<ByteBuffer>> writingBuffers = new HashMap<>();


    public boolean isAuthenticated(String login) {
        return null != connectedClients.get(login);
    }

    public void addUser(String login, SocketChannel channel) {
        connectedClients.put(login, channel);
    }

    public Collection<SocketChannel> getChannels() {
        return connectedClients.values();
    }

    public SocketChannel getChannel(String login) {
        return connectedClients.get(login);
    }

    public boolean isAuthenticated(SocketChannel channel) {
        return connectedClients.values().contains(channel);
    }

    /**
     * This method removes client identified by its SocketChannel
     * and tries to close it if opened
     */
    public boolean removeClient(SocketChannel channel) throws IOException {

        writingBuffers.remove(channel);
        boolean removed = connectedClients.values().remove(channel);
//        try {
            channel.close();
//        } catch (IOException ex) {
//            throw new IOException("Error occured while closing client's SocketChannel");
//        }

        return removed;
    }

    public ByteBuffer prepareUserList() {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

        buffer.putInt(connectedClients.size());
        connectedClients.keySet().forEach(user -> {
            buffer.putInt(user.length());
            buffer.put(user.getBytes(Constants.CHARSET));
        });
        return buffer;
    }

    public void addWritingClient(SocketChannel newClient) {
        writingBuffers.put(newClient, new LinkedList<>());
    }

    public void addBufferToClient(SocketChannel client, ByteBuffer buffer) {
        writingBuffers.get(client).add(buffer);
    }

    public List<ByteBuffer> getClientBuffers(SocketChannel client) {
        List<ByteBuffer> list = new LinkedList<>(writingBuffers.get(client));
        writingBuffers.get(client).clear();
        return list;
    }

}
