package piotr.messenger.server.core;

import piotr.messenger.server.util.ConversationEnd;
import piotr.messenger.server.util.ConversationPair;
import piotr.messenger.library.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConversationWorker implements Runnable {

	private final Logger logger;
	private final ConversationPair convPair;
	private final ConversationPair clients;
	private int handlerPort;
	private int clientCount;
	private boolean isRunning;
	private String handlerAddress;
	private ByteBuffer readBuffer;
    private Map<SocketChannel, ByteBuffer> pendingData;
	private BlockingQueue<ConversationEnd> handlersEndData;

	public ConversationWorker(String handlerAddress, int handlerPort,
                              BlockingQueue<ConversationEnd> handlersEndData,
                              ConversationPair convPair) {

        logger = LoggerFactory.getLogger(ConversationWorker.class);
		this.handlerAddress = handlerAddress;
		this.handlerPort = handlerPort;
		this.handlersEndData = handlersEndData;
		this.convPair = convPair;
		clients = new ConversationPair(null, null);
		readBuffer = ByteBuffer.allocate(Constants.BUFFER_SIZE*2);
		pendingData = new HashMap<>();
		clientCount = 0;
		isRunning = false;
	}

    private void openSocket(ServerSocketChannel handler, Selector convSelector) throws IOException {

	    handler.configureBlocking(false);
        handler.socket().bind(new InetSocketAddress(handlerAddress, handlerPort));
        handler.register(convSelector, SelectionKey.OP_ACCEPT);
        isRunning = true;
    }


	@Override
	public void run() {

	    Thread.currentThread().setName("ConvThread-" + handlerPort);

        try (ServerSocketChannel handler = ServerSocketChannel.open();
             Selector convSelector = SelectorProvider.provider().openSelector()) {

		    openSocket(handler, convSelector);

            while (isRunning) {

                convSelector.select();

                SelectionKey key;
                Iterator<SelectionKey> selectedKeys;
                selectedKeys = convSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                    } else if (key.isAcceptable()) {
                        accept(handler, convSelector);
                    } else if (key.isReadable()) {
                        read(key, convSelector);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
            }

        } catch (IOException ioEx) {
            logger.error(ioEx.getMessage());
        }

        logger.info("Closing conversation handler on port {}.", handlerPort);
		handlersEndData.add(new ConversationEnd(handlerPort, convPair));
	}

	private void accept(ServerSocketChannel handler, Selector convSelector) throws IOException {

		//accept connection, set it to non-blocking mode
		if (clientCount < 2) {
			SocketChannel newClient = handler.accept();
            clients.addClient(newClient);
			clientCount++;

			newClient.configureBlocking(false);
			if (clientCount == 2) {
				clients.getClient1().register(convSelector, SelectionKey.OP_READ);
				clients.getClient2().register(convSelector, SelectionKey.OP_READ);
			}
		} else {
			SocketChannel cancelCh = handler.accept();
			cancelCh.close();
            logger.info("Client rejected, doesn't belong to this conversation");
		}
	}

	private void read(SelectionKey key, Selector convSelector) throws IOException {
		SocketChannel clientRead = (SocketChannel)key.channel();
		readBuffer.clear();
		//read data from channel
		int bytesRead;
        bytesRead = clientRead.read(readBuffer);

		if (bytesRead == -1) {
			//client ended conversation cleanly, server closing channel
            clients.getClient1().close();
            clients.getClient2().close();
			key.cancel();
			isRunning = false;

		} else {

            clientRead = clients.getOtherClient(clientRead);
			byte[] copyData = new byte[bytesRead];
			System.arraycopy(readBuffer.array(), 0, copyData, 0, bytesRead);
			send(clientRead, copyData, convSelector);
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();

        ByteBuffer buffer = pendingData.get(clientSocket);
        clientSocket.write(buffer);
        if (buffer.remaining() > 0) {
            logger.error("Buffer was not emptied!");
        }

        key.interestOps(SelectionKey.OP_READ);
	}

	private void send(SocketChannel client, byte[] data, Selector convSelector) {

        pendingData.put(client, ByteBuffer.wrap(data));
        SelectionKey selectionKey = client.keyFor(convSelector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
	}


}

