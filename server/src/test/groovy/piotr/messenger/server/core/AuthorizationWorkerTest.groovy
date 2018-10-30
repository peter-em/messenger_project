package piotr.messenger.server.core

import org.springframework.boot.test.context.SpringBootTest
import piotr.messenger.library.Constants
import piotr.messenger.server.service.AuthorizationService
import piotr.messenger.server.util.ServerPorts
import spock.lang.Specification
import spock.lang.Subject

import javax.annotation.Resource
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

@SpringBootTest
class AuthorizationWorkerTest extends Specification {

    @Subject
    AuthorizationWorker authWorker

    @Resource
    AuthorizationService authService
    @Resource
    ServerPorts parameters
    ConversationsWorker worker
    Thread task

    def setup() {
        worker = Mock()
        worker.isRunning() >> true
        authWorker = new AuthorizationWorker(authService, worker, parameters)
        task = new Thread(authWorker)
        task.start()
    }

    def cleanup() {
        task.interrupt()
    }


    def "Should close client channel (bytesread is -1) for sending incorrectly formatted data"() {
        given:"Connected client providing invalid data"
        SocketChannel channel = createServerConnection()
        ByteBuffer buffer = ByteBuffer.allocate(64)
        buffer.putInt(5)
        buffer.flip()
        channel.write(buffer)
        buffer.clear()

        when:"Trying to read from server"
        def bytes = channel.read(buffer)

        then:"Server closed this channel and -1 bytes are read"
        bytes == -1
    }

    def "Should return 0 with port number and then negative value for second client"() {
        given:"Connected clients trying to register with the same login"
        SocketChannel channel1 = createServerConnection()
        SocketChannel channel2 = createServerConnection()
        ByteBuffer buffer = setRegisterMode(client())
        channel1.write(buffer)
        buffer.flip()
        channel2.write(buffer)
        buffer.clear()

        when:"Reading response for the first client"
        def bytes = channel1.read(buffer)
        buffer.flip()

        then:"Should return 0 and positive integer"
        bytes > 0
        buffer.getInt() == 0
        buffer.getInt() > 0

        when:"Reading response for the second client"
        buffer.clear()
        bytes = channel2.read(buffer)
        buffer.flip()

        then:"Should return -1 (register error)"
        bytes > 0
        buffer.getInt() == -1
    }

    def "Should accept logging in user and then return -2 for another client trying to auth. with same login"() {
        given:"Registerd user and another client providing same login"
        SocketChannel channel1 = createServerConnection()
        SocketChannel channel2 = createServerConnection()
        ByteBuffer buffer = setRegisterMode(client())
        channel1.write(buffer)
        buffer = setLoginMode(client())
        channel2.write(buffer)
        buffer.clear()

        when:"Second client reads server response"
        channel2.read(buffer)
        buffer.flip()

        then:"Should get -2 (as someone already logged in with provided login"
        buffer.getInt() == -2
    }


    def createServerConnection() {
        SocketChannel channel = SocketChannel.open()
        channel.connect(new InetSocketAddress("localhost", 6969))
        channel.configureBlocking(true)
        channel
    }

    def client() {
        ByteBuffer buffer = ByteBuffer.allocate(64)
        buffer.putInt(4).put("name".getBytes(Constants.CHARSET))
        buffer.putInt(6).put("passwd".getBytes(Constants.CHARSET))
        buffer
    }

    def setLoginMode(ByteBuffer buffer) {
        buffer.putInt(Constants.LOGIN_MODE)
        buffer.flip()
    }

    def setRegisterMode(ByteBuffer buffer) {
        buffer.putInt(Constants.REGISTER_MODE)
        buffer.flip()
    }
}
