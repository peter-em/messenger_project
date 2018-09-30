package piotr.messenger.server.util

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.channels.SocketChannel

class ConversationPairTest extends Specification {

    static channel = SocketChannel.open()

    @Unroll
    def "Should hasNullClient() return true when at least one SocketChannel client is null"() {
        given:"ConversationPair with assigned clients"
        ConversationPair pair = new ConversationPair(client1, client2)

        when:"checking if pair contains null client"
        def hasNull = pair.hasNullClient()

        then:"should return expected"
        hasNull == expected

        where:
        client1 | client2 || expected
        channel | null    || true
        null    | channel || true
        channel | channel || false
    }
}
