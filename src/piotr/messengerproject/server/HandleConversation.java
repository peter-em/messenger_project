package piotr.messengerproject.server;

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
	private byte[] rawData;
	private byte[] copyData;
	private int clientCount;
	private volatile boolean isRunning;
	private volatile boolean isWaiting;
	private boolean stopWorker;
	private List changeRequests = new LinkedList();
	private Map pendingData = new HashMap();
	private ArrayBlockingQueue handlersEndData;
	private ConversationPair convPair;

	HandleConversation(String address, int port,
									  ArrayBlockingQueue queue, ConversationPair pair) {
		handlerAddress = address;
		handlerPort = port;
		handlersEndData = queue;
		convPair = pair;
		readBuffer = ByteBuffer.allocate(2048);
		clientCount = 0;
		isRunning = false;
		isWaiting = true;
		stopWorker = false;
		openSocket();
	}

	@Override
	public void run() {

		//System.out.println(Thread.currentThread().getName() + " czekam.");

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
			//System.out.println("Handler " + Thread.currentThread().getName() + " startuje");
			try {
				Iterator changes = changeRequests.iterator();
				while (changes.hasNext()) {
					ChangeRequest change = (ChangeRequest) changes.next();
					switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey swKey = change.socket.keyFor(talkSelector);
							swKey.interestOps(change.ops);
					}
				}
				changeRequests.clear();
				talkSelector.select();

				//przejscie po zbiorze kluczy dla ktorych dostepne sa zdarzenia
				selectedKeys = talkSelector.selectedKeys().iterator();
				while (selectedKeys.hasNext() && isRunning) {
					//System.out.println("jest kolejny klucz");
					key = (SelectionKey)selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
						continue;

					//sprawdzanie jakie zdarzenie jest dostepne oraz jego obsluga
					if (key.isAcceptable()) {
						//System.out.println("\n##### nowy client #####");
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
		//System.out.println("accept dla serverSocketu: " + serverSocketChannel.getLocalAddress());

		//akceptowanie polaczenia, ustawianie go w trybie non-blocking
		if (clientCount < 2) {
			SocketChannel newClient = serverSocketChannel.accept();
			if (clientCount == 0)
				client1 = newClient;
			else
				client2 = newClient;
			clientCount++;

			//Socket socket = client.socket();
			newClient.configureBlocking(false);
			if (clientCount == 2) {
				client1.register(talkSelector, SelectionKey.OP_READ);
				client2.register(talkSelector, SelectionKey.OP_READ);
			}
		} else {
			SocketChannel cancelCh = serverSocketChannel.accept();
			cancelCh.close();
			System.err.println("Klient odrzucony, rozmowa prywatna");
		}
	}

	private void read(SelectionKey key) throws IOException {
		int bytesRead;
		//System.out.println("+++++++ czytam w handlerThread");
		//System.out.println("wywoluje read: " + Thread.currentThread().getName());
		SocketChannel clientRead = (SocketChannel)key.channel();
		//czyszczenie bufora odczytu przed przyjeciem nowych danych
		readBuffer.clear();
		//proba odczytania danych z kanalu

		try {
			bytesRead = clientRead.read(readBuffer);
		} catch (IOException ioEx) {
			//blad spowodowany zerwaniem polaczenia przez clienta
			//anulowanie wybranego klucza (selecion key) i zamkniecie kanalu
			System.err.println("Błąd podczas odczytywania danych z kanału. Zamykam połącznie");
			System.out.println(ioEx.getMessage());
			key.cancel();
			if (client1.isOpen())
				client1.close();
			if (client2.isOpen())
				client2.close();
			isRunning = false;
			return;
		}

		if (bytesRead == -1) {
			//client czysto zakonczyl polaczenie, serwer rowniez zamyka kanal
			if (client1.isOpen())
				client1.close();
			if (client2.isOpen())
				client2.close();
			key.cancel();
			isRunning = false;
			//System.out.println("Koniec rozmowy, jeden z klientów zakończył połączenie\n");
			//return;

		} else if (bytesRead == 0) {
			System.err.println("$$$ ---- Klient wyslal 0 bajtow"); //malo prawdopodobna sytuacja
		} else if (bytesRead > 0) {

			if (clientRead == client1)
				clientRead = client2;
			else
				clientRead = client1;
			//data = readBuffer.array();
			copyData = new byte[bytesRead];
			System.arraycopy(readBuffer.array(), 0, copyData, 0, bytesRead);
			send(clientRead, copyData);
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel clientSocket = (SocketChannel) key.channel();
		List queue = (List) pendingData.get(clientSocket);

		ByteBuffer buffer;
		while (!queue.isEmpty()) {
			buffer = (ByteBuffer) queue.get(0);
			//System.out.println("%%% buffer: " + buffer.remaining());
			clientSocket.write(buffer);
			if (buffer.remaining() > 0) {
				System.out.println("!!!! bufor nie zostal oprozniony");
				break;
			}
			queue.remove(0);
		}

		if (queue.isEmpty())
			key.interestOps(SelectionKey.OP_READ);
	}

	public void send(SocketChannel client, byte[] data) {

		changeRequests.add(new ChangeRequest(client, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		List queue = (List)pendingData.get(client);
		if (queue == null) {
			queue = new ArrayList();
			pendingData.put(client, queue);
		}
		queue.add(ByteBuffer.wrap(data));
	}

	private void openSocket() {
		try {
			handler = ServerSocketChannel.open();
			talkSelector = SelectorProvider.provider().openSelector();
			handler.configureBlocking(false);

			handler.socket().bind(new InetSocketAddress(handlerAddress, handlerPort));
			handler.register(talkSelector, SelectionKey.OP_ACCEPT);

			//System.out.println("Starting conversation handler on port: " + handlerPort);
			isRunning = true;
		} catch (IOException ioEx) {
			System.err.println("Problem occured while opening handler port");
			System.out.println(ioEx.getMessage());
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
			System.out.println(ioEx.getMessage());
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isWaiting() {
		return isWaiting;
	}

	public void startHandler() {
		if (isWaiting) {
			isWaiting = false;
		}
	}

	public int getHandlerSocket() {
		return handlerPort;
	}
}

