package piotr.messenger.server.util;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Clients {

    private List<String> clients;
    private List<SocketChannel> channels;

    public Clients() {
        clients = new ArrayList<>();
        channels = new ArrayList<>();
    }

    public boolean hasUser(String userName) {
        return clients.contains(userName);
    }

    public void addUser(String userName, SocketChannel channel) {
        clients.add(userName);
        channels.add(channel);
    }

    public List<String> getUsers() {
        return clients;
    }

    public List<SocketChannel> getChannels() {
        return channels;
    }

    public int usersCount() {
        return clients.size();
    }

    public String getUser(SocketChannel channel) {
        return clients.get(channels.indexOf(channel));
    }

    public SocketChannel getChannel(String userName) {
        int index = clients.indexOf(userName);
        if (index != -1)
            return channels.get(index);
        else
            return null;
    }

    public void dropUser(SocketChannel channel) {
        clients.remove(channels.indexOf(channel));
        channels.remove(channel);
    }
}
