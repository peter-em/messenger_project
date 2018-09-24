package piotr.messenger.server.util;

import lombok.Data;

import java.nio.channels.SocketChannel;

@Data
public class ConversationPair {

	private final SocketChannel client1;
	private final SocketChannel client2;

    public boolean hasNullClient() {
		return client1 == null || client2 == null;
	}
}

