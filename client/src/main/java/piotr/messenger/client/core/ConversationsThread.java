package piotr.messenger.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import piotr.messenger.client.gui.ConvComponents;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


@Component
public class ConversationsThread implements Runnable {

    private ConvParameters parameters;
    private Map<String, ConvComponents> convComponentsMap;
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
                TimeUnit.MILLISECONDS.sleep(5);
                if (++time > 100) {
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
                TimeUnit.MILLISECONDS.sleep(50);
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

        int messageMode = buffer.getInt();
        int messageCount = buffer.getInt();

        // list data contains fields
        // 0 - conv partner (remoter user) or empty
        // next (3 * messageCount) fields:
        //    field1: author of a message
        //    field2: localdatetime of a message
        //    field3: content of a message
        List<String> data = ClientDataConverter.decodeBufferToList(3 * messageCount + 1, buffer);

        if (messageMode == Constants.REGULAR_MSG) {
            LocalDateTime localDateTime = LocalDateTime.parse(data.get(2));
            data.set(0, data.get(1));
            if (!convComponentsMap.containsKey(data.get(0))) {
                convService.createConvPage(data.get(0));
                convComponentsMap.get(data.get(0)).setOldestMessage(localDateTime);
                messageQueue.add(new TransferData(data.get(0), data.get(2), Constants.ARCHIVED_MSG));
            }

            WindowMethods.printNewMessage(data.get(1), data.get(3), localDateTime.toLocalTime(),
                    convComponentsMap.get(data.get(0)).getPrintArea());

        } else if (messageMode == Constants.ARCHIVED_MSG) {
            String conversation = data.get(0);
            LocalDateTime localDateTime = convComponentsMap.get(conversation).getOldestMessage();
            for (int i = 0; i < messageCount; ++i) {

                localDateTime = LocalDateTime.parse(data.get(3*i + 2));
                WindowMethods.printArchivedMessage(data.get(3*i + 1), data.get(3*i + 3), localDateTime.toLocalTime(),
                        convComponentsMap.get(conversation).getPrintArea());

            }
            convComponentsMap.get(conversation).setOldestMessage(localDateTime);
        }
    }


    private void writer(SocketChannel channel) throws IOException, InterruptedException {

        while (!messageQueue.isEmpty()) {

            TransferData input = messageQueue.take();
            if (input.getMsgMode() == Constants.C_REQUEST) {
                if (convComponentsMap.containsKey(input.getType())) {
                    continue;
                }
                convService.createConvPage(input.getType());
                input.setMsgMode(Constants.ARCHIVED_MSG);
            }

            prepareBuffer(input);
            channel.write(buffer);
        }
    }

    private void prepareBuffer(TransferData input) {
        buffer.clear();
        buffer.putInt(input.getMsgMode());
        byte[] data = parameters.getUserName().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
        data = input.getType().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
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
    public void setConvComponentsMap(Map<String, ConvComponents> convComponentsMap) {
        this.convComponentsMap = convComponentsMap;
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
