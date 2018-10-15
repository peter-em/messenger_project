package piotr.messenger.server.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.server.util.ConnectionParameters;
import piotr.messenger.server.service.AuthorizationService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.CancelledKeyException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthorizationWorker implements Runnable {

	private Selector selector;
	private Logger logger;
	private AuthorizationService authorizationService;
	private ConnectionParameters parameters;
	private ConversationsWorker worker;

    public AuthorizationWorker() {
        logger = LoggerFactory.getLogger(AuthorizationWorker.class);
        System.setProperty("sun.net.useExclusiveBind", "false");
    }

    @Override
	public void run() {

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            openSocket(serverSocket);
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

                authorizationService.updateClients();

                //iterating through set of keys which have available events
                selector.select();
                SelectionKey key;
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    key = selectedKeys.next();
                    selectedKeys.remove();

                    //check for available event and handle it
                    if (!key.isValid()) {
                        logger.error("INVALID KEY");
                        key.cancel();

                    } else if (key.isAcceptable()) {
                        authorizationService.acceptClient(serverSocket);

                    } else if (key.isReadable()) {
                        authorizationService.readClientData((SocketChannel) key.channel());

                    } else if (key.isWritable()) {
                        authorizationService.writeClientData((SocketChannel) key.channel());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException ioEx) {
            logger.error("Problem occured while listening for events - "
                    + ioEx.getMessage());
        } catch (CancelledKeyException cancelled) {
            logger.error("Cancelled ({}).", cancelled.getMessage());
            Thread.currentThread().interrupt();
        } catch (InterruptedException abqEx) {
            logger.error("ArrayBlockingQueue error ({})", abqEx.getMessage());
            Thread.currentThread().interrupt();
        }

        worker.stopWorker();
		logger.info("Closing server");
	}

	private void openSocket(ServerSocketChannel serverSocket) throws IOException {
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(parameters.getHostAddress(), parameters.getHostPort()));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        logger.info("Starting server, port in use: {}.", parameters.getHostPort());
	}

    @Autowired
    public void setSelector(@Qualifier("mainSelector") Selector selector) {
        this.selector = selector;
    }

    @Autowired
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Autowired
    public void setParameters(ConnectionParameters parameters) {
        this.parameters = parameters;
    }

    @Autowired
    public void setWorker(ConversationsWorker worker) {
        this.worker = worker;
    }
}

