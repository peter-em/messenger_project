package piotr.messenger.client.core;

import piotr.messenger.client.util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * class used to send conversation data to the server
 * */
public class Writer implements Runnable {

	private ByteBuffer buffer;
	private SocketChannel channel;
    private volatile boolean isRunning;
	private Reader reader;
	private ArrayBlockingQueue<String> receiveDataQueue;

	Writer(SocketChannel channel, Reader reader, ArrayBlockingQueue<String> queue) {
		this.channel = channel;
		this.reader = reader;
		buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE*2);
		receiveDataQueue = queue;
	}

	@Override
	public void run() {

		isRunning = true;
		try {
			String input;
			while (isRunning) {

				if (!receiveDataQueue.isEmpty()) {

					try {
						input = receiveDataQueue.take();
					} catch (InterruptedException bqEx) {
						System.out.println(bqEx.getMessage());
						continue;
					}

                    if (input.length() == 0)
                        break;

					buffer.clear();
                    buffer.put(input.getBytes(Constants.CHARSET));
					buffer.flip();
					channel.write(buffer);
					buffer.clear();

				}
				try {
					TimeUnit.MILLISECONDS.sleep(40);
				} catch (InterruptedException trit) {
					trit.printStackTrace();
				}
			}
		} catch (IOException ioEx) {
			System.out.println(ioEx.getMessage());
		}

		isRunning = false;
		if (reader.isRunning())
			reader.stopWorker();

		try {
			channel.close();
		} catch (IOException ioex) {
			System.err.println("Fatall error while closing socketChannel");
		}
	}

	void stopWorker() { isRunning = false; }

	boolean isRunning() { return isRunning; }
}

