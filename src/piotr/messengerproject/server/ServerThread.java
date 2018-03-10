package piotr.messengerproject.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerThread implements Runnable {

	private static final int CONV_MAX = 40;
	protected static final int BUFF_SIZE = 1024;
	private static final String C_ASK = "a";
	private static final String C_REFUSE = "n";
	private static final String C_ACCEPT = "y";
	private static final String C_CONFIRM = "c";
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final String appID = "MessageServerClient";

	private String hostName;
	private int listenPort;
	private ServerSocketChannel serverSocket;
	private Selector selector;
	//private boolean isRunning = false;
	private ByteBuffer readBuffer;
	private List<ChangeRequest> changeRequests;
	//mapowanie socketChannel clienta do listy instancji ByteBufforow
	private Map<SocketChannel, List<ByteBuffer>> pendingData;
	private Map<SocketChannel, ClientState> clientsState;
	private ExecutorService handlersExecutor;
	private ArrayList<String> clientsIDs;
	private ArrayList<SocketChannel> clientsChnnls;
	private List<ConversationPair> pendingPairs;
	private List<ConversationPair> activePairs;
	private List<HandleConversation> handlerWorkers;
	private List<Integer> handlersPorts;
	private ArrayBlockingQueue<ConversationEnd> handlersEndData;


	ServerThread(String hostName, int listenPort) {
		System.setProperty("sun.net.useExclusiveBind", "false");
		this.hostName = hostName;
		this.listenPort = listenPort;
		readBuffer = ByteBuffer.allocate(BUFF_SIZE);
		changeRequests = new LinkedList<>();//or maybe ArrayList<> should be used
		pendingData = new HashMap<>();
		clientsState = new HashMap<>();
		clientsIDs = new ArrayList<>();
		clientsChnnls = new ArrayList<>();
		pendingPairs = new ArrayList<>(); 	//or maybe LinkedList<> should be used
		activePairs = new ArrayList<>();		//or maybe LinkedList<> should be used
		handlerWorkers = new ArrayList<>();	//or maybe LinkedList<> should be used
		handlersPorts = new LinkedList<>();	//or maybe LinkedList<> should be used
		handlersEndData = new ArrayBlockingQueue<>(CONV_MAX*2);
		openSocket();
		handlersExecutor = Executors.newFixedThreadPool(CONV_MAX);
	}

	@Override
	public void run() {

		while (!Thread.interrupted()) {
			try {

				for (ChangeRequest change : changeRequests) {
					//if (change.type == ChangeRequest.CHANGEOPS) {
						SelectionKey selectionKey = change.socket.keyFor(selector);
						selectionKey.interestOps(change.ops);
					//}
				}
				changeRequests.clear();

				//clear leftovers from terminated conversation handler
				while (!handlersEndData.isEmpty()) {
					ConversationEnd data;
					try {
						data = handlersEndData.take();
						handlersPorts.remove(handlersPorts.indexOf(data.portNr));
						activePairs.remove(data.convPair);
						handlerWorkers.remove(data.worker);
					} catch (InterruptedException bqEx) {
						System.err.println("Problem occured while reading ArrayBlockingQueue");
						System.err.println(bqEx.getMessage());
					}
				}

				//iterating through set of keys which have available events
				selector.select();
				Iterator selectedKeys;
				SelectionKey key;
				selectedKeys = selector.selectedKeys().iterator();
				//while (selectedKeys.hasNext() && isRunning) {
				while (selectedKeys.hasNext()) {
					key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					//check for available event and handle it
					if (!key.isValid()) {
						System.err.println("Invalid key");
					} else if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}

			} catch (IOException ioEx) {
				System.err.println("Problem occured while listenint for events");
				System.out.println(ioEx.getMessage());
				//isRunning = false;
			}
		}

		System.out.println("Closing server");
		closeSocket();
		handlersExecutor.shutdown();
	}

	private void accept(SelectionKey key) throws IOException {

		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
		//accept incoming connection, set it in non-blocking mode
		SocketChannel newClient = serverSocketChannel.accept();
		newClient.configureBlocking(false);
		//register new client with selector, prepared for reading incoming data
		newClient.register(selector, SelectionKey.OP_READ);
		//System.out.println("Client accepted, waiting for data");
		clientsState.put(newClient, new ClientState());
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel clientRead = (SocketChannel)key.channel();
		ClientState state = clientsState.get(clientRead);

		//clear read buffer before accepting incoming data
		readBuffer.clear();
		int bytesRead;
		//try to read from client's channel
		try {
			bytesRead = clientRead.read(readBuffer);
		} catch (IOException ioEx) {
			//exception caused by client disconnecting 'unproperly'
			//cancel 'SelectionKey key' and close corresponding channel
			System.err.println("SERVER: Exception raised while reading data. Closing client connection");
			System.err.println(ioEx.getMessage());
			bytesRead = -1;
		}

		if (bytesRead <= 0) {
			//client ended connection clearly, server closes corresponding channel
			if (state.getState() > ClientState.WAITFORUID) {
				dropRegisteredClient(clientRead);
			}
			clientsState.remove(clientRead);
			key.cancel();
			clientRead.close();
		}

		readBuffer.flip();
		if (state.getState() == ClientState.WERIFYAPP) {
		//perform app veryfication
			byte[] dataCopy = new byte[appID.length()];
			System.arraycopy(readBuffer.array(), 0, dataCopy, 0, appID.length());

			if (!checkClientID(new String(dataCopy, CHARSET))) {
				//client hasnt provided proper ID
				//(invalid app) --> dumping connection
				clientRead.close();
				key.cancel();
				clientsState.remove(clientRead);
			} else {
				//change client state if application is valid
				state.setState(ClientState.WAITFORUID);
			}
		} else if (state.getState() == ClientState.WAITFORUID) {
			String clientData = new String(readBuffer.array(), CHARSET);
			String clientId = clientData.split(";")[0];

			//veryfication successful, returning list of connected clients
			//or -1 if provided login is already in use
			readBuffer.clear();
			if (clientsIDs.isEmpty()) {
				readBuffer.putInt(1);
				clientsIDs.add(clientId);
				clientsChnnls.add(clientRead);
			} else {

				readBuffer.putInt(clientsIDs.size() + 1);
				for (String str : clientsIDs) {
					if (str.equalsIgnoreCase(clientId)) {
						readBuffer.clear();
						readBuffer.putInt(-1);
						readBuffer.flip();
						send(clientRead, readBuffer.array());
						return;
					}
					readBuffer.put((str.concat(";")).getBytes(CHARSET));
				}
				readBuffer.put((clientId.concat(";")).getBytes(CHARSET));

				clientsIDs.add(clientId);
				clientsChnnls.add(clientRead);
			}

			//change client state from waiting for veryfication
			//into enabling conversations
			state.setState(ClientState.SERVECLIENT);
			//update all connected clients with information
			//about quantity and logins of active users
			readBuffer.flip();
			for (SocketChannel clnt : clientsChnnls) {
				send(clnt, readBuffer.array());
			}

		} else if (state.getState() == ClientState.SERVECLIENT) {
			//Veryfied client tries to create new conversation
			ConversationPair pair;
			String[] userData = (new String(readBuffer.array(), CHARSET)).split(";");

			//userData[0] holds info about type of message (ask for, refuse, confirm conversation)
			//userData[1] holds info about receiver of this request
			switch (userData[0]) {
				case C_ASK:
					//
					if (clientsIDs.contains(userData[1])) {
						SocketChannel askedChannel = clientsChnnls.get(clientsIDs.indexOf(userData[1]));
						String askingUser = clientsIDs.get(clientsChnnls.indexOf(clientRead));

						ConversationPair convPair = new ConversationPair(clientRead, askedChannel);

						readBuffer.clear();
						if (!pendingPairs.contains(convPair) && !activePairs.contains(convPair)) {
							//asked user available, no such conversation started
							pendingPairs.add(convPair);
							readBuffer.putInt(-10);
							readBuffer.put((askingUser.concat(";")).getBytes(CHARSET));
							readBuffer.flip();
							//forward this request to asked client
							send(askedChannel, readBuffer.array());
						} else {
							//conversation between this users has already started
							readBuffer.putInt(-20);
							readBuffer.put((userData[1].concat(";")).getBytes(CHARSET));
							readBuffer.flip();
							//send response about active conv
							send(clientRead, readBuffer.array());
						}
					}
					break;

				case C_REFUSE:
					//user refused, inform client sending request
					pair = new ConversationPair(clientRead,
							  clientsChnnls.get(clientsIDs.indexOf(userData[1])));

					pendingPairs.remove(pair);
					readBuffer.putInt(-30);
					readBuffer.put((userData[2] + ";").getBytes(CHARSET));
					readBuffer.flip();
					send(pair.client2, readBuffer.array());
					break;

				case C_ACCEPT:
					if (!clientsIDs.contains(userData[1])) {
						//this client is no longer available
						return;
					}
					pair = new ConversationPair(clientRead,
							  clientsChnnls.get(clientsIDs.indexOf(userData[1])));
					//conversation request accepted
					pendingPairs.remove(pair);
					activePairs.add(pair);

					if (handlerWorkers.size() < CONV_MAX) {

						//create new handler and send clients connection data
						//if limit of conversations was not reached
						Integer createPort = listenPort + 1;
						if (!handlersPorts.isEmpty()) {
							createPort = handlersPorts.get(handlersPorts.size() - 1) + 1;
						}
						handlersPorts.add(createPort);

						HandleConversation worker = new HandleConversation(hostName, createPort, handlersEndData, pair);
						handlerWorkers.add(worker);
						handlersExecutor.execute(worker);

						readBuffer.clear();
						readBuffer.putInt(-40);
						readBuffer.put((createPort + ";" + hostName + ";"
							  + userData[1] + ";" + userData[2] + ";").getBytes(CHARSET));
						readBuffer.flip();
						send(pair.client1, readBuffer.array());
						send(pair.client2, readBuffer.array());

					}
					break;

				case C_CONFIRM:
					int port = Integer.parseInt(userData[2]);

					for (HandleConversation obj : handlerWorkers) {
						if (obj.getHandlerSocket() == port) {
							if (obj.isWaiting()) {
								//System.out.println("Connection on port '" + port + "' confirmed, starting handler");
								obj.startHandler();
							} else if (!obj.isRunning()) {
								handlersPorts.remove(handlersPorts.indexOf(port));
								handlerWorkers.remove(obj);
							} //else {
							//System.out.println("Second client confirmed, handler is up and running");
							//}
							break;
						}
					}
			}
		}

	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();
		List<ByteBuffer> queue = pendingData.get(clientSocket);
		ByteBuffer buffer;


		while (!queue.isEmpty()) {
			buffer = queue.get(0);
			clientSocket.write(buffer);

			if (buffer.remaining() > 0) {
				System.err.println("!!!! buffer was not fully emptied !!!!");
				break;
			}
			queue.remove(0);
		}

		if (queue.isEmpty()) {
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	private void send(SocketChannel client, byte[] data) {

		ChangeRequest request = new ChangeRequest(client, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE);
		changeRequests.add(request);

		//pendingData.computeIfAbsent(client, value -> new ArrayList<>());
		//List<ByteBuffer> queue = pendingData.get(client);
		//queue.add(ByteBuffer.wrap(data));
		pendingData.computeIfAbsent(client, value -> new ArrayList<>()).add
				  (ByteBuffer.wrap(data));
	}

	private void openSocket() {
		try {
			serverSocket = ServerSocketChannel.open();
			selector = SelectorProvider.provider().openSelector();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(hostName, listenPort));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Starting listening on port " + listenPort);
			//isRunning = true;
		} catch (IOException ioEx) {
			System.out.println("Problem occured while opening listening port");
			System.out.println(ioEx.getMessage());
		}
	}

	private void closeSocket() {
		try {
			if (serverSocket.isOpen())
				serverSocket.close();
		} catch (IOException ioEx) {
			System.out.println("Problem occured while closing server socket");
			System.out.println(ioEx.getMessage());
		}
	}

	private boolean checkClientID(String clientData) {
		return appID.equals(clientData);
	}

	private void dropRegisteredClient(SocketChannel client) {

		//System.out.println("Removing verified client");
		clientsIDs.remove(clientsChnnls.indexOf(client));
		clientsChnnls.remove(client);

		readBuffer.clear();
		readBuffer.putInt(clientsIDs.size());
		if (clientsIDs.size() > 0) {
			for (String str : clientsIDs) {
				readBuffer.put((str.concat(";")).getBytes(CHARSET));
			}
		}

		for (SocketChannel channel : clientsChnnls) {
			send(channel, readBuffer.array());
		}

		for (ConversationPair pair : pendingPairs) {
			if (pair.client1 == client && pair.client2 == client)
				pendingPairs.remove(pair);
		}
	}

	/*public void stopServer() {
		System.out.println("Stopping server");
		//isRunning = false;
	}*/

	@Override
	public String toString() {
		return "ServerThread{" +
				  "CONV_MAX=" + CONV_MAX +
				  ", hostName='" + hostName + '\'' +
				  ", listenPort=" + listenPort +
				  ", serverSocket=" + serverSocket +
				  ", selector=" + selector +
				  ", readBuffer=" + readBuffer +
				  ", changeRequests=" + changeRequests +
				  ", pendingData=" + pendingData +
				  ", clientsState=" + clientsState +
				  ", handlersExecutor=" + handlersExecutor +
				  ", charset=" + CHARSET +
				  ", appID='" + appID + '\'' +
				  ", clientsIDs=" + clientsIDs +
				  ", clientsChs=" + clientsChnnls +
				  ", pendingPairs=" + pendingPairs +
				  ", activePairs=" + activePairs +
				  ", handlerWorkers=" + handlerWorkers +
				  ", handlersPorts=" + handlersPorts +
				  ", handlersEndData=" + handlersEndData +
				  '}';
	}
}

