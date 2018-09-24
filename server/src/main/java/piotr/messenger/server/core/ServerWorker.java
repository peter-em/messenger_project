package piotr.messenger.server.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.server.service.ConnectionParameters;
import piotr.messenger.server.service.DataFlowService;

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
public class ServerWorker implements Runnable {

	private ServerSocketChannel serverSocket;
	private Selector selector;
	private Logger logger;
	private DataFlowService dataFlowService;
	private ConnectionParameters parameters;

    public ServerWorker() {
        logger = LoggerFactory.getLogger(ServerWorker.class);
        System.setProperty("sun.net.useExclusiveBind", "false");
    }

    @Override
	public void run() {

        openSocket();

        while (!Thread.interrupted()) {
			try {

                dataFlowService.cleanupClosedConversations();
                dataFlowService.updateClients();

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
                        dataFlowService.acceptClient(serverSocket);

					} else if (key.isReadable()) {
                        dataFlowService.readClientData((SocketChannel) key.channel());

					} else if (key.isWritable()) {
                        dataFlowService.writeClientData((SocketChannel) key.channel());
                        key.interestOps(SelectionKey.OP_READ);
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
		}

		logger.info("Closing server");
        dataFlowService.terminateHandlers();
		closeSocket();
	}

	private void openSocket() {
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(parameters.getHostAddress(), parameters.getHostPort()));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException ioEx) {
            logger.error("Problem occured while opening listening port - ({}).", ioEx.getMessage());
            Thread.currentThread().interrupt();
            return;
		}
        logger.info("Starting server, port in use: {}.", parameters.getHostPort());
	}

	private void closeSocket() {
		try {
			if (serverSocket.isOpen())
				serverSocket.close();
		} catch (IOException ioEx) {
            logger.error("Problem occured while closing server socket - ({}).", ioEx.getMessage());
		}
	}


    @Autowired
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    @Autowired
    public void setDataFlowService(DataFlowService dataFlowService) {
        this.dataFlowService = dataFlowService;
    }

    @Autowired
    public void setParameters(ConnectionParameters parameters) {
        this.parameters = parameters;
    }
}

