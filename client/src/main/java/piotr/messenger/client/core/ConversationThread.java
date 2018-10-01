package piotr.messenger.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import piotr.messenger.client.gui.PrintWriteAreas;
import piotr.messenger.client.service.WindowMethods;
import piotr.messenger.client.util.ConvParameters;
import piotr.messenger.library.Constants;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * class used to read/send conversation data from/to the server
 * */

public class ConversationThread implements Runnable {

    private final ConvParameters params;
    private final PrintWriteAreas areas;
    private final JTabbedPane appTabbs;
    private final Map<String, BlockingQueue<String>> writeQueues;
    private final ByteBuffer buffer;
    private final Logger logger;
    private final String remoteUser;
    private boolean isRunning;

    public ConversationThread(ConvParameters params,
                              JTabbedPane appTabbs,
                              Map<String, BlockingQueue<String>> writeQueues,
                              PrintWriteAreas areas) {
        this.params = params;
        this.writeQueues = writeQueues;
        this.areas = areas;
        this.appTabbs = appTabbs;
        remoteUser = params.getRemoteUser();
        buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        logger = LoggerFactory.getLogger(ConversationThread.class);
    }

    @Override
    public void run() {

        writeQueues.put(remoteUser, new ArrayBlockingQueue<>(Constants.BLOCKING_SIZE));
        try (SocketChannel channel = SocketChannel.open()) {

            // connect to server using params
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(params.getHostAddress(), params.getHostPort()));

            int time = 0;
            while (!channel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(10);
                if (++time > 10) {
                    throw new IOException("Connection could not be finalized");
                }
            }

            // start read-write loop
            isRunning = true;
            while (isRunning) {
                readerLoop(channel);
                writerLoop(channel);
                TimeUnit.MILLISECONDS.sleep(60);
            }

        } catch (IOException ioEx) {
            logger.error(ioEx.getMessage());
        } catch (InterruptedException itrEx) {
            logger.error(itrEx.getMessage());
            Thread.currentThread().interrupt();
        }

        // remove BlockingQueue and JTextAreas
        // mapped to this conversation after closing it
        areas.getPrintAreas().remove(remoteUser);
        areas.getWriteAreas().remove(remoteUser);
        writeQueues.remove(remoteUser);
    }

    private void readerLoop(SocketChannel channel) throws IOException {

        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            isRunning = false;
            appTabbs.removeTabAt(getConvTabIndex());
            return;
        }

        String message = "";
        while (bytesRead > 0) {

            buffer.flip();

            message = message.concat(new String(buffer.array(), 0, buffer.remaining(), Constants.CHARSET));

            buffer.clear();
            bytesRead = channel.read(buffer);
        }
        if (message.length() > 0) {
            WindowMethods.printMessage(remoteUser, message, areas.getPrintAreas().get(remoteUser));
        }
    }

    private void writerLoop(SocketChannel channel) throws IOException, InterruptedException {

        if (!writeQueues.get(remoteUser).isEmpty()) {

            String input = writeQueues.get(remoteUser).take();

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

    private int getConvTabIndex() {
        for (int i = 1; i < appTabbs.getTabCount(); i++) {
            if (appTabbs.getTitleAt(i).equals(remoteUser)) {
                return i;
            }
        }
        return 0;
    }
}

