package piotr.messenger.server.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import piotr.messenger.server.service.ConversationsService;
import piotr.messenger.server.util.ServerPorts;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;


@Component
@Slf4j
public class ConversationsWorker implements Runnable {

    @Resource private ServerPorts ports;
    @Resource private ConversationsService service;
    private volatile boolean isRunning;


    @Override
    public void run() {

        Thread.currentThread().setName("Worker (" + ports.getWorkerPort() + ")");

        System.setProperty("sun.net.useExclusiveBind", "false");
        try (ServerSocketChannel handler = ServerSocketChannel.open();
             Selector convSelector = SelectorProvider.provider().openSelector()) {

            openSocket(handler, convSelector);
            log.info("Worker started");

            while (isRunning) {

                convSelector.select();

                SelectionKey key;
                Iterator<SelectionKey> selectedKeys;
                selectedKeys = convSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                    } else if (key.isAcceptable()) {
                        service.acceptUser(handler, convSelector);

                    } else if (key.isReadable()) {
                        service.read((SocketChannel) key.channel(), convSelector);

                    } else if (key.isWritable()) {
                        service.write((SocketChannel) key.channel());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException ioEx) {
            log.error(ioEx.getMessage());
        }
        log.info("Closing worker");

    }

    private void openSocket(ServerSocketChannel handler, Selector convSelector) throws IOException {

        handler.configureBlocking(false);
        handler.socket().bind(new InetSocketAddress(ports.getWorkerPort()));
        handler.register(convSelector, SelectionKey.OP_ACCEPT);
        isRunning = true;
    }

    boolean isRunning() {
        return isRunning;
    }

    void stopWorker() {
        isRunning = false;
    }
}
