package piotr.messenger.client;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;


public class ClientGUI implements
        ActionListener, WindowListener, ChangeListener, KeyListener {


    JFrame appFrame;
	JPanel mainPanel;
	JPanel centerPanel;
	JPanel southPanel;
	JToolBar buttonsMenuBar;
	JButton closeTab;
	JTabbedPane appPages;
	JList<Object> usersList;
	JTextField chooseUser;
	JButton sendRequest;
	JLabel ownerName;
	JLabel usersCount;

	DefaultListModel<Object> defListModel;
    ArrayBlockingQueue<String> mainDataQueue;
	Map<String, JTextArea> printAreas;
	Map<String, JTextArea> writeAreas;


	ClientGUI() {

	    appFrame = new JFrame();
        printAreas = new HashMap<>();
        writeAreas = new HashMap<>();

        WorkerThread worker = new WorkerThread(this);

		initMainWindow();
		addListeneres();
		appFrame.pack();

		Thread task = new Thread(worker);
		task.start();
	}

	private void initMainWindow() {
        appFrame.setTitle(Constants.APP_NAME);
        appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		SwingUtilities.updateComponentTreeUI(mainPanel);

        appFrame.setMinimumSize(new Dimension(290, 500));
        appFrame.setPreferredSize(new Dimension(300, 500));
        appFrame.setMaximumSize(new Dimension(360, 650));

		centerFrame();
        appFrame.setContentPane(mainPanel);

		usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersList.setLayoutOrientation(JList.VERTICAL);
		defListModel = new DefaultListModel<>();
		usersList.setModel(defListModel);
		usersCount.setText("Active users: " + defListModel.size());

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(mainPanel);
	}

	private void addListeneres() {

		closeTab.addActionListener(this);
		sendRequest.addActionListener(this);

        appFrame.addWindowListener(this);

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

        appPages.addChangeListener(this);
	}

	//------------ METHODS LISTENING FOR APPLICATION WINDOW CHANGES - BEGIN ------------//
	//handle closing ('X' button or Alt+F4)
	public void windowClosing(WindowEvent we) {
		int nOption;
		int tabCount = appPages.getTabCount();
		if (tabCount > 1) {

			nOption = JOptionPane.showConfirmDialog(mainPanel, "You have open conversations: " +
								 (tabCount - 1) + "\nClose anyway?", "Closing Programm",
					  JOptionPane.OK_CANCEL_OPTION);

			if (nOption == JOptionPane.CANCEL_OPTION || nOption == JOptionPane.CLOSED_OPTION)
				return;

		}
		mainDataQueue.add("q;");
        appFrame.setVisible(false);
//        mainPanel.setVisible(false);
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

    //handle conversation tabs changes
    @Override
    public void stateChanged(ChangeEvent e) {

        int idx = appPages.getSelectedIndex();
        if (idx > 0) {
            String tabName = appPages.getTitleAt(idx);
            writeAreas.get(tabName).requestFocus();
        }

    }

    //centering app on the screen
    private void centerFrame() {
        Dimension windowSize = appFrame.getPreferredSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();
        int dx = centerPoint.x - windowSize.width/2;
        int dy = centerPoint.y - windowSize.height/2;
        appFrame.setLocation(dx, dy);
    }

    void printMessage(String sender, String receiver, String message) {

        //display message in proper conversation tab
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        JTextArea tmpPrint = printAreas.get(receiver);
        tmpPrint.append(sender + ", " + dateFormat.format(calendar.getTime()) + "\n");
        tmpPrint.append(message + "\n\n");
        tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());
    }

    // ---------- HANDLE KEY PRESSES ----------- //
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            e.consume();
            int idx = appPages.getSelectedIndex();
            if (idx > 0) {
                String tabName = appPages.getTitleAt(idx);
                JTextArea tmpWrite = writeAreas.get(tabName);
                if (tmpWrite.getText().length() != 0) {

                    if (e.getModifiers() == InputEvent.SHIFT_MASK) {
                        tmpWrite.append("\n");
                        return;
                    }

                    mainDataQueue.add(tabName + ";" + tmpWrite.getText());
                    printMessage("me", tabName, tmpWrite.getText());
                    tmpWrite.setText("");
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    // ---------- HANDLE KEY PRESSES ----------- //
}

