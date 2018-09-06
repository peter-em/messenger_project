package piotr.messenger.springclient.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import piotr.messenger.springclient.gui.listener.ConvKeyListener;
import piotr.messenger.springclient.util.Constants;
import piotr.messenger.springclient.util.DialogsHandler;
import piotr.messenger.springclient.util.LoginData;
import piotr.messenger.springclient.gui.MainWindow;
import piotr.messenger.springclient.gui.LoginWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//worker thread - manages application data
@Component
public class WorkerThread implements Runnable {

    private Logger logger;
    private boolean awaitingConv;
    private ByteBuffer buffer;
    private SocketChannel channel;
    private List<String> convUsers;
    private DialogsHandler dialogs;
    private List<String> clientsNames;
    private MainWindow appManager;
    private LoginWindow loginWindow;
    private String userName;
    private Map<String, ArrayBlockingQueue<String>> readThreads;
    private Map<String, ArrayBlockingQueue<String>> writeThreads;
    private Map<String, JTextArea> writeAreas;
    private Map<String, JTextArea> printAreas;
    private ArrayBlockingQueue<String> mainDataQueue;
    private DefaultListModel<String> defListModel;

    public WorkerThread() {
//        logger = LoggerFactory.getLogger(MainWindow.class);
        logger = LoggerFactory.getLogger(WorkerThread.class);
        awaitingConv = false;
        convUsers = new LinkedList<>();
        clientsNames = new LinkedList<>();
        readThreads = new HashMap<>();
		writeThreads = new HashMap<>();
    }

    public void init(MainWindow appManager) {
        this.appManager = appManager;
    }

    private SocketChannel connectToServer() {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(Constants.SRVR_ADDRESS, Constants.PORT_NR));
            int timeOut = 0;
            while (!channel.finishConnect()) {
                if (timeOut++ > 100)
                    return null;
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (IOException ioEx) {
            logger.error("Connection problem ({}).", ioEx.getMessage());
            return null;
        } catch (InterruptedException intrEx) {
            logger.error("Main thread interrupted ({}).", intrEx.getMessage());
//                return false;
        }
        return channel;
    }

    private void handleResponse(int response) {
        String usersData = new String(buffer.array(), Constants.CHARSET);
        //user holds another user's login
//            logger.info("input: {}", usersData);
//            logger.info("response: {}", response);
        String[] users = usersData.split(";");
        if (response > 0) {

            clientsNames.clear();
            for (int i = 0; i < response; i++) {
                if (userName.equals(users[i]))
                    continue;
                clientsNames.add(users[i]);
            }
            Collections.sort(clientsNames);

            defListModel.removeAllElements();
            for (String str : clientsNames) {
                defListModel.addElement(str);
            }
            appManager.getUsersCount().setText(("Active users: " + defListModel.size()));

        } else if (response == -10) {
            //another user wants to talk
//            appManager.getMainDataQueue().add(dialogs.convInvite(users[0]));
            mainDataQueue.add(dialogs.convInvite(users[0]));
            awaitingConv = true;
            convUsers.add(users[0]);

        } else if (response == -20) {
            //response when asked conv has already started
            dialogs.hasStarted(users[0]);

        } else if (response == -30) {
            //asked user refused conversation
            dialogs.refused(users[0]);
        } else if (response == -40) {

            //user accepted invitation
            String[] connectData = usersData.split(";");
            //connectData contains - [0]: port number, [1]: server address,
            //[2] and [3]: logins of users starting conversation
            String startConvUser = connectData[2].equals(userName)?connectData[3]:connectData[2];

            if (doConnect(connectData[1], Integer.parseInt(connectData[0]), startConvUser)) {
                createConvPage(startConvUser);
            }
        }
    }

    private void sendToServer(ByteBuffer buffer) {
        try {
            channel.write(buffer);
        } catch (IOException ioEx) {
            logger.error("Writing to server channel failed ({}).", ioEx.getMessage());
        }
    }

    //method creating new tab for conversation
    private void createConvPage(String convUser) {

        JTextArea printArea = new JTextArea();
        printArea.setFont(Constants.AREA_FONT);
        JTextArea writeArea = new JTextArea();
        writeArea.setFont(Constants.AREA_FONT);
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
//        writeArea.addKeyListener(appManager);
        writeArea.addKeyListener(new ConvKeyListener());

        JScrollPane scroll1 = new JScrollPane(printArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scroll2 = new JScrollPane(writeArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new BorderLayout());

        scroll2.setPreferredSize(new Dimension(0, 60));
        panel.add(scroll2, BorderLayout.SOUTH);
        panel.add(scroll1, BorderLayout.CENTER);



        appManager.getAppPages().addTab(convUser, panel);
//        appManager.getWriteAreas().put(convUser, writeArea);
        writeAreas.put(convUser, writeArea);
//        appManager.getPrintAreas().put(convUser, printArea);
        printAreas.put(convUser, printArea);


        if (appManager.getAppPages().getSelectedIndex() == 0)
            for (int i = 1; i < appManager.getAppPages().getTabCount(); i++) {
                if (appManager.getAppPages().getTitleAt(i).equals(convUser)) {
                    appManager.getAppPages().setSelectedIndex(i);
//                    break;
                    return;
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

    public static void printMessage(String sender, String receiver, String message,
                                    JTextArea tmpPrint) {
        //display message in proper conversation tab
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
//        JTextArea tmpPrint = printAreas.get(receiver);
        tmpPrint.append(sender + ", " + dateFormat.format(calendar.getTime()) + "\n");
        tmpPrint.append(message + "\n\n");
        tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());
    }

    public void printMessage(String sender, String receiver, String message) {

        //display message in proper conversation tab
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        JTextArea tmpPrint = printAreas.get(receiver);
        tmpPrint.append(sender + ", " + dateFormat.format(calendar.getTime()) + "\n");
        tmpPrint.append(message + "\n\n");
        tmpPrint.setCaretPosition(tmpPrint.getDocument().getLength());
    }

    @Override
    public void run() {

//        LoginWindow loginWindow = new LoginWindow();


        Thread.currentThread().setName("Client_NO_ID");

        channel = connectToServer();
        if (channel == null) {
            logger.info("channel is null");
            performSafeClose();
            return;
        }

        try {

            buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
            userName = "";

            int response = -1;
            int bytesRead = 0;

            loginWindow.showWindow();
            while (response == -1) {

                Thread.sleep(128);
                if (!loginWindow.isLoginDataReady()) {
                    continue;
                }

                LoginData loginData = loginWindow.getLoginData();
                if (loginData == null) {
                    loginWindow.disposeWindow();
//                    appManager.getAppFrame().setVisible(false);
                    appManager.getAppFrame().dispose();
                    return;
                }
                userName = loginData.getLogin();


                buffer.clear();
                buffer.put((userName + ";").getBytes(Constants.CHARSET));
                buffer.flip();
                channel.write(buffer);
                loginData.clearData();

                buffer.clear();
                bytesRead = channel.read(buffer);
                buffer.flip();
                response = buffer.getInt();

                if (response == -1) {
                    userName = "";
                    loginWindow.dataInvalid(Constants.SIGNUP_ERROR);
//                    loginWindow.setLoginData(null);

                }
            }
            channel.configureBlocking(false);
            loginWindow.disposeWindow();


            //reveal window app if verification was succesful
//            appManager.setMainDataQueue(new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));

            appManager.getOwnerName().setText(userName);
//            setOwnerName(userName);
            appManager.getAppFrame().setVisible(true);
            Thread.currentThread().setName("Client_" + userName);

            boolean sendOK;

            String[] inputArray;
            String input;

            List<String> removingConvs = new ArrayList<>();
            while (bytesRead != -1) {

                buffer.clear();
                bytesRead = channel.read(buffer);
                if (bytesRead != 0) {
                    buffer.flip();
                    response = buffer.getInt();
                    buffer.compact();

                    handleResponse(response);
                }


//                if (!appManager.getMainDataQueue().isEmpty()) {
                if (!mainDataQueue.isEmpty()) {

                    try {
//                        input = appManager.getMainDataQueue().take();
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

                        } else if (inputArray[0].equals("t")) {
                            //send termination information to writer
                            writeThreads.get(inputArray[1]).add("");
                            //remove references to BlockingQueues and JTextAreas mapped to conversation
                            removeMapings(inputArray[1]);
                        } else if (inputArray[0].equals("a") && clientsNames.contains(inputArray[1])) {
                            //send conversation request
                            buffer.put(("a;" + inputArray[1] + ";").getBytes(Constants.CHARSET));
                        } else {
                            int index = input.indexOf(';');
                            writeThreads.get(input.substring(0,index)).add(input.substring(index+1));
//                    writeThreads.get(tabName).add(tmpWrite.getText());

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
                            for (int i = 1; i < appManager.getAppPages().getTabCount(); i++) {
                                if (appManager.getAppPages().getTitleAt(i).equals(convKey)) {
                                    appManager.getAppPages().removeTabAt(i);
                                    break;
                                }
                            }
                            removingConvs.add(convKey);

                        } else {
                            // process received message
//                            appManager.printMessage(convKey, convKey, input);
                            printMessage(convKey, convKey, input);
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
            channel.close();

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
//        appManager.getWriteAreas().remove(convUser);
        writeAreas.remove(convUser);
//        appManager.getWriteAreas().remove(convUser); <-- prob should be removing from printAreas
        printAreas.remove(convUser);
    }

    private void performSafeClose() {

        for (ArrayBlockingQueue<String> queue : writeThreads.values()) {
            queue.add("");
        }
        appManager.getAppFrame().setVisible(false);
        appManager.getAppFrame().dispose();
    }

    @Autowired
    public void setLoginWindow(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
    }

    public ArrayBlockingQueue<String> getMainDataQueue() {
        return mainDataQueue;
    }

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<String> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

    public Map<String, JTextArea> getWriteAreas() {
        return writeAreas;
    }

    @Autowired
    public void setWriteAreas(@Qualifier("writeAreas") Map<String, JTextArea> writeAreas) {
        this.writeAreas = writeAreas;
    }

    public Map<String, JTextArea> getPrintAreas() {
        return printAreas;
    }

    @Autowired
    public void setPrintAreas(@Qualifier("printAreas") Map<String, JTextArea> printAreas) {
        this.printAreas = printAreas;
    }

    @Autowired
    public void setDefListModel(DefaultListModel<String> defListModel) {
        this.defListModel = defListModel;
    }

    @Autowired
    public void setDialogs(DialogsHandler dialogs) {
        this.dialogs = dialogs;
    }
}
