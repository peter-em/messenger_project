package piotr.messenger.server.database;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import piotr.messenger.library.util.ClientData;


@Component
@AllArgsConstructor
public class UsersDatabase {

    private final UsersJDBCTemplate mysqlTable;

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

}
