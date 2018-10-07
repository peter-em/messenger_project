package piotr.messenger.server.service;

import lombok.Getter;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;

import java.util.LinkedList;
import java.util.List;

@Component
public class ConnectionParameters {

    private final @Getter String hostAddress;
    private final @Getter int hostPort;
    private final @Getter List<Integer> executorPorts;
    private int lastUsedPort;

    public ConnectionParameters() {
        hostAddress = Constants.HOST_ADDRESS;
        hostPort = Constants.PORT_NR;
        executorPorts = new LinkedList<>();
        lastUsedPort = 0;
    }

    public int getWorkerPort() {
        int availablePort = hostPort + 1;
        while (executorPorts.contains(availablePort)) {
            ++availablePort;
        }
        executorPorts.add(availablePort);
        lastUsedPort = availablePort;
        return lastUsedPort;
    }

    public int getLastUsedPort() {
        return lastUsedPort;
    }

    public void deletePort(int port) {
        executorPorts.remove((Integer) port);
    }

}
