package piotr.messenger.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * class used to read data in conversation from server
 * */

public class ReadData implements Runnable {

	private ByteBuffer buffer;
	private SocketChannel channel;
	private volatile boolean isRunning;
	private ArrayBlockingQueue<String> sendDataQueue;
	private WriteData writer;

	ReadData(SocketChannel channel, ArrayBlockingQueue<String> queue) {
		this.channel = channel;
		buffer = ByteBuffer.allocate(ClientGUI.BUFFER_SIZE*2);
		sendDataQueue = queue;
	}

	@Override
	public void run() {
		byte[] data;
		int count;
		int bytesRead;
		String receive;
		isRunning = true;

		try {
			while (isRunning) {
				buffer.clear();
				bytesRead = channel.read(buffer);

				if (bytesRead == -1) {
					try {
						sendDataQueue.put("");
					} catch (InterruptedException itrEx) {
						itrEx.printStackTrace();
					}
					break;
				}

				while (bytesRead != 0) {

					buffer.flip();
					count = buffer.remaining();
					data = new byte[count];
					System.arraycopy(buffer.array(), 0, data, 0, count);

					receive = new String(data, ClientGUI.CHARSET);

					try {
						sendDataQueue.put(receive);
					} catch (InterruptedException itrEx) {
						itrEx.printStackTrace();
					}

					buffer.clear();
					bytesRead = channel.read(buffer);
				}


				try {
					TimeUnit.MILLISECONDS.sleep(40);
				} catch (InterruptedException itrEx) {
					itrEx.printStackTrace();
				}
			}
		} catch (IOException ioEx) {
			System.out.println(ioEx.getMessage());
		}
		isRunning = false;
		if (writer.isRunning())
			writer.stopWorker();

		try {
			channel.close();
		} catch (IOException ioex) {
			System.err.println("Fatall error while closing socketChannel");
		}
	}

	public void setWriter(WriteData writer) {
		this.writer = writer;
	}

	public void stopWorker() {
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
}

