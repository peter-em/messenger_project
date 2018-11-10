package piotr.messenger.server.database;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import piotr.messenger.library.util.ClientData;
import piotr.messenger.server.database.model.UserJPA;
import piotr.messenger.server.database.service.UserJPAService;


@Component
@AllArgsConstructor
public class UsersDatabase {

    private final UserJPAService jpaService;
    private final BCryptPasswordEncoder passwordEncoder;

    public boolean verifyClient(ClientData data) {

        UserJPA user = jpaService.getUser(data.getLogin());
        if (passwordEncoder.matches(data.getPassword(), user.getPassword())) {
            user.setLastloggedAt(null);
            user.setActive(1);
            jpaService.updateUserStatus(user);
            return true;
        }
        return false;
    }

    public boolean registerClient(ClientData data) {

        if (!jpaService.hasUser(data.getLogin())) {
            jpaService.registerUser(data.getLogin(), passwordEncoder.encode(data.getPassword()));
            return true;
        }
        return false;
    }

    public void setUserOffline(String login) {
        UserJPA user = jpaService.getUser(login);
        if (user.getActive() == 1) {
            user.setActive(0);
            jpaService.updateUserStatus(user);
        }
    }
}
