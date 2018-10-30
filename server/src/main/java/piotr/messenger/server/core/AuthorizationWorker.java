package piotr.messenger.server.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.server.util.ServerPorts;
import piotr.messenger.server.service.AuthorizationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;


@Component
@Slf4j
public class AuthorizationWorker implements Runnable {

	private final AuthorizationService authorizationService;
	private final ConversationsWorker worker;
	private final ServerPorts ports;

    public AuthorizationWorker(AuthorizationService authorizationService,
                               ConversationsWorker worker,
                               ServerPorts ports) {
        this.authorizationService = authorizationService;
        this.worker = worker;
        this.ports = ports;
        System.setProperty("sun.net.useExclusiveBind", "false");
    }

    @Override
	public void run() {

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open();
             Selector selector = SelectorProvider.provider().openSelector()) {
            openSocket(serverSocket, selector);
            Thread task = new Thread(worker);
            task.start();

            int counter = 0;
            while (!worker.isRunning() || counter++ < 20) {
                Thread.sleep(100);
            }
            if (counter == 20) {
                Thread.currentThread().interrupt();
            }

            while (!Thread.interrupted()) {

                authorizationService.updateClients(selector);

                //iterating through set of keys which have available events
                selector.select();
                SelectionKey key;
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    keyIterator.remove();

                    //check for available event and handle it
                    if (!key.isValid()) {
                        log.error("INVALID KEY");
                        key.cancel();

                    } else if (key.isAcceptable()) {
                        authorizationService.acceptClient(serverSocket.accept(), selector);

                    } else if (key.isReadable()) {
                        authorizationService.readClientData((SocketChannel) key.channel(), selector);

                    } else if (key.isWritable()) {
                        authorizationService.writeClientData((SocketChannel) key.channel());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException ioEx) {
            log.error("Problem occured while listening for events - "
                    + ioEx.getMessage());
        } catch (CancelledKeyException cancelled) {
            log.error("Cancelled ({}).", cancelled.getMessage());
            Thread.currentThread().interrupt();
        } catch (InterruptedException abqEx) {
            log.error("ArrayBlockingQueue error ({})", abqEx.getMessage());
            Thread.currentThread().interrupt();
        }

        worker.stopWorker();
		log.info("Closing server");
	}

	private void openSocket(ServerSocketChannel serverSocket, Selector selector) throws IOException {
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(ports.getHostPort()));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        log.info("Starting server, port in use: {}.", ports.getHostPort());
	}
}
