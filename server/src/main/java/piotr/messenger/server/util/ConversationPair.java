package piotr.messenger.server.util;

import lombok.Data;

import java.nio.channels.SocketChannel;

@Data
public class ConversationPair {

	private SocketChannel client1;
	private SocketChannel client2;

    public ConversationPair(SocketChannel client1, SocketChannel client2) {
        this.client1 = client1;
        this.client2 = client2;
    }

    public boolean hasNullClient() {
		return client1 == null || client2 == null;
	}

	public void addClient(SocketChannel client) {
        if (client1 == null) {
            client1 = client;
        } else if (client2 == null) {
            client2 = client;
        }
    }

	public SocketChannel getOtherClient(SocketChannel client) {
        return client.equals(client1)?client2:client1;
    }
}

