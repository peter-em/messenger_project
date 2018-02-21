package piotr.messengerproject.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Pijotr on 2016-12-26.
 */
public class WriteData implements Runnable {

	private final Charset charset = Charset.forName("UTF-8");
	private final int bufferSize = 1024;
	private ByteBuffer buffer;
	private SocketChannel channel;
	private ReadData reader;
	private volatile boolean isRunning;
	private ArrayBlockingQueue receiveDataQueue;

	public WriteData(SocketChannel channel, ReadData reader, ArrayBlockingQueue queue) {
		this.channel = channel;
		buffer = ByteBuffer.allocate(bufferSize);
		this.reader = reader;
		receiveDataQueue = queue;
	}

	@Override
	public void run() {

		isRunning = true;
		try {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String inputArray[];
			String input;
			while (isRunning) {

				//System.out.println("petla write");


				//if (stdIn.ready()) {
				//System.out.println("sa dane");

				if (!receiveDataQueue.isEmpty()) {

					//if ((input = stdIn.readLine()) != null) {
					try {
						input = (String)receiveDataQueue.take();
						//sendOK = true;
					} catch (InterruptedException bqEx) {
						System.out.println(bqEx.getMessage());
						input = "";
						//sendOK = false;
						continue;
					}

					inputArray = input.split(";");
					if (inputArray[0].equalsIgnoreCase("s"))
						break;

					//if (input.length() == 0)
					//	continue;

					buffer.clear();
					buffer.put(inputArray[1].getBytes(charset));
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

		//System.out.println("zatrzymywanie readera");
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

	public void stopWorker() {
		isRunning = false;
	}
}

