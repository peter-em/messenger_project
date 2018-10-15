package piotr.messenger.server.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import piotr.messenger.server.database.model.UserJPA;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserJPA, Integer> {

    Optional<UserJPA> findByLogin(String login);

}
