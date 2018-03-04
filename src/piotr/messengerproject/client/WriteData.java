package piotr.messengerproject.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * class used to send conversation data to server
 * */

public class WriteData implements Runnable {

	private ByteBuffer buffer;
	private SocketChannel channel;
	private ReadData reader;
	private volatile boolean isRunning;
	private ArrayBlockingQueue<String> receiveDataQueue;

	WriteData(SocketChannel channel, ReadData reader, ArrayBlockingQueue<String> queue) {
		this.channel = channel;
		this.reader = reader;
		buffer = ByteBuffer.allocate(ClientGUI.BUFFER_SIZE*2);
		receiveDataQueue = queue;
	}

	@Override
	public void run() {

		isRunning = true;
		try {
			String inputArray[];
			String input;
			while (isRunning) {

				if (!receiveDataQueue.isEmpty()) {

					try {
						input = receiveDataQueue.take();
					} catch (InterruptedException bqEx) {
						System.out.println(bqEx.getMessage());
						continue;
					}

					inputArray = input.split(";");
					if (inputArray[0].equalsIgnoreCase("s"))
						break;

					buffer.clear();
					buffer.put(inputArray[1].getBytes(ClientGUI.CHARSET));
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

	public boolean isRunning() { return isRunning; }

	public void stopWorker() { isRunning = false; }
}

