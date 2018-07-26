package piotr.messenger.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HandleConversation implements Runnable {

	private ServerSocketChannel handler;
	private SocketChannel client1;
	private SocketChannel client2;
	private Selector talkSelector;
	private String handlerAddress;
	private int handlerPort;
	private ByteBuffer readBuffer;
	private int clientCount;
	private volatile boolean isRunning;
	private volatile boolean isWaiting;
	private List<ChangeRequest> changeRequests;
	private Map<SocketChannel, List<ByteBuffer>> pendingData;
	private ArrayBlockingQueue<ConversationEnd> handlersEndData;
	private ConversationPair convPair;

	HandleConversation(String handlerAddress, int handlerPort,
									  ArrayBlockingQueue<ConversationEnd> queue, ConversationPair pair) {
		changeRequests = new LinkedList<>();
		pendingData = new HashMap<>();
		this.handlerAddress = handlerAddress;
		this.handlerPort = handlerPort;
		handlersEndData = queue;
		convPair = pair;
		readBuffer = ByteBuffer.allocate(ServerThread.BUFF_SIZE*2);
		clientCount = 0;
		isRunning = false;
		isWaiting = true;
		openSocket();
	}

	@Override
	public void run() {


		int timer = 0;
		try {
			while (isWaiting && timer < 1200) {
				TimeUnit.MILLISECONDS.sleep(50);
				timer++;
			}
		} catch (InterruptedException itrEx) {
			System.out.println(itrEx.getMessage());
		}
		if (timer == 1200) {
			isRunning = false;
			//isWaiting = false;
		} else {
			isRunning = true;
			isWaiting = false;
		}
		Iterator selectedKeys;
		SelectionKey key;
		while (isRunning) {
			try {
				for (ChangeRequest changeRequest : changeRequests) {
					switch (changeRequest.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey swKey = changeRequest.socket.keyFor(talkSelector);
							swKey.interestOps(changeRequest.ops);
					}
				}
				changeRequests.clear();
				talkSelector.select();

				selectedKeys = talkSelector.selectedKeys().iterator();
				while (selectedKeys.hasNext() && isRunning) {
					key = (SelectionKey)selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
						continue;

					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable())
						read(key);
					else if (key.isWritable())
						write(key);
				}

			} catch (IOException ioEx) {
				System.err.println("Problem occured while listenint for events");
				System.out.println(ioEx.getMessage());
				isRunning = false;
			}
		}

		//System.out.println("Closing conversation handler");
		closeSocket();
		handlersEndData.add(new ConversationEnd(handlerPort, convPair, this));
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();

		//accept connection, set it to non-blocking mode
		if (clientCount < 2) {
			SocketChannel newClient = serverSocketChannel.accept();
			if (clientCount == 0)
				client1 = newClient;
			else
				client2 = newClient;
			clientCount++;

			newClient.configureBlocking(false);
			if (clientCount == 2) {
				client1.register(talkSelector, SelectionKey.OP_READ);
				client2.register(talkSelector, SelectionKey.OP_READ);
			}
		} else {
			SocketChannel cancelCh = serverSocketChannel.accept();
			cancelCh.close();
			System.err.println("Client rejected, doesn't belong to this conversation");
		}
	}

	private void read(SelectionKey key) throws IOException {
		int bytesRead;
		SocketChannel clientRead = (SocketChannel)key.channel();
		readBuffer.clear();
		//read data from channel
		try {
			bytesRead = clientRead.read(readBuffer);
		} catch (IOException ioEx) {
			//exception caused by client breaking connection
			//cancel selection key, close channel
			System.err.println("Error occurred while reading data. Closing channel");
			System.err.println(ioEx.getMessage());
			key.cancel();
			if (client1.isOpen())
				client1.close();
			if (client2.isOpen())
				client2.close();
			isRunning = false;
			return;
		}

		if (bytesRead == -1) {
			//client ended conversation cleanly, server closing channel
			if (client1.isOpen())
				client1.close();
			if (client2.isOpen())
				client2.close();
			key.cancel();
			isRunning = false;

		} else if (bytesRead > 0) {

			if (clientRead == client1)
				clientRead = client2;
			else
				clientRead = client1;
			byte[] copyData = new byte[bytesRead];
			System.arraycopy(readBuffer.array(), 0, copyData, 0, bytesRead);
			send(clientRead, copyData);
		} else
			System.err.println("!!!! Client send 0 bytes"); //this should not happen
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();
		List queue = pendingData.get(clientSocket);

		ByteBuffer buffer;
		while (!queue.isEmpty()) {
			buffer = (ByteBuffer) queue.get(0);
			clientSocket.write(buffer);
			if (buffer.remaining() > 0) {
				System.err.println("!!!! Buffer was not emptied");
				break;
			}
			queue.remove(0);
		}

		if (queue.isEmpty())
			key.interestOps(SelectionKey.OP_READ);
	}

	private void send(SocketChannel client, byte[] data) {

		changeRequests.add(new ChangeRequest(client, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

		//pendingData.computeIfAbsent(client, value -> new ArrayList<>());
		//List<ByteBuffer> queue = pendingData.get(client);
		//queue.add(ByteBuffer.wrap(data));
		pendingData.computeIfAbsent(client, value -> new ArrayList<>()).add
				  (ByteBuffer.wrap(data));
	}

	private void openSocket() {
		try {
			handler = ServerSocketChannel.open();
			talkSelector = SelectorProvider.provider().openSelector();
			handler.configureBlocking(false);

			handler.socket().bind(new InetSocketAddress(handlerAddress, handlerPort));
			handler.register(talkSelector, SelectionKey.OP_ACCEPT);

			isRunning = true;
		} catch (IOException ioEx) {
			System.err.println("Problem occured while opening handler port");
			System.err.println(ioEx.getMessage());
		}
	}

	private void closeSocket() {
		try {
			if (handler.isOpen()) {
				handler.close();
				talkSelector.close();
			}
		} catch (IOException ioEx) {
			System.err.println("Problem occured while closing server socket");
			System.err.println(ioEx.getMessage());
		}
	}

	public boolean isRunning() { return isRunning; }

	public boolean isWaiting() { return isWaiting; }

	public void startHandler() {
		if (isWaiting) {
			isWaiting = false;
		}
	}

	public int getHandlerSocket() { return handlerPort; }
}

