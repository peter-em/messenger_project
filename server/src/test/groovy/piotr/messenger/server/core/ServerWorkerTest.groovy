package piotr.messenger.server.core

import piotr.messenger.server.service.ConnectionParameters
import spock.lang.Specification

import java.nio.channels.ServerSocketChannel

class ServerWorkerTest extends Specification {


    def "Should openSocket() return false when port already in use"() {

        setup:
        ServerSocketChannel socket = ServerSocketChannel.open()
        ConnectionParameters parameters = new ConnectionParameters()
        socket.socket().bind(new InetSocketAddress(parameters.getHostAddress(), parameters.getHostPort()))

        ServerWorker worker = new ServerWorker()
        worker.setParameters(new ConnectionParameters())

        when:
        def socketOpened = worker.openSocket()

        then:
        !socketOpened

        cleanup:
        socket.close()
    }

    def "Should openSocket() throw NullPointerException if autowiring fails"() {

        given:
        ServerWorker worker = new ServerWorker()

        when:
        worker.openSocket()

        then:
        thrown(NullPointerException)
    }





}
