package piotr.messenger.library.service

import piotr.messenger.library.util.ClientData
import spock.lang.Specification

import java.nio.ByteBuffer

class ClientDataConverterTest extends Specification {

    def "Should read String from ByteBuffer"() {
        given:
        def word = "somestring"
        ByteBuffer buffer = ByteBuffer.allocate(word.size())
        byte[] array = new byte[word.size()]
        buffer.put(word.bytes)
        buffer.flip()

        when:
        def returned = ClientDataConverter.getStringFromArray(buffer, array, word.size())

        then:
        returned == word
    }

    def "Should returned ClientData object be equal after conversions"() {
        given:
        ClientData inData = new ClientData("login", "password", 1)

        when:
        ByteBuffer buffer = ClientDataConverter.encodeAuthToBuffer(inData)
        buffer.flip()
        ClientData outData = ClientDataConverter.decodeAuthFromBuffer(buffer)

        then:
        inData == outData
    }

}
