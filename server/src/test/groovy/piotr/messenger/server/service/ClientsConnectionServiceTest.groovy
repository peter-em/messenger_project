package piotr.messenger.server.service

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class ClientsConnectionServiceTest extends Specification {

    @Subject ClientsConnectionService connectionService
    static clientChannel = SocketChannel.open()
    static clientStr = "someuser"

    def setup() {
        connectionService = new ClientsConnectionService()
        connectionService.connectedClients.put(clientStr,clientChannel)
    }

    @Unroll
    def "isAuthenticated()#Nr: should return #bool for provided client(#value)"() {
        expect:"authentication returns bool"
        connectionService.isAuthenticated(value) == bool

        where:
        Nr  | value               || bool
        1   | clientChannel       || true
        2   | SocketChannel.open()|| false
        3   | clientStr           || true
        4   | "otheruser"         || false
    }

    def "Should container size increase after addUser method call"() {
        when:"new user added"
        connectionService.addUser("newuser",SocketChannel.open())

        then:"container size should be 2"
        connectionService.connectedClients.size() == 2
    }

    @Unroll
    def "Should return #value from getChannel() method call"() {
        expect:"value from getChannel(client)"
        connectionService.getChannel(clientStr) == clientChannel

        where:
        client      | value
        clientStr   | clientChannel
        "nonrgstr"  | null
    }

    def "Should return non empty Collection"() {
        when:"requested for SocketChannels"
        def value = connectionService.getChannels()

        then:"collection with one element should be returned"
        value.size() == 1
        value[0] == clientChannel
    }

    def "Should remove elements from map containers"() {
        given:"writingBuffers containter with one element added"
        SocketChannel ch = SocketChannel.open()
        connectionService.writingBuffers.put(ch, null)

        when:"removeClient() method called, with client ch"
        def value = connectionService.removeClient(ch)

        then:"should return false"
        !value

        and:"writingBuffers, connectedClients sizes should be respectively 0 and 1"
        connectionService.writingBuffers.size() == 0
        connectionService.connectedClients.size() == 1

        when:"removeClient() method called, with clientChannel"
        value = connectionService.removeClient(clientChannel)

        then:"should return true and connectedClients size is 0"
        value
        connectionService.connectedClients.size() == 0
    }

    def "Should return prepared ByteBuffer from connectedCliens"() {
        given:"example buffer"
        def example = getBuffer()

        when:"prepareUserList is called"
        def returned = connectionService.prepareUserList()

        then:"returned and example buffers should be the same"
        example.flip()
        returned.flip()
        returned == example
    }

    def "WritingBuffers' size should increase to 1"() {
        when:"adding writing client"
        connectionService.addWritingClient(clientChannel)

        then:"size should be 1"
        connectionService.writingBuffers.size() == 1
    }

    def "Should list of buffers for provided client increase to 1"() {
        given:"client with allocated list for buffers"
        connectionService.addWritingClient(clientChannel)

        when:"adding buffer to client"
        connectionService.addBufferToClient(clientChannel, ByteBuffer.allocate(64))

        then:"size of buffers list be 1"
        connectionService.writingBuffers.get(clientChannel).size() == 1
    }

    def "Should return list with single buffer"() {
        given:"client with allocated buffer"
        connectionService.addWritingClient(clientChannel)
        connectionService.addBufferToClient(clientChannel, ByteBuffer.allocate(64))

        when:"client's buffer list requested"
        def list = connectionService.getClientBuffers(clientChannel)

        then:"returned list should have one element"
        list.size() == 1
    }

    def getBuffer() {
        ByteBuffer buff = ByteBuffer.allocate(1024)
        buff.putInt(1).putInt(clientStr.length())
        buff.put(clientStr.bytes)
        buff
    }
}
