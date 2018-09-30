package piotr.messenger.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import piotr.messenger.library.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * class used to read/send conversation data from/to the server
 * */

public class ConversationThread implements Runnable {

    private final SocketChannel channel;
    private final ArrayBlockingQueue<String> sendDataQueue;
    private final ArrayBlockingQueue<String> receiveDataQueue;
    private final ByteBuffer buffer;
    private final Logger logger;
    private boolean isRunning;

    public ConversationThread(SocketChannel channel,
                              ArrayBlockingQueue<String> sendDataQueue,
                              ArrayBlockingQueue<String> receiveDataQueue) {
        this.channel = channel;
        this.sendDataQueue = sendDataQueue;
        this.receiveDataQueue = receiveDataQueue;
        buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        logger = LoggerFactory.getLogger(ConversationThread.class);
    }

    @Override
    public void run() {
        isRunning = true;

        try {
            while (isRunning) {
                readerLoop();
                writerLoop();
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (IOException ioEx) {
            logger.error(ioEx.getMessage());
        } catch (InterruptedException irEx) {
            logger.error(irEx.getMessage());
            Thread.currentThread().interrupt();
        }

        try {
            channel.close();
        } catch (IOException ioex) {
            logger.error("Fatall error while closing socketChannel - {}", ioex.getMessage());
        }
    }

    private void readerLoop() throws IOException, InterruptedException {

        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            sendDataQueue.put("");
            isRunning = false;
            return;
        }

        while (bytesRead > 0) {

            buffer.flip();
            String receive = new String(buffer.array(), 0, buffer.remaining(), Constants.CHARSET);

            sendDataQueue.put(receive);

            buffer.clear();
            bytesRead = channel.read(buffer);
        }
    }

    private void writerLoop() throws IOException, InterruptedException {

        if (!receiveDataQueue.isEmpty()) {

            String input = receiveDataQueue.take();

            if (input.length() == 0) {
                isRunning = false;
                return;
            }

            buffer.clear();
            buffer.put(input.getBytes(Constants.CHARSET));
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        }
    }
}

