package piotr.messenger.client.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.MainWindow;
import piotr.messenger.client.gui.LoginWindow;
import piotr.messenger.client.service.ConversationService;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.library.util.ClientData;


//worker thread - manages application data
@Component
public class WorkerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private MainWindow mainWindow;
    private LoginWindow loginWindow;
    private ConversationService convService;
    private ByteBuffer buffer;
    private String userName;
    private ArrayBlockingQueue<TransferData> mainDataQueue;
    private volatile boolean isRunning;


    private void connectToServer(SocketChannel channel) throws IOException {

        channel.configureBlocking(true);
        channel.connect(new InetSocketAddress(Constants.HOST_ADDRESS, Constants.PORT_NR));
    }

    private void handleResponse(int response) {

        if (response > 0) {

            List<String> clients = ClientDataConverter.decodeListFromServer(response, buffer);
            Collections.sort(clients);
            clients.remove(userName);

            DefaultListModel<String> listModel = mainWindow.getDefListModel();
            listModel.removeAllElements();
            for (String str : clients) {
                listModel.addElement(str);
            }
            mainWindow.getUsersCount().setText(("Active users: " + listModel.size()));

        } else if (response == -40) {

            //  another user requested conv, server sends connectData
            //  connectData contains - [0]: port number, [1]: server address,
            //  [2] and [3]: logins of users involved in conversation
            List<String> connectData = ClientDataConverter.decodeListFromServer(4, buffer);
            int port = Integer.parseInt(connectData.get(0));
            String startConvUser = connectData.get(2).equals(userName)?connectData.get(3):connectData.get(2);

            if (convService.doConnect(connectData.get(1), port, startConvUser)) {
                convService.createConvPage(startConvUser, mainWindow.getAppTabbs());
            }
        }
    }

    private boolean performVerification(SocketChannel channel) throws IOException, InterruptedException {
        int response = -1;
        while (response != 0) {

            Thread.sleep(128);
            if (!loginWindow.isLoginDataReady()) {
                continue;
            }

            ClientData clientData = loginWindow.getClientData();
            if (clientData == null) {
                loginWindow.disposeWindow();
                mainWindow.getMainFrame().dispose();
                return false;
            }
            userName = clientData.getLogin();

            ByteBuffer tmpBuffer = ClientDataConverter.encodeToBuffer(
                    new ClientData(userName, clientData.getPassword(), clientData.getConnectMode()));

            tmpBuffer.flip();
            channel.write(tmpBuffer);

            tmpBuffer.clear();
            channel.read(tmpBuffer);
            tmpBuffer.flip();
            response = tmpBuffer.getInt();

            if (response != 0) {
                loginWindow.dataInvalid(response);
            }
        }
        channel.configureBlocking(false);
        loginWindow.disposeWindow();
        return true;
    }

    @Override
    public void run() {

        Thread.currentThread().setName("Client_NO_ID");

        try(SocketChannel channel = SocketChannel.open()) {
            connectToServer(channel);

            loginWindow.showWindow();

            if (!performVerification(channel))
                return;

            //reveal window app if verification was succesful
            mainWindow.getOwnerName().setText(userName);
            mainWindow.getMainFrame().setVisible(true);
            isRunning = true;
            Thread.currentThread().setName("Client_" + userName);

            buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
            int bytesRead;
            while (isRunning) {

                buffer.clear();
                bytesRead = channel.read(buffer);
                if (bytesRead != 0) {
                    buffer.flip();
                    handleResponse(buffer.getInt());
                }

                if (!mainDataQueue.isEmpty()) {

                    TransferData input = mainDataQueue.take();

                    //input data - type: type of message, content: receiver/message
                    if (input.getType().equals(Constants.C_TERMINATE)) {
                        //send termination information to writer
                        convService.getWriteQueues().get(input.getContent()).add("");
                        //remove references to BlockingQueues and JTextAreas mapped to conversation
                        convService.removeMapings(input.getContent());
                    } else if (input.getType().equals(Constants.C_REQUEST)) {
                        //send conversation request
                        buffer.clear();
                        prepareBufferForRequest(input.getContent(), userName);
                        //send buffer data
                        buffer.flip();
                        channel.write(buffer);
                    } else {
                        convService.getWriteQueues().get(input.getType()).add(input.getContent());
                    }
                }

                convService.readDataQueues(mainWindow.getAppTabbs());
                buffer.clear();
                TimeUnit.MILLISECONDS.sleep(32);
            }

        } catch (IOException ioEx) {
            logger.error("Connection problem ({}).", ioEx.getMessage());
        } catch (InterruptedException intrEx) {
            Thread.currentThread().interrupt();
            logger.error("Main thread interrupted ({}).", intrEx.getMessage());
        }
        convService.stopConvThreads();
        mainWindow.disposeWindow();
    }

    private void prepareBufferForRequest(String receiver, String sender) {
        buffer.putInt(1).put("a".getBytes(Constants.CHARSET));
        buffer.putInt(receiver.length()).put(receiver.getBytes(Constants.CHARSET));
        buffer.putInt(sender.length()).put(sender.getBytes(Constants.CHARSET));
    }

    public void stopWorker() {
        isRunning = false;
    }

    @Autowired
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Autowired
    public void setLoginWindow(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
    }

    @Autowired
    public void setMainDataQueue(ArrayBlockingQueue<TransferData> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

    @Autowired
    public void setConvService(ConversationService convService) {
        this.convService = convService;
    }
}
