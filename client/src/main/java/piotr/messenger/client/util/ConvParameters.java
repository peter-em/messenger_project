package piotr.messenger.client.util;

import lombok.Getter;

public class ConvParameters {

    private final @Getter String hostAddress;
    private final @Getter int hostPort;
    private final @Getter String userName;

    public ConvParameters(String serverAddress, int serverPort, String remoteUser) {
        this.hostAddress = serverAddress;
        this.hostPort = serverPort;
        this.userName = remoteUser;
    }
}
