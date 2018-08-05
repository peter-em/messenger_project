package piotr.messenger.client;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientGUI extends JFrame implements ActionListener, WindowListener, ChangeListener {


	private JPanel rootPanel;
	private JPanel centerPanel;
	private JPanel southPanel;
	private JToolBar buttonsMenuBar;
	private JButton closeTab;
	protected JTabbedPane appPages;
	private JList<Object> usersList;
	private JTextField chooseUser;
	private JButton sendRequest;
	private JLabel ownerName;
	private JLabel usersCount;

	private DefaultListModel<Object> defListModel;
	static ArrayBlockingQueue<String> mainDataQueue;
	private Map<String, ArrayBlockingQueue<String>> readThreads;
	private Map<String, ArrayBlockingQueue<String>> writeThreads;
	private Map<String, JTextArea> printAreas;
	private Map<String, JTextArea> writeAreas;
	private Logger logger;


	ClientGUI() {
		setTitle(Constants.APP_NAME);

		logger = LoggerFactory.getLogger(ClientGUI.class);
		HandleWorkload worker = new HandleWorkload(logger);

		initComponents();
		addListeneres();
		pack();

		Thread task = new Thread(worker);
		task.start();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		SwingUtilities.updateComponentTreeUI(rootPanel);

		setMinimumSize(new Dimension(290, 500));
		setPreferredSize(new Dimension(300, 500));
		setMaximumSize(new Dimension(360, 650));

		centerFrame();
		setContentPane(rootPanel);


		usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersList.setLayoutOrientation(JList.VERTICAL);
		defListModel = new DefaultListModel<>();
		usersList.setModel(defListModel);

		usersCount.setText("Active users: " + defListModel.size());

		readThreads = new HashMap<>();
		writeThreads = new HashMap<>();
		printAreas = new HashMap<>();
		writeAreas = new HashMap<>();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

//		appPages.addChangeListener(new TabChangeListener());
        appPages.addChangeListener(this);
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

	    Logger logger;

        HandleWorkload(Logger logger) {
            this.logger = logger;
        }

        @Override
		public void run() {

            Thread.currentThread().setName("Client_NO_ID");
			ByteBuffer buffer;
			try (SocketChannel channel = SocketChannel.open()) {
				channel.configureBlocking(true);
				channel.connect(new InetSocketAddress(Constants.SRVR_ADDRESS, Constants.PORT_NR));

				int timeOut = 0;
				while (!channel.finishConnect()) {
					if (timeOut++ > 100)
						return;
					TimeUnit.MILLISECONDS.sleep(100);
				}

				buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
                logger.debug("Sending cerificate");
				buffer.clear();
				buffer.put(Constants.CLIENT_PATTERN.getBytes(Constants.CHARSET));
				buffer.flip();
				channel.write(buffer);
				String userName = "";

				int response = -1;
				while (response == -1) {

					while (userName.length() < 3) {
						userName = JOptionPane.showInputDialog(rootPanel, "Enter Your login to start,\n" +
								  "or press 'Cancel' to exit app.\nLogin must contain " +
								  "at least 3 characters.", "Enter Login", JOptionPane.INFORMATION_MESSAGE);

						if (userName == null) {
							setVisible(false);
							dispose();
							return;
						}
						userName = userName.trim();
					}

					buffer.clear();
					buffer.put((userName + ";").getBytes(Constants.CHARSET));
					buffer.flip();
					channel.write(buffer);

					buffer.clear();
					channel.read(buffer);
					buffer.flip();
					response = buffer.getInt();

					if (response == -1) {
                        JOptionPane.showMessageDialog(rootPanel, "This login is already in use,\n",
                                "Login error", JOptionPane.INFORMATION_MESSAGE);
                        userName = "";
                    }
				}


				//reveal window app if verification was succesful
				mainDataQueue = new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE);
				ownerName.setText(userName);
				setVisible(true);
				Thread.currentThread().setName("Client_" + userName);

				// wyswietlanie listy uzytkownikow
				boolean clientsUpdate = true;
				boolean awaitingConv = false;
				boolean sendOK;
				int bytesRead = 0;
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
				while (bytesRead != -1) {

					if (clientsUpdate) {
						//print active users
						if (response == 1) {
							//no other users
							if (defListModel.size() != 0) {
								defListModel.removeAllElements();
								usersCount.setText("Active users: " + defListModel.size());
							}
						} else {
							usersData = new String(buffer.array(), Constants.CHARSET);
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
							usersData = new String(buffer.array(), Constants.CHARSET);
							String[] convUser = usersData.split(";");

							new Thread(new DialogsHandler(DialogsHandler.CONV_REQUEST, convUser[0], rootPanel)).start();

							awaitingConv = true;
							convUsers.add(convUser[0]);

						} else if (response == -20) {
							//response when asked conv has already started
							usersData = new String(buffer.array(), Constants.CHARSET);
							new Thread(new DialogsHandler(DialogsHandler.CONV_STARTED, usersData.split(";")[0], rootPanel)).start();

						} else if (response == -30) {
							//asked user refused conversation
							usersData = new String(buffer.array(), Constants.CHARSET);
							new Thread(new DialogsHandler(DialogsHandler.CONV_REFUSED, usersData.split(";")[0], rootPanel)).start();
						} else if (response == -40) {

							//user accepted invitation
							usersData = new String(buffer.array(), Constants.CHARSET);
							String[] connectData = usersData.split(";");
							//connectData contains - [0]: port number, [1]: server address,
							//[2] and [3]: logins of users starting conversation
							String startConvUser = connectData[2].equals(userName)?connectData[3]:connectData[2];

							//confirm connection
							buffer.clear();
							buffer.put(("c;" + userName + ";" + connectData[0] + ";").getBytes(Constants.CHARSET));
							buffer.flip();
							channel.write(buffer);

							if (doConnect(connectData[1], Integer.parseInt(connectData[0]), startConvUser)) {
								createConvPage(startConvUser);
							}
						}
					}

					if (!mainDataQueue.isEmpty()) {

						try {
							input = mainDataQueue.take();
							sendOK = true;
						} catch (InterruptedException bqEx) {
						    logger.error("Main queue ({})", bqEx.getMessage());
							input = "";
							sendOK = false;
						}

						buffer.clear();
						if (input.length() > 0) {
							inputArray = input.split(";");
							//inputArray - [0]: type of message, [1]: sender, [2]: message content
							if (inputArray[0].equals("q")) {
								break;
							} else if (awaitingConv) {
								if (inputArray[0].equals("y")) {
									//inform server about accepted conv
									buffer.put(("y;" + inputArray[1] + ";" + userName + ";").getBytes(Constants.CHARSET));
									convUsers.remove(inputArray[1]);
								} else {
									//inform server about refused conv
									buffer.put(("n;" + inputArray[1] + ";" + userName + ";").getBytes(Constants.CHARSET));
									convUsers.remove(inputArray[1]);
								}
								if (convUsers.isEmpty())
									awaitingConv = false;

							} else if (inputArray[0].equals("s")) {
								//copy and process received message
								JTextArea tmpPrint = printAreas.get(inputArray[1]);
								calendar = Calendar.getInstance();
								tmpPrint.append("me, " + dateFormat.format(calendar.getTime()) + "\n");
								tmpPrint.append(inputArray[2] + "\n\n");
								tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());
								//display message in proper conversation tab
								writeThreads.get(inputArray[1]).add("g;" + inputArray[2]);

							} else if (inputArray[0].equals("t")) {
								//send termination information to writer
								writeThreads.get(inputArray[1]).add("s;");
								//remove references to BlockingQueues and JTextAreas mapped to conversation
								removeMapings(inputArray[1]);
							} else if (inputArray[0].equals("a") && clientsNames.contains(inputArray[1])) {
								//send conversation request
								buffer.put(("a;" + inputArray[1] + ";").getBytes(Constants.CHARSET));
							}
						}
						//send buffer data when flag is true
						if (sendOK) {
							buffer.flip();
							channel.write(buffer);
						}
					}

					//check queues if there are any data from readers to display
					for (String convKey : readThreads.keySet()) {
						ArrayBlockingQueue queue = readThreads.get(convKey);
						if (!queue.isEmpty()) {
							try {
								input = (String)queue.take();
							} catch (InterruptedException bqEx) {
								logger.error("Read queue ({})", bqEx.getMessage());
								continue;
							}

							//special case, due to inability to send empty string,
							//only conv reader can input it to signal EoC
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

					//remove mapping for closed conversation
					if (!removingConvs.isEmpty()) {
						for (String str : removingConvs) {
							removeMapings(str);
						}
						removingConvs.clear();
					}

					buffer.clear();
					TimeUnit.MILLISECONDS.sleep(32);

				}

			} catch (IOException ioEx) {
                logger.error("Connection problem ({}).", ioEx.getMessage());
			} catch (InterruptedException intrEx) {
                logger.error("Main thread interrupted ({}).", intrEx.getMessage());
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

			for (ArrayBlockingQueue<String> queue : writeThreads.values()) {
				queue.add("s;");
			}
			setVisible(false);
			dispose();
		}
	}

    //handle conversation tabs changes
    @Override
    public void stateChanged(ChangeEvent e) {

        int idx = appPages.getSelectedIndex();
        if (idx > 0) {
            String tabName = appPages.getTitleAt(idx);
            writeAreas.get(tabName).requestFocus();
        }

    }

	//method creating new tab for conversation
	private void createConvPage(String convUser) {

		JTextArea printArea = new JTextArea();
		printArea.setFont(new Font("Consolas", Font.PLAIN, 13));
		JTextArea writeArea = new JTextArea();
		writeArea.setFont(new Font("Consolas", Font.PLAIN, 13));
		printArea.setEditable(false);
		printArea.setWrapStyleWord(true);
		printArea.setLineWrap(true);
		writeArea.setWrapStyleWord(true);
		writeArea.setLineWrap(true);
		printArea.setBackground(Constants.TEXT_AREA_COLOR);
		writeArea.setBackground(Constants.TEXT_AREA_COLOR);
		writeArea.setCaretColor(Constants.TEXT_COLOR);
		printArea.setForeground(Constants.TEXT_COLOR);
		writeArea.setForeground(Constants.TEXT_COLOR);
		writeArea.addKeyListener(new WriterListener(appPages, writeAreas, mainDataQueue));

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

			int time = 0;
			while (!convChannel.finishConnect()) {
                logger.debug("doConnect - finishConnect() pending");
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException itrEx) {
					itrEx.printStackTrace();
				}
				if (++time > 10) {
					throw new IOException("Connection could not be finalized");
				}
			}

			readThreads.put(convUser, new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));
			writeThreads.put(convUser, new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));

            Reader reader = new Reader(convChannel, readThreads.get(convUser));
			Writer writer = new Writer(convChannel, reader, writeThreads.get(convUser));
			reader.setWriter(writer);
			new Thread(reader).start();
			new Thread(writer).start();

		} catch (IOException ioEx) {
            logger.error("Failed to create new conversation ({})!", ioEx.getMessage());
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

