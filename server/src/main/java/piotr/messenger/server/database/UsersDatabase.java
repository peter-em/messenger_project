package piotr.messenger.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.util.ClientData;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Component
public class UsersDatabase {

    private Map<String, SocketChannel> connectedClients;
    private UsersJDBCTemplate mysqlTable;

    public UsersDatabase() {
        connectedClients = new HashMap<>();
    }

    public boolean hasUser(String userName) {
        return null != connectedClients.get(userName);
    }

    public boolean hasUser(SocketChannel channel) {
        return connectedClients.values().contains(channel);
    }

    public void addUser(String userName, SocketChannel channel) {
        connectedClients.put(userName, channel);
    }

    public boolean verifyClient(ClientData data) {
        UserSQL user = mysqlTable.getUser(data.getLogin());
        return user != null && user.getPassword().equals(data.getPassword());
    }

    public boolean registerClient(ClientData data) {
        if (!mysqlTable.hasUser(data.getLogin())) {
            mysqlTable.registerUser(data.getLogin(), data.getPassword());
            return true;
        }
        return false;
    }

    public int connectedSize() {
        return connectedClients.size();
    }

    public Set<String> getUsers() {
        return connectedClients.keySet();
    }

    public Collection<SocketChannel> getChannels() {
        return connectedClients.values();
    }


    public SocketChannel getChannel(String userName) {
        return connectedClients.get(userName);
    }

    public void dropUser(SocketChannel channel) {
        connectedClients.values().remove(channel);
    }

    @Autowired
    public void setMysqlTable(UsersJDBCTemplate mysqlTable) {
        this.mysqlTable = mysqlTable;
    }
}
