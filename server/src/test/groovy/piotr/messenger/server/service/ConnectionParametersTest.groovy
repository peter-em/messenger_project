package piotr.messenger.server.service

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
}
