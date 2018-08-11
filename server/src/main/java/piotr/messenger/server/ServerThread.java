package piotr.messenger.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;


public class ServerThread implements Runnable {


	private String hostName;
	private int listenPort;
	private ServerSocketChannel serverSocket;
	private Selector selector;
	//private boolean isRunning = false;
	//mapowanie socketChannel clienta do listy instancji ByteBufforow
	private Map<SocketChannel, List<ByteBuffer>> pendingData;
//    private Map<SocketChannel, ByteBuffer> pendingData;
	private Map<SocketChannel, ClientState> clientsState;
	private ExecutorService handlersExecutor;
    private Clients clients;
	private List<ConversationPair> pendingPairs;
	private List<ConversationPair> activePairs;
	private List<HandleConversation> handlerWorkers;
	private List<Integer> handlersPorts;
	private ArrayBlockingQueue<ConversationEnd> handlersEndData;

	private final Logger logger;


	ServerThread() {
		System.setProperty("sun.net.useExclusiveBind", "false");
		this.hostName = Constants.HOST_NAME;
		this.listenPort = Constants.PORT;
		pendingData = new HashMap<>();
		clientsState = new HashMap<>();
        clients = new Clients();
		pendingPairs = new ArrayList<>(); 	//or maybe LinkedList<> should be used
		activePairs = new ArrayList<>();		//or maybe LinkedList<> should be used
		handlerWorkers = new ArrayList<>();	//or maybe LinkedList<> should be used
		handlersPorts = new LinkedList<>();	//or maybe ArrayList<> should be used
		handlersEndData = new ArrayBlockingQueue<>(Constants.CONV_MAX*2);
        logger = LoggerFactory.getLogger(ServerThread.class);

		handlersExecutor = Executors.newFixedThreadPool(Constants.CONV_MAX);
	}

	@Override
	public void run() {

        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        openSocket();



		while (!Thread.interrupted()) {
			try {

				//clear leftovers from terminated conversation handler
				while (!handlersEndData.isEmpty()) {
                    ConversationEnd data = handlersEndData.take();
                    handlersPorts.remove(new Integer(data.portNr));
                    activePairs.remove(data.convPair);
                    handlerWorkers.remove(data.worker);
				}

				//iterating through set of keys which have available events
				selector.select();
				SelectionKey key;
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				//while (selectedKeys.hasNext() && isRunning) {
				while (selectedKeys.hasNext()) {
                    key = selectedKeys.next();
					selectedKeys.remove();

					//check for available event and handle it
					if (!key.isValid()) {
                        logger.error("INVALID KEY");
					} else if (key.isAcceptable()) {
//						logger.debug("new client");
						accept();
					} else if (key.isReadable()) {
//						logger.debug("reading from client");
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}

			} catch (IOException ioEx) {
                logger.error("Problem occured while listening for events - "
                        + ioEx.getMessage());
				//isRunning = false;
			} catch (InterruptedException abqEx) {
                logger.error("ArrayBlockingQueue error - " + abqEx.getMessage());
            } catch (CancelledKeyException cancelled) {
			    logger.error("Cancelled ({}).", cancelled.getMessage());
			    cancelled.printStackTrace();
			    break;
            }
		}

		logger.info("Closing server");
		closeSocket();
		handlersExecutor.shutdown();
	}

	private void accept(/*SelectionKey key*/) throws IOException {

//		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
		//accept incoming connection, set it in non-blocking mode
//		SocketChannel newClient = serverSocketChannel.accept();
        SocketChannel newClient = serverSocket.accept();
		newClient.configureBlocking(false);
		//register new client with selector, prepared for reading incoming data
		newClient.register(selector, SelectionKey.OP_READ);
//        logger.debug("Client accepted, waiting for data");
		clientsState.put(newClient, new ClientState());
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel clientRead = (SocketChannel)key.channel();
		ClientState state = clientsState.get(clientRead);

		//clear read buffer before accepting incoming data
        ByteBuffer readBuffer = ByteBuffer.allocate(Constants.BUFF_SIZE);
//		readBuffer.clear();
		int bytesRead = -1;
		//try to read from client's channel
		try {
			bytesRead = clientRead.read(readBuffer);
		} catch (IOException ioEx) {
			//exception caused by client disconnecting 'unproperly'
			//cancel 'SelectionKey key' and close corresponding channel
            logger.info("Unexpected end of connection ({}).", ioEx.getMessage());
		}

		if (bytesRead <= 0) {
			//client ended connection clearly, server closes corresponding channel
			if (state.getState() > ClientState.WAITFORUID) {
//				logger.debug("dropRegisteredClient");
				dropRegisteredClient(clientRead);
			}
			clientsState.remove(clientRead);
			key.cancel();
			clientRead.close();
			logger.debug("client dropping");
			return;
		}

//		logger.debug("reading proceeds");
		readBuffer.flip();
		if (state.getState() == ClientState.WERIFYAPP) {
//		    logger.debug("app verification");
		    //perform app veryfication
			byte[] dataCopy = new byte[Constants.APP_ID.length()];
			System.arraycopy(readBuffer.array(), 0, dataCopy, 0, Constants.APP_ID.length());

			if (!checkClientID(new String(dataCopy, Constants.CHARSET))) {
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
//		    logger.debug("client verification");
			String clientData = new String(readBuffer.array(), Constants.CHARSET);
			String clientId = clientData.split(";")[0];

			//veryfication successful, returning list of connected clients
			//or -1 if provided login is already in use

			readBuffer.clear();

			if (clients.hasUser(clientId)) {
			    readBuffer.putInt(-1);

            } else {
                readBuffer.putInt(0);
                clients.addUser(clientId, clientRead);
                //change client state from waiting for veryfication
                //into enabling conversations
                state.setState(ClientState.SERVECLIENT);
            }
            readBuffer.flip();
            send(clientRead, readBuffer.array());
            sendUserListToClients();

		} else if (state.getState() == ClientState.SERVECLIENT) {
			//Veryfied client tries to create new conversation
			ConversationPair pair;
			String[] userData = (new String(readBuffer.array(), Constants.CHARSET)).split(";");

			//userData[0] holds info about type of message (ask for, refuse, confirm conversation)
			//userData[1] holds info about receiver of this request
			switch (userData[0]) {
				case Constants.C_ASK:
					//
                    if (clients.hasUser(userData[1])) {
                        SocketChannel askedChannel = clients.getChannel(userData[1]);

                        String askingUser = clients.getUser(clientRead);

						ConversationPair convPair = new ConversationPair(clientRead, askedChannel);

						readBuffer.clear();
						if (!pendingPairs.contains(convPair) && !activePairs.contains(convPair)) {
							//asked user available, no such conversation started
							pendingPairs.add(convPair);
							readBuffer.putInt(-10);
							readBuffer.put((askingUser.concat(";")).getBytes(Constants.CHARSET));
							readBuffer.flip();
							//forward this request to asked client
							send(askedChannel, readBuffer.array());
						} else {
							//conversation between this users has already started
							readBuffer.putInt(-20);
							readBuffer.put((userData[1].concat(";")).getBytes(Constants.CHARSET));
							readBuffer.flip();
							//send response about active conv
							send(clientRead, readBuffer.array());
						}
					}
					break;

				case Constants.C_REFUSE:
					//user refused, inform client sending request
                    pair = new ConversationPair(clientRead, clients.getChannel(userData[1]));

					pendingPairs.remove(pair);
					readBuffer.putInt(-30);
					readBuffer.put((userData[2] + ";").getBytes(Constants.CHARSET));
					readBuffer.flip();
					send(pair.client2, readBuffer.array());
					break;

				case Constants.C_ACCEPT:
                    if (!clients.hasUser(userData[1])) {
						//this client is no longer available
						return;
					}
                    pair = new ConversationPair(clientRead, clients.getChannel(userData[1]));
					//conversation request accepted
					pendingPairs.remove(pair);
					activePairs.add(pair);

					if (handlerWorkers.size() < Constants.CONV_MAX) {

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
							  + userData[1] + ";" + userData[2] + ";").getBytes(Constants.CHARSET));
						readBuffer.flip();
						send(pair.client1, readBuffer.array());
						send(pair.client2, readBuffer.array());

					}
					break;

				case Constants.C_CONFIRM:
					int port = Integer.parseInt(userData[2]);

					for (HandleConversation obj : handlerWorkers) {
						if (obj.getHandlerSocket() == port) {
							if (obj.isWaiting()) {
                                //logger.debug("Connection on port '{}' confirmed, starting handler", port);
								obj.startHandler();
							} else if (!obj.isRunning()) {
                                handlersPorts.remove(new Integer(port));
								handlerWorkers.remove(obj);
							} //else {
                            //logger.debug("Second client confirmed, handler is up and running");
							//}
							break;
						}
					}
			}
		}

	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();

        for (ByteBuffer buff : pendingData.get(clientSocket)) {
            clientSocket.write(buff);
        }
        pendingData.get(clientSocket).clear();


//        if (buffer.remaining() > 0) {
//            logger.error("!!! buffer was not fully emptied !!!");
//        }
        key.interestOps(SelectionKey.OP_READ);
	}

	private void send(SocketChannel client, byte[] data) {

//		pendingData.computeIfAbsent(client, value -> new ArrayList<>()).add(ByteBuffer.wrap(data));
        pendingData.computeIfAbsent(client, value -> new ArrayList<>());
        pendingData.get(client).add(ByteBuffer.wrap(data));


        SelectionKey selectionKey = client.keyFor(selector);
        selectionKey.interestOps(SelectionKey.OP_WRITE);

	}

	private void sendUserListToClients() {
	    ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFF_SIZE);
        StringBuilder usersList = new StringBuilder();
        for (String str : clients.getUsers()) {
            usersList.append(str);
            usersList.append(';');
        }
        buffer.putInt(clients.usersCount());
        logger.debug("READ count: {}, list: {}", clients.usersCount(), usersList);
        buffer.put(usersList.toString().getBytes(Constants.CHARSET));

        //update all connected clients with information
        //about quantity and logins of active users
        buffer.flip();
        for (SocketChannel clnt : clients.getChannels()) {
            send(clnt, buffer.array());
        }
    }

	private void openSocket() {
		try {
			serverSocket = ServerSocketChannel.open();
			selector = SelectorProvider.provider().openSelector();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(hostName, listenPort));
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Starting server, port in use: {}.", listenPort);
			//isRunning = true;
		} catch (IOException ioEx) {
            logger.error("Problem occured while opening listening port - ({}).", ioEx.getMessage());
            Thread.currentThread().interrupt();
		}
	}

	private void closeSocket() {
		try {
			if (serverSocket.isOpen())
				serverSocket.close();
		} catch (IOException ioEx) {
            logger.error("Problem occured while closing server socket - ({}).", ioEx.getMessage());
		}
	}

	private boolean checkClientID(String clientData) {
		return Constants.APP_ID.equals(clientData);
	}

	private void dropRegisteredClient(SocketChannel client) {

        clients.dropUser(client);
        sendUserListToClients();

		for (ConversationPair pair : pendingPairs) {
			if (pair.client1 == client && pair.client2 == client) {
			    logger.debug("SAME CLIENTS");
                pendingPairs.remove(pair);
            }
		}
	}

	/*public void stopServer() {
		System.out.println("Stopping server");
		//isRunning = false;
	}*/

}

