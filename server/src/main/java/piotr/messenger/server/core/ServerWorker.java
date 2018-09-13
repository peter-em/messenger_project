package piotr.messenger.server.core;

import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.server.service.ConnectionParameters;
import piotr.messenger.server.service.ConversationService;
import piotr.messenger.server.service.DataFlowService;
import piotr.messenger.server.util.ClientState;
import piotr.messenger.server.util.ConversationEnd;
import piotr.messenger.server.util.ConversationPair;
import piotr.messenger.server.util.UsersDatabase;
import piotr.messenger.library.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.CancelledKeyException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		System.setProperty("sun.net.useExclusiveBind", "false");

	}

	@Override
	public void run() {
        logger = LoggerFactory.getLogger(ServerWorker.class);

        serverSocket = openSocket();
        if (serverSocket == null) {
            return;
        }

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
				//isRunning = false;
			} catch (CancelledKeyException cancelled) {
			    logger.error("Cancelled ({}).", cancelled.getMessage());
			    cancelled.printStackTrace();
			    break;
            }
		}

		logger.info("Closing server");
		closeSocket();
        dataFlowService.terminaterHandlers();
	}

	private ServerSocketChannel openSocket() {
        ServerSocketChannel serverSocket = null;
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(parameters.getHostAddress(), parameters.getHostPort()));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Starting server, port in use: {}.", parameters.getHostPort());
			//isRunning = true;
		} catch (IOException ioEx) {
            logger.error("Problem occured while opening listening port - ({}).", ioEx.getMessage());
            Thread.currentThread().interrupt();
		}
		return serverSocket;
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

