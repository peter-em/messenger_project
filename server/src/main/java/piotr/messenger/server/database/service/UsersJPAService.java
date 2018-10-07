package piotr.messenger.server.database.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import piotr.messenger.server.database.model.UserJPA;
import piotr.messenger.server.database.repository.IUserRepository;

import javax.annotation.Resource;

@Service
public class UsersJPAService implements UsersDAO<UserJPA> {

    private static final ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("id");

    @Resource
    private IUserRepository repository;

    @Override
    public void registerUser(String login, String password) {
        UserJPA user = new UserJPA();
        user.setLogin(login);
        user.setPassword(password);
        repository.save(user);
    }

    @Override
    public UserJPA getUser(String login) {

        return repository.findUserByLogin(login).orElseGet(() -> {
            UserJPA user = new UserJPA();
            user.setPassword("");
            return user;
        });
    }

    @Override
    public boolean hasUser(String login) {
        UserJPA user = new UserJPA();
        user.setLogin(login);
        return repository.exists(Example.of(user, matcher));
    }

    @Override
    public void updateLastLogged(UserJPA user) {
        user.setLastloggedAt(null);
        repository.save(user);
    }
}
