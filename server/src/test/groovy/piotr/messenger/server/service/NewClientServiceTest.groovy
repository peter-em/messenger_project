package piotr.messenger.server.service

import piotr.messenger.library.Constants
import piotr.messenger.server.database.UsersDatabase
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.util.stream.Collectors
import java.util.stream.IntStream

class NewClientServiceTest extends Specification {

    @Shared
    String str = IntStream.range(0, Constants.RECORD_LENGTH+1)
            .mapToObj({ i -> "c"})
            .collect(Collectors.joining(""))

    ByteBuffer dataBuffer
    UsersDatabase database
    ClientsConnectionService connectionService

    @Subject
    NewClientService service

    def setup() {
        dataBuffer = ByteBuffer.allocate(32)
        dataBuffer.putInt(0).putInt(0)

        database = Mock()
        connectionService = Mock()
        service = new NewClientService(database, connectionService)
    }

    @Unroll
    def 'Should return -4 when data in ByteBuffer #reason'() {

        when:
        buffer.flip()
        def result = service.handleData(buffer, null)

        then:
        result == -4

        where:
        reason                                  | buffer
        'were encoded incorrectly'              | ByteBuffer.allocate(64)
                                                    .putInt(5).put("str".bytes)
        'contains string longer than accepted'  | ByteBuffer.allocate(64)
                                                    .putInt(Constants.RECORD_LENGTH+1).put(str.bytes)
    }

    def 'Should return -2 if someone already logged into provided account'() {
        given:'Mocked connectionservice'
        connectionService.isAuthenticated(_ as String) >> true
        dataBuffer.putInt(1).put("l".getBytes(Constants.CHARSET))
        dataBuffer.putInt(1).put("p".getBytes(Constants.CHARSET))
        dataBuffer.putInt(1)
        dataBuffer.flip()

        when:'handleData checks if such client is already logged in'
        def result = service.handleData(dataBuffer,null)

        then:'should return -2'
        result == -2
    }


    @Unroll
    def 'Should return 0 and add user to connectionService when verification was successful'() {
        given:'Mocked database methods'
        database.verifyClient(_) >> flag
        database.registerClient(_) >> flag
        dataBuffer.putInt(mode)
        dataBuffer.flip()

        when:'handleData performs database verification'
        def result = service.handleData(dataBuffer,null)

        then:'should return 0 or -1'
        result == value

        where:
        mode                    | flag      | value
        Constants.LOGIN_MODE    | true      | 0
        Constants.REGISTER_MODE | true      | 0
        Constants.LOGIN_MODE    | false     | -1
        Constants.REGISTER_MODE | false     | -1
    }
}
