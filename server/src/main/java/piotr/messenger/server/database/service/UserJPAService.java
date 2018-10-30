package piotr.messenger.server.database.service;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import piotr.messenger.server.database.model.UserJPA;
import piotr.messenger.server.database.repository.IUserRepository;

import javax.annotation.Resource;

@Service
public class UserJPAService {

    @Resource
    private IUserRepository repository;

    public void registerUser(String login, String password) {
        UserJPA user = new UserJPA();
        user.setLogin(login);
        user.setPassword(password);
        repository.save(user);
    }

    public UserJPA getUser(String login) {
        return repository.findByLogin(login).orElse(new UserJPA());
    }

    public boolean hasUser(String login) {
        UserJPA user = new UserJPA();
        user.setLogin(login);
        return repository.exists(Example.of(user));
    }

    public void updateLastLogged(UserJPA user) {
        user.setLastloggedAt(null);
        repository.save(user);
    }
}
