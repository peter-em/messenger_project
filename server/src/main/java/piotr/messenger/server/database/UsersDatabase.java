package piotr.messenger.server.database;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import piotr.messenger.library.util.ClientData;
import piotr.messenger.server.database.model.UserJPA;
import piotr.messenger.server.database.service.UserJPAService;


@Component
@AllArgsConstructor
public class UsersDatabase {

    private final UserJPAService jpaService;

    public boolean verifyClient(ClientData data) {

        UserJPA user = jpaService.getUser(data.getLogin());
        if (data.getPassword().equals(user.getPassword())) {
            jpaService.updateLastLogged(user);
            return true;
        }
        return false;
    }

    public boolean registerClient(ClientData data) {

        if (!jpaService.hasUser(data.getLogin())) {
            jpaService.registerUser(data.getLogin(), data.getPassword());
            return true;
        }
        return false;
    }
}
