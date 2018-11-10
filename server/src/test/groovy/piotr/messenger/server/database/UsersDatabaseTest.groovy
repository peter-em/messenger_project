package piotr.messenger.server.database

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import piotr.messenger.library.util.ClientData
import piotr.messenger.server.database.model.UserJPA
import piotr.messenger.server.database.service.UserJPAService

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll


class UsersDatabaseTest extends Specification {

    UserJPAService service
    BCryptPasswordEncoder passwordEncoder
    @Subject UsersDatabase database
    def data = new ClientData("","",0)

    def setup() {
        service = Mock()
        passwordEncoder = new BCryptPasswordEncoder()
        database = new UsersDatabase(service, passwordEncoder)
    }

    @Unroll
    def "Verification #Nr: should return #value for provided ClientData"() {
        given:"mocked getUser method and ClientData object"
        data.setPassword(password)
        service.getUser(_) >> user

        expect:"verifyClient returns #value"
        database.verifyClient(data) == value

        where:
        Nr  | password  | user      || value
        1   | "passwd11"| goodUser()|| true
        2   | "other"   | goodUser()|| false
    }

    @Unroll
    def "Should #not call registerUser when provided credentials are #not available"() {
        given:"mocked hasUser method"
        service.hasUser(_) >> flag

        when:"registerClient method is called"
        database.registerClient(data)

        then:"registerUser method from jdbc should be called #n times"
        n * service.registerUser(_,_)

        where:
        not  | flag  || n
        ""   | false || 1
        "not"| true  || 0
    }

    @Unroll
    def "Should change user status when active equals 1"() {
        given:"mocked getUser method"
        UserJPA user = goodUser()
        user.setActive(status)
        service.getUser(_ as String) >> user

        when:"setUserOffline method is called"
        database.setUserOffline("")

        then:"should update user status when it was 1"
        n * service.updateUserStatus(user)

        where:
        status  | n
        0       | 0
        1       | 1
    }

    def goodUser() {
        UserJPA user = new UserJPA()
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder()
        user.setPassword(encoder.encode("passwd11"))
        user
    }
}
