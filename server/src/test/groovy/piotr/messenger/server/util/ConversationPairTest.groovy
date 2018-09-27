package piotr.messenger.server.util

import spock.lang.Specification

import java.nio.channels.SocketChannel

class ConversationPairTest extends Specification {

    def "Should hasNullClient() return true when at least one SocketChannel client is null"() {
        given:
        SocketChannel client = SocketChannel.open()
        ConversationPair pair1 = new ConversationPair(client, null)
        ConversationPair pair2 = new ConversationPair(null, client)

        when:
        def hasNull1 = pair1.hasNullClient()
        def hasNull2 = pair2.hasNullClient()

        then:
        hasNull1
        hasNull2

    }
}
