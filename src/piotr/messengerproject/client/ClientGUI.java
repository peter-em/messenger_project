package piotr.messengerproject.client;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class ClientGUI extends JFrame implements ActionListener, WindowListener {
	private static final String hostName = "127.0.0.1";
	private static final int portNr = 6125;
	private static final int bufferSize = 1024;
	private static final String clientPattern = "PMateuszMessageServerClient";
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final int BLOCKING_SIZE = 128;

	private JPanel rootPanel;
	private JToolBar buttonsMenuBar;
	private JButton closeTab;
	private JLabel usersCount;
	private JTabbedPane appPages;
	private JTextField chooseUser;
	private JButton sendRequest;
	private JList usersList;
	private JPanel mainClient;
	private JLabel ownerName;

	private static final Color textAreaColor = new Color(84, 88, 90);
	private static final Color textColor = new Color(242,242,242);
	private DefaultListModel defListModel;
	private ArrayBlockingQueue<String> mainDataQueue;
	private Map<String, ArrayBlockingQueue> readThreads;
	private Map<String, ArrayBlockingQueue> writeThreads;
	private Map<String, JTextArea> printAreas;
	private Map<String, JTextArea> writeAreas;


	ClientGUI() {
		setTitle("Chat-o-Matt");

		HandleWorkload worker = new HandleWorkload();

		initComponents();
		addListeneres();
		pack();

		Thread task = new Thread(worker);
		task.start();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		SwingUtilities.updateComponentTreeUI(this);

		setMinimumSize(new Dimension(290, 500));
		setPreferredSize(new Dimension(300, 500));
		setMaximumSize(new Dimension(360, 650));

		centerFrame();
		setContentPane(rootPanel);


		usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersList.setLayoutOrientation(JList.VERTICAL);
		defListModel = new DefaultListModel();
		usersList.setModel(defListModel);

		usersCount.setText("Active users: " + defListModel.size());

		readThreads = new HashMap<>();
		writeThreads = new HashMap<>();
		printAreas = new HashMap<>();
		writeAreas = new HashMap<>();

		//for windows OS
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);

	}

	private void addListeneres() {

		closeTab.addActionListener(this);
		sendRequest.addActionListener(this);

		addWindowListener(this);

		usersList.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
			ListSelectionModel lsm = (ListSelectionModel) event.getSource();
			if (!lsm.getValueIsAdjusting()) {
				if (lsm.isSelectionEmpty()) {
					lsm.clearSelection();
					chooseUser.setText("");
				} else {
					String user = (String) defListModel.getElementAt(lsm.getLeadSelectionIndex());
					chooseUser.setText(user);
				}
			}
		});

		appPages.addChangeListener(new TabChangeListener());
	}


	//------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - BEGIN ------------//
	//handle closing ('X' button or Alt+F4)
	public void windowClosing(WindowEvent we) {
		int nOption;
		int tabCount = appPages.getTabCount();
		if (tabCount > 1) {

			nOption = JOptionPane.showConfirmDialog(rootPanel, "You have open conversations: " +
								 (tabCount - 1) + "\nClose anyway?", "Closing Programm",
					  JOptionPane.OK_CANCEL_OPTION);

			if (nOption == JOptionPane.CANCEL_OPTION || nOption == JOptionPane.CLOSED_OPTION)
				return;

		}
		mainDataQueue.add("q;");
		setVisible(false);
	}

	public void windowClosed(WindowEvent we) {}

	public void windowDeiconified(WindowEvent we) {}

	public void windowIconified(WindowEvent we) {}

	public void windowOpened(WindowEvent we) {}

	public void windowActivated(WindowEvent we) {}

	public void windowDeactivated(WindowEvent we) {}
	//------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - END ------------//


	//handle clickable buttons
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == sendRequest) {
			//send conversation request button
			String user = chooseUser.getText();
			if (user.length() > 0) {
				mainDataQueue.add("a;" + user + ";");
			}

		} else if (e.getSource() == closeTab) {
			//close conversation button
			int idx = appPages.getSelectedIndex();
			if (idx > 0) {
				String tabName = appPages.getTitleAt(idx);
				mainDataQueue.add("t;" + tabName + ";");
				appPages.removeTabAt(idx);

			}
		}
	}

	//worker thread - manages application data
	private class HandleWorkload implements Runnable {

		@Override
		public void run() {

			ByteBuffer buffer;
			try (SocketChannel channel = SocketChannel.open()) {
				channel.configureBlocking(true);
				channel.connect(new InetSocketAddress(hostName, portNr));

				int timeOut = 0;
				while (!channel.finishConnect()) {
					if (timeOut++ > 100)
						return;
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException itrEx) {
						itrEx.printStackTrace();
					}
				}

				buffer = ByteBuffer.allocate(bufferSize);
				System.out.println("Sending certificate.");
				buffer.clear();
				buffer.put(clientPattern.getBytes(CHARSET));
				buffer.flip();
				channel.write(buffer);
				String userName = "";

				int response = -1;
				while (response == -1) {

					while (true) {
						userName = JOptionPane.showInputDialog(rootPanel, "Enter Your login to start,\n" +
								  "or press 'Cancel' to exit app.\nLogin must contain " +
								  "at least 3 characters.", "Enter Login", JOptionPane.INFORMATION_MESSAGE);
						if (userName == null) {
							setVisible(false);
							dispose();
							return;
						} else if (userName.length() > 2) {
							break;
						}
					}

					buffer.clear();
					buffer.put((userName + ";").getBytes(CHARSET));
					buffer.flip();
					channel.write(buffer);

					buffer.clear();
					channel.read(buffer);
					buffer.flip();
					response = buffer.getInt();

					if (response == -1)
						JOptionPane.showMessageDialog(rootPanel, "This login is already in use,\n",
							  "Login error", JOptionPane.INFORMATION_MESSAGE);
				}


				//reveal window app if verification was succesful
				mainDataQueue = new ArrayBlockingQueue<>(BLOCKING_SIZE);
				ownerName.setText(userName);
				setVisible(true);

				// wyswietlanie listy uzytkownikow
				//
				boolean clientsUpdate = true;
				boolean awaitingConv = false;
				boolean sendOK;
				int bytesRead;
				String[] inputArray;
				String input;
				String usersData;
				Calendar calendar;
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				List<String> convUsers = new LinkedList<>();
				List<String> removingConvs = new ArrayList<>();
				List<String> clientsNames = new LinkedList<>();
				channel.configureBlocking(false);

				buffer.compact();
				while (true) {

					if (clientsUpdate) {
						//print active users
						if (response == 1) {
							//no other users
							if (defListModel.size() != 0) {
								defListModel.removeAllElements();
								usersCount.setText("Active users: " + defListModel.size());
							}
						} else {
							usersData = new String(buffer.array(), CHARSET);
							String[] users = usersData.split(";");

							clientsNames.clear();
							for (int i = 0; i < response; i++) {
								if (userName.equals(users[i]))
									continue;
								clientsNames.add(users[i]);
							}
							Collections.sort(clientsNames);
							if (defListModel.size() != 0) {
								defListModel.removeAllElements();
							}
							for (String str : clientsNames) {
								defListModel.addElement(str);
							}
							usersCount.setText("Active users: " + defListModel.size());
						}
						clientsUpdate = false;
					}

					buffer.clear();
					bytesRead = channel.read(buffer);
					if (bytesRead == -1) {
						System.out.println("STOP. Server closed the connection");
						break;
					}

					if (bytesRead != 0) {
						buffer.flip();
						response = buffer.getInt();
						buffer.compact();

						if (response > 0) {
							//response greater than 0 means update active clients list
							clientsUpdate = true;
							continue;
						} else if (response == -10) {
							//another user wants to talk
							//convUser[0] holds his login
							usersData = new String(buffer.array(), CHARSET);
							String[] convUser = usersData.split(";");

							new Thread(new DialogsHandler(DialogsHandler.CONV_REQUEST, convUser[0])).start();

							awaitingConv = true;
							convUsers.add(convUser[0]);

						} else if (response == -20) {
							//System.out.println("Taka rozmowa już się rozpoczęła");
							usersData = new String(buffer.array(), CHARSET);
							new Thread(new DialogsHandler(DialogsHandler.CONV_STARTED, usersData.split(";")[0])).start();

						} else if (response == -30) {

							usersData = new String(buffer.array(), CHARSET);

							//System.out.println("Wybrany użytkownik " + usersData.split(";")[0] + " odmówił rozmowy");
							new Thread(new DialogsHandler(DialogsHandler.CONV_REFUSED, usersData.split(";")[0])).start();
						} else if (response == -40) {

							//odebranie odpowiedzi o zaakceptowaniu rozmowy
							usersData = new String(buffer.array(), CHARSET);
							String[] connectData = usersData.split(";");


							String starConvUser = connectData[2].equals(userName)?connectData[3]:connectData[2];
							//System.out.println("Uzytkownik " + starConvUser + " zgodzil sie na rozmowe");

							//System.out.println("Laczenie z handlerem na adresie "
							// + connectData[1] + " i porcie " + connectData[0]);
							buffer.clear();
							buffer.put(("c;" + userName + ";" + connectData[0] + ";").getBytes(CHARSET));
							buffer.flip();
							channel.write(buffer);

							if (doConnect(connectData[1], Integer.parseInt(connectData[0]), starConvUser)) {
								createConvPage(starConvUser);
							}
						}

					}

					if (!mainDataQueue.isEmpty()) {

						try {
							input = mainDataQueue.take();
							sendOK = true;
						} catch (InterruptedException bqEx) {
							System.out.println(bqEx.getMessage());
							input = "";
							sendOK = false;
						}

						buffer.clear();
						if (input.length() > 0) {
							inputArray = input.split(";");

							if (inputArray[0].equals("q")) {
								break;
							} else if (inputArray[0].equals("y") && awaitingConv) {
								//wysylanie informacji zwrotnej o przyjeciu rozmowy
								buffer.put(("y;" + inputArray[1] + ";" + userName + ";").getBytes(CHARSET));
								convUsers.remove(inputArray[1]);
								if (convUsers.isEmpty())
									awaitingConv = false;
							} else if (inputArray[0].equals("n") && awaitingConv) {
								//wysylanie informacji zwrotnej o odrzuceniu rozmowy
								buffer.put(("n;" + inputArray[1] + ";" + userName + ";").getBytes(CHARSET));
								convUsers.remove(inputArray[1]);
								if (convUsers.isEmpty())
									awaitingConv = false;
							} else if (inputArray[0].equals("a") && clientsNames.contains(inputArray[1])) {
								//wysylanie zapytania o nowa rozmowe
								//System.out.println("Wysyłam zapytanie, input: " + inputArray[1]);
								buffer.put(("a;" + inputArray[1] + ";").getBytes(CHARSET));
							} else if (inputArray[0].equals("s")) {
								//kopiowanie tekstu clienta w danej rozmowie do okna wynikowego
								//System.out.println("Kopiuje tekst rozmowy " + inputArray[1]);
								JTextArea tmpPrint = printAreas.get(inputArray[1]);
								calendar = Calendar.getInstance();
								tmpPrint.append("ja, " + dateFormat.format(calendar.getTime()) + "\n");
								tmpPrint.append(inputArray[2] + "\n\n");
								tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());

								//wyslanie danych do wlasciwego writera (rozmowcy)
								writeThreads.get(inputArray[1]).add("g;" + inputArray[2]);

							} else if (inputArray[0].equals("t")) {

								//przesłanie impulsu zakonczenia rozmowy do writera
								writeThreads.get(inputArray[1]).add("s;");
								//usuniecie zmapowanych z rozmowa referencji do BlockingQueues i JTextAreas
								removeMapings(inputArray[1]);

							} else
								continue;
						}
						if (sendOK) {
							buffer.flip();
							channel.write(buffer);
						}
					}

					//przegladniecie kolejek czy sa dane od readerow do wyswietlenia
					for (String convKey : readThreads.keySet()) {
						ArrayBlockingQueue queue = readThreads.get(convKey);
						if (!queue.isEmpty()) {
							try {
								input = (String)queue.take();
							} catch (InterruptedException bqEx) {
								System.out.println(bqEx.getMessage());
								continue;
							}

							//specjalna sytuacja, przez blokade wysylania pustych stringow
							//do rozmowcy, jedyna mozliwosc zerowego stringa pochodzi
							//od danego readera rozmowy i informuje o jej zakonczeniu
							if (input.length() == 0) {
								for (int i = 1; i < appPages.getTabCount(); i++) {
									if (appPages.getTitleAt(i).equals(convKey)) {
										appPages.removeTabAt(i);
										break;
									}
								}
								removingConvs.add(convKey);

							} else {

								JTextArea tmpPrint = printAreas.get(convKey);
								calendar = Calendar.getInstance();
								tmpPrint.append(convKey + ", " + dateFormat.format(calendar.getTime()) + "\n");
								tmpPrint.append(input + "\n\n");
								tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());
							}
						}
					}

					//usuwanie mapowania dla zakonczonych rozmoww
					if (!removingConvs.isEmpty()) {
						for (String str : removingConvs) {
							removeMapings(str);
						}
						removingConvs.clear();
					}

					buffer.clear();
					try {
						TimeUnit.MILLISECONDS.sleep(24);
					} catch (InterruptedException itrEx) {
						itrEx.printStackTrace();
					}
				}
				//wyslanie informacji do wszystkich istniejacych rozmow o zamknieciu polaczen
				//^ ^ ^ ^ to chyba juz nie potrzebne

			} catch (IOException ioEx) {
				System.err.println("Connection problem.");
				System.err.println(ioEx.getMessage());
			}
			performSafeClose();
		}

		//removing blocking queues mapped to conversation after closing it
		private void removeMapings(String convUser) {
			writeThreads.remove(convUser);
			readThreads.remove(convUser);
			writeAreas.remove(convUser);
			printAreas.remove(convUser);
		}

		private void performSafeClose() {

			for (ArrayBlockingQueue queue : writeThreads.values()) {
				queue.add("s;");
			}
			setVisible(false);
			dispose();
		}
	}

	//klasa do wyswietlania okienek z informacjami
	private class DialogsHandler implements Runnable {

		public static final int CONV_STARTED = 1;
		public static final int CONV_REQUEST = 2;
		public static final int CONV_REFUSED = 3;
		private String convUser;
		private int dialogType;

		DialogsHandler(int dialog, String convUser) {
			dialogType = dialog;
			this.convUser = convUser;
		}

		@Override
		public void run() {
			if (dialogType == 1) {
				JOptionPane.showMessageDialog(rootPanel, "Conversation with user " + convUser
						  + "\nhas already started", "Request Refused", JOptionPane.INFORMATION_MESSAGE);
			} else if (dialogType == 2) {
				int answr = JOptionPane.showConfirmDialog(rootPanel, "User '" + convUser + "' wants to talk,\n"
						  + "Do You agree?", "New Conversation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (answr == JOptionPane.NO_OPTION || answr == JOptionPane.CLOSED_OPTION) {
					System.out.println("Odrzucono rozmowę");
					mainDataQueue.add(new String("n;" + convUser + ";"));
				} else {
					System.out.println("Zaakceptowano rozmowe");
					mainDataQueue.add(new String("y;" + convUser + ";"));
				}
			} else if (dialogType == 3) {
				JOptionPane.showMessageDialog(rootPanel, "User '" + convUser + "' refused conversation",
						  "Conversation canceled", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	//nadpisana klasa do obslugi eventow klawiatury
	private class WriterListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {

			if (e.getKeyCode() == KeyEvent.VK_ENTER) {

				e.consume();
				int idx = appPages.getSelectedIndex();
				if (idx > 0) {
					//System.out.println("Indeks: " + idx);
					String tabName = appPages.getTitleAt(idx);
					JTextArea tmpWrite = writeAreas.get(tabName);
					if (tmpWrite.getText().length() != 0) {

						if (e.getModifiers() == InputEvent.SHIFT_MASK) {
							tmpWrite.append("\n");
							return;
						}

						try {
							mainDataQueue.put("s;" + tabName + ";" + tmpWrite.getText() + ";");
							tmpWrite.setText("");
						} catch (InterruptedException itrEx) {
							System.err.println(itrEx.getMessage());
						}

					}
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

	private class TabChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {

			int idx = appPages.getSelectedIndex();
			if (idx > 0) {
				String tabName = appPages.getTitleAt(idx);
				writeAreas.get(tabName).requestFocus();
			}

		}
	}

	//metoda tworzaca nowa karte jezeli rozmowa potwierdzona
	private void createConvPage(String convUser) {

		JTextArea printArea = new JTextArea();
		printArea.setFont(new Font("Consolas", 0, 13));
		JTextArea writeArea = new JTextArea();
		writeArea.setFont(new Font("Consolas", 0, 13));
		printArea.setEditable(false);
		printArea.setWrapStyleWord(true);
		printArea.setLineWrap(true);
		writeArea.setWrapStyleWord(true);
		writeArea.setLineWrap(true);
		printArea.setBackground(textAreaColor);
		writeArea.setBackground(textAreaColor);
		writeArea.setCaretColor(textColor);
		printArea.setForeground(textColor);
		writeArea.setForeground(textColor);
		writeArea.addKeyListener(new WriterListener());

		JScrollPane scroll1 = new JScrollPane(printArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scroll2 = new JScrollPane(writeArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel panel = new JPanel(new BorderLayout());

		scroll2.setPreferredSize(new Dimension(0, 60));
		panel.add(scroll2, BorderLayout.SOUTH);
		panel.add(scroll1, BorderLayout.CENTER);



		appPages.addTab(convUser, panel);
		writeAreas.put(convUser, writeArea);
		printAreas.put(convUser, printArea);

		if (appPages.getSelectedIndex() == 0)
			for (int i = 1; i < appPages.getTabCount(); i++) {
				if (appPages.getTitleAt(i).equals(convUser)) {
					appPages.setSelectedIndex(i);
					break;
				}
			}

	}

	//creating reader and writer for new conv
	private boolean doConnect(String host, int port, String convUser) {

		try {
			SocketChannel convChannel = SocketChannel.open();
			convChannel.configureBlocking(false);
			convChannel.connect(new InetSocketAddress(host, port));
			while (!convChannel.finishConnect()) {
			}

			readThreads.put(convUser, new ArrayBlockingQueue(128));
			writeThreads.put(convUser, new ArrayBlockingQueue(128));

			ReadData reader;
			WriteData writer;
			reader = new ReadData(convChannel, readThreads.get(convUser));
			writer = new WriteData(convChannel, reader, writeThreads.get(convUser));
			reader.setWriter(writer);
			new Thread(reader).start();
			new Thread(writer).start();

		} catch (IOException ioEx) {
			System.err.println("Failed to create new conversation");
			System.err.println(ioEx.getMessage());
			return false;
		}
		return true;

	}

	//centering app on the screen
	private void centerFrame() {
		Dimension windowSize = getPreferredSize();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint = ge.getCenterPoint();
		int dx = centerPoint.x - windowSize.width/2;
		int dy = centerPoint.y - windowSize.height/2;
		setLocation(dx, dy);
	}

}

