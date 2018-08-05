package piotr.messenger.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

class Clients {

    private List<String> clients;
    private List<SocketChannel> channels;

    Clients() {
        clients = new ArrayList<>();
        channels = new ArrayList<>();
    }

    boolean hasUser(String userName) {
        return clients.contains(userName);
    }

    void addUser(String userName, SocketChannel channel) {
        clients.add(userName);
        channels.add(channel);
    }

    List<String> getUsers() {
        return clients;
    }

    List<SocketChannel> getChannels() {
        return channels;
    }

    int usersCount() {
        return clients.size();
    }

    String getUser(SocketChannel channel) {
        return clients.get(channels.indexOf(channel));
    }

    SocketChannel getChannel(String userName) {
        return channels.get(clients.indexOf(userName));
    }

    void dropUser(SocketChannel channel) {
        clients.remove(channels.indexOf(channel));
        channels.remove(channel);
    }
}
