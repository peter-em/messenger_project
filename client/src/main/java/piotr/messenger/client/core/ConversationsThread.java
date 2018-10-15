package piotr.messenger.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.PrintWriteAreas;
import piotr.messenger.client.service.ConversationService;
import piotr.messenger.client.service.WindowMethods;
import piotr.messenger.client.util.ConvParameters;
import piotr.messenger.client.util.TransferData;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class ConversationsThread implements Runnable {

    private ConvParameters parameters;
    private PrintWriteAreas areas;
    private ConversationService convService;
    private BlockingQueue<TransferData> messageQueue;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private Logger logger = LoggerFactory.getLogger(ConversationsThread.class);
    private volatile boolean isRunning;


    void setParameters(ConvParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void run() {

        try (SocketChannel channel = SocketChannel.open()) {

            // connect to server using params
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(parameters.getHostAddress(), parameters.getHostPort()));

            int time = 0;
            while (!channel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(10);
                if (++time > 50) {
                    throw new IOException("Connection could not be finalized");
                }
            }
            Thread.currentThread().setName("ConvWorker(" + parameters.getHostPort() + ")");

            // send userName in order to confirm connection on server side
            buffer.putInt(parameters.getUserName().length()).put(parameters.getUserName().getBytes(Constants.CHARSET));
            buffer.flip();
            channel.write(buffer);

            // start read-write loop
            isRunning = true;
            while (isRunning) {
                reader(channel);
                writer(channel);
                TimeUnit.MILLISECONDS.sleep(80);
            }

        } catch (IOException ioEx) {
            logger.error(ioEx.getMessage());
        } catch (InterruptedException itrEx) {
            logger.error(itrEx.getMessage());
            Thread.currentThread().interrupt();
        }

    }

    private void reader(SocketChannel channel) throws IOException {

        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            isRunning = false;
            return;
        }

        if (bytesRead == 0) {
            return;
        }

        buffer.flip();

        // list users contains two (2) userNames
        // 0 - sender of this message
        // 1 - receiver of this message (current, local user)(unimportant)
        List<String> users = ClientDataConverter.decodeBufferToList(2, buffer);
        String message = new String(buffer.slice().array(), buffer.position(), buffer.remaining(), Constants.CHARSET);

        if (!convService.isConvPageCreated(users.get(0))) {
            convService.createConvPage(users.get(0));
        }

        if (message.length() > 0) {
            WindowMethods.printMessage(users.get(0), message, areas.getPrintAreas().get(users.get(0)));
        }
    }


    private void writer(SocketChannel channel) throws IOException, InterruptedException {

        if (!messageQueue.isEmpty()) {

            TransferData input = messageQueue.take();
            prepareBuffer(input);
            channel.write(buffer);
        }
    }

    private void prepareBuffer(TransferData input) {
        buffer.clear();
        buffer.putInt(parameters.getUserName().length()).put(parameters.getUserName().getBytes(Constants.CHARSET));
        buffer.putInt(input.getType().length()).put(input.getType().getBytes(Constants.CHARSET));
        buffer.put(input.getContent().getBytes(Constants.CHARSET));
        buffer.flip();
    }

    void stopConv() {
        isRunning = false;
    }

    boolean isRunning() {
        return isRunning;
    }

    @Autowired
    public void setAreas(PrintWriteAreas areas) {
        this.areas = areas;
    }

    @Autowired
    public void setConvService(ConversationService convService) {
        this.convService = convService;
    }

    @Autowired
    public void setMessageQueue(@Qualifier("messageQueue") BlockingQueue<TransferData> messageQueue) {
        this.messageQueue = messageQueue;
    }
}
