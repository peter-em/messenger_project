package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.database.UsersDatabase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

@Component
public class ClientsConnectionService {

    private final Map<String, SocketChannel> connectedClients = new HashMap<>();
    private final Map<SocketChannel, List<ByteBuffer>> writingBuffers = new HashMap<>();
    private final UsersDatabase database;

    public ClientsConnectionService(UsersDatabase database) {
        this.database = database;
    }

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
        channel.close();
        Optional<Map.Entry<String, SocketChannel>> optional = connectedClients
                .entrySet()
                .stream()
                .filter(e -> channel.equals(e.getValue()))
                .findFirst();

        if (optional.isPresent()) {
            database.setUserOffline(optional.get().getKey());
            connectedClients.remove(optional.get().getKey());
            return true;
        }
        return false;
    }

    public ByteBuffer prepareUserList() {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

        buffer.putInt(connectedClients.size());
        connectedClients.keySet().forEach(user -> {
            byte[] data = user.getBytes(Constants.CHARSET);
            buffer.putInt(data.length);
            buffer.put(data);
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
