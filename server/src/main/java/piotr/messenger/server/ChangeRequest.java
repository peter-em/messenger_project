package piotr.messenger.server;

import java.nio.channels.SocketChannel;

public class ChangeRequest {

	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;

	public SocketChannel socket;
	public int type;
//	public int ops;

	ChangeRequest(SocketChannel socket, int type) {
		this.socket = socket;
		this.type = type;
//		this.ops = ops;
	}

	@Override
	public String toString() {
		return "ChangeRequest{" +
				  "socket=" + socket +
				  ", type=" + type +
//				  ", ops=" + ops +
				  '}';
	}
}
