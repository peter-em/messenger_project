package piotr.messenger.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.util.ClientData;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Component
public class UsersDatabase {

    private List<String> clients;
    private List<SocketChannel> channels;
    private UsersJDBCTemplate mysqlTable;

    public UsersDatabase() {
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

    public List<String> getUsers() {
        return clients;
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

    @Autowired
    public void setMysqlTable(UsersJDBCTemplate mysqlTable) {
        this.mysqlTable = mysqlTable;
    }
}
