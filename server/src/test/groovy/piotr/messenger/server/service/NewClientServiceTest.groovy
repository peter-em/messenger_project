package piotr.messenger.server.service

import piotr.messenger.library.Constants
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.util.stream.Collectors
import java.util.stream.IntStream

class NewClientServiceTest extends Specification {

    @Shared str = IntStream.range(0, Constants.RECORD_LENGTH+1).mapToObj({ i -> "c"}).collect(Collectors.joining(""))

    @Unroll
    def 'Should return -1 when data in ByteBuffer #reason'() {
        given:
        def service = new NewClientService(null, null)

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

}
