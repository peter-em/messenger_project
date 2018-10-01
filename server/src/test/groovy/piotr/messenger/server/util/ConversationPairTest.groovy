package piotr.messenger.server.util

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.channels.SocketChannel

class ConversationPairTest extends Specification {


    static channel1 = SocketChannel.open()
    static channel2 = SocketChannel.open()


    @Unroll
    def "Should hasNullClient() return true when at least one SocketChannel client is null"() {
        given:"ConversationPair with assigned clients"
        def pair = new ConversationPair(client1, client2)

        when:"checking if pair contains null client"
        def hasNull = pair.hasNullClient()

        then:"should return expected"
        hasNull == expected

        where:
        client1  | client2  || expected
        channel1 | null     || true
        null     | channel2 || true
        channel1 | channel2 || false
    }

    def "Should add at most two clients"() {
        given:"ConversationPair with null clients"
        def pair = new ConversationPair(null, null)

        when:"adding one new client"
        pair.addClient(channel1)

        then:"should still have null client"
        pair.hasNullClient()

        when:"adding second client"
        pair.addClient(channel2)

        then:"should not have a null client"
        !pair.hasNullClient()

        when:"adding another client"
        pair.addClient(SocketChannel.open())

        then:"pair should remain unchanged"
        pair.getClient1() == channel1
        pair.getClient2() == channel2
    }

    def "Should return other client than provided as argument"() {
        given:"ConversationPair with assigned clients"
        def pair = new ConversationPair(channel1, channel2)

        when:"getOtherClient() called"
        def returned1 = pair.getOtherClient(channel1)
        def returned2 = pair.getOtherClient(channel2)

        then:"should return other client from this pair"
        returned1 == channel2
        returned2 == channel1
    }
}
