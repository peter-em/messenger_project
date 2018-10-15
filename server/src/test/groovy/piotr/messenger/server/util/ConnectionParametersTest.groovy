package piotr.messenger.server.util

import piotr.messenger.server.util.ConnectionParameters
import spock.lang.Specification

class ConnectionParametersTest extends Specification {


    def "Do getLastUsedPort() and getWorkerPort() return the same value"() {
        given:
        ConnectionParameters parameters = new ConnectionParameters()
        def port

        when:
        port = parameters.getWorkerPort()

        then:
        port == parameters.getLastUsedPort()
    }

    def "Is port reused after deleting"() {
        given:
        ConnectionParameters parameters = new ConnectionParameters()

        when:
        def port = parameters.getWorkerPort()
        parameters.deletePort(port)

        then:
        port == parameters.getWorkerPort()
    }

    def "Should remove assigned port from list"() {
        given: "connectionParameters class with one port assigned"
        ConnectionParameters parameters = new ConnectionParameters()
        def port = parameters.getWorkerPort()

        when: "assigned port is removed from the list"
        parameters.deletePort(port)

        then: "list size should be 0"
        parameters.getExecutorPorts().size() == 0
    }
}
