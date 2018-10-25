package piotr.messenger.server.database.service

import org.springframework.boot.test.context.SpringBootTest
import piotr.messenger.server.database.model.UserJPA
import piotr.messenger.server.database.repository.IUserRepository
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.annotation.Resource

@SpringBootTest
class UsersJPAServiceTest extends Specification {

    @Resource
    @Subject
    UsersJPAService service

    @Resource
    IUserRepository repository

    @Unroll
    def "Should hasUser return #value for provided '#login'"() {

        expect:"Proper boolean value for provided user"
        service.hasUser(login as String) == value

        where:
        login    || value
        "login1" || true
        "other"  || false
    }

    @Unroll
    def "Should return #non initialized UserJPA from database when provided #non existing login"() {

        when:"Getting UserJPA for provided login"
        def result = service.getUser(login)

        then:"login and password fields should be equal"
        result.login == value.login
        result.password == value.password

        where:
        non   | login    || value
        ""    | "login1" || createUser()
        "non" | "other"  || new UserJPA()
    }

    def "Should update lastloggedAt field"() {
        given:"UserJPA from database"
        def user = repository.findByLogin("login1").orElse(new UserJPA())

        expect:"registered and lastlogged dates should be the same"
        user.registeredAt.isEqual(user.lastloggedAt)


        when:"updateLastLogged(UserJPA user) method is called"
        service.updateLastLogged(user)

        and:"user object is retrieved again from database"
        user = repository.findByLogin("login1").orElse(new UserJPA())

        then:"lastlogged is updated"
        user.lastloggedAt.isAfter(user.registeredAt)
    }

    def "Should table size increase when registering new client"() {
        given:"Login and password of a new user"
        def login = "newuser"
        def password = "passwd"

        and:"table size before"
        def size = repository.count()

        when:"registering user"
        service.registerUser(login, password)

        then:"table size should increase"
        repository.count() > size
    }


    def setup() {
        repository.save(createUser())
    }

    def cleanup() {
        repository.deleteAll()
    }

    def createUser() {
        UserJPA user = new UserJPA()
        user.login = "login1"
        user.password = "password"
        user
    }
}