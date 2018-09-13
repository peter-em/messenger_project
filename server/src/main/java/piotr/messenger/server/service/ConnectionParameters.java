package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;

import java.util.LinkedList;
import java.util.List;

@Component
public class ConnectionParameters {

    private String hostAddress;
    private int hostPort;
    private List<Integer> executorPorts;

    public ConnectionParameters() {
        hostAddress = Constants.HOST_ADDRESS;
        hostPort = Constants.PORT_NR;
        executorPorts = new LinkedList<>();
    }

    public int getWorkerPort() {
        int availablePort = hostPort + 1;
        while (executorPorts.contains(availablePort)) {
            ++availablePort;
        }
        System.out.println("availablePort: " + availablePort);
        executorPorts.add(availablePort);
        return availablePort;
    }

    public int getLastUsedPort() {
        return executorPorts.get(executorPorts.size() - 1);
    }

    public void deletePort(int port) {
        executorPorts.remove(new Integer(port));
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getHostPort() {
        return hostPort;
    }
}
