package piotr.messenger.server.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ServerPorts {

    @Value("${server.int.hostport}")
    private @Getter int hostPort;

    public int getWorkerPort() {
        return hostPort+1;
    }
}
