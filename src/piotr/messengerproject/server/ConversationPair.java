package piotr.messengerproject.server;

import java.nio.channels.SocketChannel;

/**
 * Created by Pijotr on 2016-12-29.
 */
public class ConversationPair {

	public SocketChannel client1;
	public SocketChannel client2;

	public ConversationPair(SocketChannel client1, SocketChannel client2) {
		this.client1 = client1;
		this.client2 = client2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConversationPair that = (ConversationPair) o;

		if (client1 == that.client1 && client2 == that.client2)
			return true;
		if (client1 == that.client2 && client2 == that.client1)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result = client1 != null ? client1.hashCode() : 0;
		result = 31 * result + (client2 != null ? client2.hashCode() : 0);
		return result;
	}

	public boolean hasClient(SocketChannel client) {
		if (client == client2)
			return true;
		return false;
	}
}

