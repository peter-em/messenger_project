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
	private static final int BUFF_SIZE = 1024;
	private static final String C_ASK = "a";
	private static final String C_REFUSE = "n";
	private static final String C_ACCEPT = "y";
	private static final String C_CONFIRM = "c";
	private static final Charset charset = Charset.forName("UTF-8");
	private static final String appID = "PMateuszMessageServerClient";

	private String hostName;
	private int listenPort;
	private ServerSocketChannel serverSocket;
	private Selector selector;
	private boolean isRunning = false;
	private ByteBuffer readBuffer;
	//lista instancji ChangeRequest
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
	private ArrayBlockingQueue handlersEndData;


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
		handlersPorts = new ArrayList<>();	//or maybe LinkedList<> should be used
		handlersEndData = new ArrayBlockingQueue(CONV_MAX*2);
		openSocket();
		handlersExecutor = Executors.newFixedThreadPool(CONV_MAX);
	}

	@Override
	public void run() {

		while (!Thread.interrupted()) {
			try {

				//int countChanges = 0;
				for (ChangeRequest change : changeRequests) {
					//countChanges++;
					//if (change.type == ChangeRequest.CHANGEOPS) {
						SelectionKey selectionKey = change.socket.keyFor(selector);
						selectionKey.interestOps(change.ops);
					//}
				}

				//System.err.println("CountChanges: " + countChanges);
				changeRequests.clear();

				//clear leftovers from terminated conversation handler
				while (!handlersEndData.isEmpty()) {
					ConversationEnd data;
					try {
						data = (ConversationEnd)handlersEndData.take();
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
				while (selectedKeys.hasNext() && isRunning) {
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
				isRunning = false;
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
		//System.out.println("Client count: " + clientCount);
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

			if (state.getState() > ClientState.WAITFORUID) {
				dropRegisteredClient(clientRead);
			}
			clientsState.remove(clientRead);
			key.cancel();
			clientRead.close();
			return;
		}

		if (bytesRead == -1) {
			//client ended connection clearly, server closes corresponding channel
			if (state.getState() > ClientState.WAITFORUID) {
				dropRegisteredClient(clientRead);
			}
			clientsState.remove(clientRead);
			key.cancel();
			clientRead.close();
			return;
		}

		if (bytesRead > 0) {
			readBuffer.flip();
			if (state.getState() == ClientState.WERIFYAPP) {
			//perform app veryfication
				byte[] dataCopy = new byte[appID.length()];
				System.arraycopy(readBuffer.array(), 0, dataCopy, 0, appID.length());

				if (!checkClientID(new String(dataCopy, charset))) {
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
				String clientData = new String(readBuffer.array(), charset);
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
						readBuffer.put((str.concat(";")).getBytes(charset));
					}
					readBuffer.put((clientId.concat(";")).getBytes(charset));

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
				String[] userData = (new String(readBuffer.array(), charset)).split(";");

				//userData[0] holds info about type of message (ask for, refuse, confirm conversation)
				//userData[1] holds info about user that this client wants to talk
				switch (userData[0]) {
					case C_ASK:
						//
						if (clientsIDs.contains(userData[1])) {
							//state.setConvUser(inputData[0]);
							SocketChannel askedChannel = clientsChnnls.get(clientsIDs.indexOf(userData[1]));
							ClientState askedState = clientsState.get(askedChannel);
							String askingUser = clientsIDs.get(clientsChnnls.indexOf(clientRead));

							ConversationPair convPair = new ConversationPair(clientRead, askedChannel);

							readBuffer.clear();
							//System.out.println("askingUser: " + askingUser);
							//System.out.println("askedUser: " + askedUser[0]);
							if (!pendingPairs.contains(convPair) && !activePairs.contains(convPair)) {
								//odpowiedz jesli rozmowca dostepny
								//i nie ma jeszcze takiej rozmowy
								pendingPairs.add(convPair);
								//askedState.setState(ClientState.ASKFORCONV);
								//System.out.println("Nie ma takiej rozmowy");
								//System.out.println("Liczba rozmów: " + (pendingPairs.size() + activePairs.size()));
								readBuffer.putInt(-10);
								readBuffer.put((askingUser.concat(";")).getBytes(charset));
								readBuffer.flip();
								send(askedChannel, readBuffer.array());
							} else {
								//odpowiedz jesli dana rozmowa juz rozpoczeta
								//System.out.println("Taka rozmowa istnieje");
								readBuffer.putInt(-20);
								readBuffer.put((userData[1].concat(";")).getBytes(charset));
								readBuffer.flip();
								//informacja zwrotna do klienta ktory wyslal zapytanie
								send(clientRead, readBuffer.array());
							}

						}
						break;

					case C_REFUSE:
						pair = new ConversationPair(clientRead,
								  clientsChnnls.get(clientsIDs.indexOf(userData[1])));
						//System.out.println("Odmowa konwersacji.\nLiczba oczekujacych rozmów przed kasowaniem: " + pendingPairs.size());

						pendingPairs.remove(pair);
						//System.out.println("Liczba rozmoów po kasowaniu: " + pendingPairs.size());

						readBuffer.putInt(-30);
						readBuffer.put((userData[2] + ";").getBytes(charset));
						readBuffer.flip();
						send(pair.client2, readBuffer.array());
						break;

					case C_ACCEPT:
						if (!clientsIDs.contains(userData[1])) {
							//System.out.println("^%$#%#^ Nie ma już takiego użytkownia");
							return;
						}
						pair = new ConversationPair(clientRead,
								  clientsChnnls.get(clientsIDs.indexOf(userData[1])));
						//System.out.println("Rozmowa z " + userData[1] + "przyjeta");
						pendingPairs.remove(pair);
						activePairs.add(pair);
						//System.out.println("Liczba aktywnych rozmów: " + activePairs.size());

						if (handlerWorkers.size() < CONV_MAX) {

							Integer createPort = listenPort + 1;
							//System.out.println("Rozmiar handlers: " + handlersPorts.size());
							for (Integer prt : handlersPorts) {
								if (prt.equals(createPort))
									createPort++;
							}
							//System.out.println("Numer portu: " + createPort);
							handlersPorts.add(createPort);
							//System.out.println("^ ^ ^ Main: Tworzenie nowego handlera na porcie " + createPort);

							//System.out.println("Liczba handlerow: " + handlerWorkers.size());
							HandleConversation worker = new HandleConversation(hostName, createPort, handlersEndData, pair);
							handlerWorkers.add(worker);
							handlersExecutor.execute(worker);

							readBuffer.clear();
							readBuffer.putInt(-40);
							readBuffer.put((createPort + ";" + hostName + ";"
								  + userData[1] + ";" + userData[2] + ";").getBytes(charset));
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
									//System.out.println("Potwierdzono połączenie na porcie " + port + ", uruchamiam handler");
									obj.startHandler();
								} else if (!obj.isRunning()) {

									handlersPorts.remove(handlersPorts.indexOf(port));
									handlerWorkers.remove(obj);
								} /*else {
								//System.out.println("Drugi client potwierdził, " + "handler już uruchomiony");
								}*/
							}
						}

				}

			}
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();
		//synchronized (pendingData) {
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
			//sKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			//sKey.attach()

			System.out.println("Starting listening on port " + listenPort);
			isRunning = true;
		} catch (IOException ioEx) {
			System.out.println("Problem occured while opening listening port");
			System.out.println(ioEx.getMessage());
		}
	}

	private void closeSocket() {
		try {
			//executor.shutdown();
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
				readBuffer.put((str.concat(";")).getBytes(charset));
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

	public void stopServer() {
		System.out.println("Stopping server");
		isRunning = false;
	}

	@Override
	public String toString() {
		return "ServerThread{" +
				  "CONV_MAX=" + CONV_MAX +
				  ", hostName='" + hostName + '\'' +
				  ", listenPort=" + listenPort +
				  ", serverSocket=" + serverSocket +
				  ", selector=" + selector +
				  ", isRunning=" + isRunning +
				  ", readBuffer=" + readBuffer +
				  ", changeRequests=" + changeRequests +
				  ", pendingData=" + pendingData +
				  ", clientsState=" + clientsState +
				  ", handlersExecutor=" + handlersExecutor +
				  ", charset=" + charset +
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

