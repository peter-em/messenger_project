package piotr.messenger.server;

import java.nio.channels.SocketChannel;


public class ConversationPair {

	SocketChannel client1;
	SocketChannel client2;

	ConversationPair(SocketChannel client1, SocketChannel client2) {
		this.client1 = client1;
		this.client2 = client2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConversationPair that = (ConversationPair) o;

		return client1 == that.client1 && client2 == that.client2
				  || client1 == that.client2 && client2 == that.client1;
	}

	@Override
	public int hashCode() {
		int result = client1 != null ? client1.hashCode() : 0;
		result = 31 * result + (client2 != null ? client2.hashCode() : 0);
		return result;
	}

	boolean hasNullClient() {
		return client1 == null || client2 == null;
	}
}

