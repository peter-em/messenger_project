package piotr.messenger.server.database

import piotr.messenger.library.util.ClientData
import piotr.messenger.server.database.model.UserJPA
import piotr.messenger.server.database.service.UsersJPAService

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll


class UsersDatabaseTest extends Specification {

    UsersJPAService service
    @Subject UsersDatabase database
    def data = new ClientData(null,null,0)

    def setup() {
        service = Mock()
        database = new UsersDatabase(service)
    }

    @Unroll
    def "Verification #Nr: should return #value for provided ClientData"() {
        given:"stubbed getUser method and ClientData object"
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
        given:"stubbed hasUser method"
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

    def goodUser() {
        UserJPA user = new UserJPA()
        user.setPassword("passwd11")
        user
    }
}
