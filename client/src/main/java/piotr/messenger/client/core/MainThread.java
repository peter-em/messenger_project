package piotr.messenger.client.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.MainWindow;
import piotr.messenger.client.gui.LoginWindow;
import piotr.messenger.client.service.ConversationService;
import piotr.messenger.client.util.ConvParameters;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.library.util.ClientData;

/**
 * worker thread - manages application data
 * connects to server, sends user credentials (login and password)
 * receives clients list from server and presents it to user
 */

@Component
public class MainThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(MainThread.class);
    private MainWindow mainWindow;
    private LoginWindow loginWindow;
    private ConversationService convService;
    private ConversationsThread conversationsThread;
    private String userName;
    private BlockingQueue<TransferData> mainDataQueue;
    private volatile boolean isRunning;


    private void connectToServer(SocketChannel channel) throws IOException {

        channel.configureBlocking(true);
        channel.connect(new InetSocketAddress(Constants.HOST_ADDRESS, Constants.PORT_NR));
    }

    private void handleResponse(ByteBuffer buffer) {

        int response = buffer.getInt();
        if (response > 0) {

            List<String> clients = ClientDataConverter.decodeBufferToList(response, buffer);
            Collections.sort(clients);
            clients.remove(userName);

            DefaultListModel<String> listModel = mainWindow.getDefListModel();
            listModel.removeAllElements();
            for (String str : clients) {
                listModel.addElement(str);
            }
            mainWindow.getUsersCount().setText(("Active users: " + listModel.size()));

        }
    }

    private int performVerification(SocketChannel channel) throws IOException, InterruptedException {
        int response = -1;
        ByteBuffer tmpBuffer = ByteBuffer.allocate(0);
        while (response != 0) {

            Thread.sleep(128);
            if (!loginWindow.isLoginDataReady()) {
                continue;
            }

            ClientData clientData = loginWindow.getClientData();
            if (clientData == null) {
                loginWindow.disposeWindow();
                mainWindow.getMainFrame().dispose();
                return 0;
            }
            userName = clientData.getLogin();

            tmpBuffer = ClientDataConverter.encodeAuthToBuffer(
                    new ClientData(userName,
                            clientData.getPassword(),
                            clientData.getConnectMode()));

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
        return tmpBuffer.getInt();
    }

    @Override
    public void run() {

        Thread.currentThread().setName("Client_NO_ID");

        try(SocketChannel channel = SocketChannel.open()) {
            connectToServer(channel);

            loginWindow.showWindow();

            int convPort = performVerification(channel);
            if (convPort == 0)
                return;
            Thread.currentThread().setName("Client_" + userName);

            // start ConvThrd, set connection parameters
            conversationsThread.setParameters(new ConvParameters(Constants.HOST_ADDRESS, convPort, userName));
            mainWindow.getAppTabbs().setName(userName);
            Thread convWorker = new Thread(conversationsThread);
            convWorker.start();

            int counter = 0;
            while (!conversationsThread.isRunning() || counter++ < 10) {
                Thread.sleep(50);
            }
            if (counter == 10) {
                logger.error("Server could not start due to connection problems");
                return;
            }

            //reveal window app if verification was succesful
            mainWindow.getOwnerName().setText(userName);
            mainWindow.getMainFrame().setVisible(true);
            isRunning = true;

            ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
            while (isRunning) {

                buffer.clear();
                int bytesRead = channel.read(buffer);
                if (bytesRead != 0) {
                    buffer.flip();
                    handleResponse(buffer);
                }

                if (!mainDataQueue.isEmpty()) {

                    TransferData input = mainDataQueue.take();

                    //input data - type: type of message, content: receiver/message
                    if (input.getType().equals(Constants.C_TERMINATE)) {
                        convService.removeConvPage(input.getContent());

                    }
                }
                TimeUnit.MILLISECONDS.sleep(200);
            }

        } catch (IOException ioEx) {
            logger.error("Connection problem ({}).", ioEx.getMessage());
        } catch (InterruptedException intrEx) {
            Thread.currentThread().interrupt();
            logger.error("Main thread interrupted ({}).", intrEx.getMessage());
        }
        conversationsThread.stopConv();
        mainWindow.disposeWindow();
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
    public void setMainDataQueue(@Qualifier("mainQueue") BlockingQueue<TransferData> mainDataQueue) {
        this.mainDataQueue = mainDataQueue;
    }

    @Autowired
    public void setConvService(ConversationService convService) {
        this.convService = convService;
    }

    @Autowired
    public void setConversationsThread(ConversationsThread conversationsThread) {
        this.conversationsThread = conversationsThread;
    }
}
