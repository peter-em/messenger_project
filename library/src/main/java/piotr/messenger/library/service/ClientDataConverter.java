package piotr.messenger.library.service;

import piotr.messenger.library.Constants;
import piotr.messenger.library.util.ClientData;

import java.nio.ByteBuffer;

public class ClientDataConverter {

    private static String getStringFromArray(ByteBuffer buffer, int size) {
        byte[] array = new byte[size];
        buffer.get(array, 0, size);
        return new String(array, Constants.CHARSET);
    }

    public static ClientData decodeFromBuffer(ByteBuffer buffer) {
        buffer.flip();
        int bytesToRead = buffer.getInt();
        String login = getStringFromArray(buffer, bytesToRead);
        bytesToRead = buffer.getInt();
        String passwd = getStringFromArray(buffer, bytesToRead);
        bytesToRead = buffer.getInt();
        ClientData clientData = new ClientData(login, passwd, bytesToRead);

        return clientData;
    }

    public static ByteBuffer encodeToBuffer(ClientData clientData) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        buffer.putInt(clientData.getLogin().length());
        buffer.put(clientData.getLogin().getBytes(Constants.CHARSET));
        buffer.putInt(clientData.getPassword().length());
        buffer.put(clientData.getPassword().getBytes(Constants.CHARSET));
        buffer.putInt(clientData.getConnectMode());
        buffer.flip();
        return buffer;
    }

}
