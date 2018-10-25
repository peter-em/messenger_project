package piotr.messenger.server.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ConnectionParameters {

    @Value("${server.string.hostaddress}")
    private @Getter String hostAddress;
    @Value("${server.int.hostport}")
    private @Getter int hostPort;
    private final @Getter List<Integer> executorPorts;
    private int lastUsedPort;

    public ConnectionParameters() {
        executorPorts = new LinkedList<>();
        lastUsedPort = -1;
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
