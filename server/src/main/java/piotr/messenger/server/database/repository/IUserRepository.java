package piotr.messenger.server.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import piotr.messenger.server.database.model.UserJPA;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserJPA, Integer> {

    @Query("SELECT u FROM UserJPA u WHERE u.login = :login")
    Optional<UserJPA> findUserByLogin(@Param("login") String login);

}
