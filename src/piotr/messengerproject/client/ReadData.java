package piotr.messengerproject.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Pijotr on 2016-12-26.
 */
public class ReadData implements Runnable {

	private final Charset charset = Charset.forName("UTF-8");
	private final int bufferSize = 1024;
	private ByteBuffer buffer;
	private SocketChannel channel;
	private volatile boolean isRunning;
	private ArrayBlockingQueue sendDataQueue;
	private WriteData writer;

	public ReadData(SocketChannel channel, ArrayBlockingQueue queue) {
		this.channel = channel;
		buffer = ByteBuffer.allocate(bufferSize);
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

					//System.out.print("Serwer: ");

					receive = new String(data, charset);
					//System.out.println(receive);
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

