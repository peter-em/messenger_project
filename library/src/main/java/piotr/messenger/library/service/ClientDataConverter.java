package piotr.messenger.library.service;

import piotr.messenger.library.Constants;
import piotr.messenger.library.util.ClientData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ClientDataConverter {

    private ClientDataConverter() {}

    public static String getStringFromArray(ByteBuffer buffer, byte[] array, int size) {
        buffer.get(array, 0, size);
        return new String(array, 0, size, Constants.CHARSET);
    }

    public static ClientData decodeFromBuffer(ByteBuffer buffer) {
        byte[] array = new byte[Constants.RECORD_LENGTH];
        int bytesToRead = buffer.getInt();
        String login = getStringFromArray(buffer, array, bytesToRead);
        bytesToRead = buffer.getInt();
        String passwd = getStringFromArray(buffer, array, bytesToRead);
        bytesToRead = buffer.getInt();
        return new ClientData(login, passwd, bytesToRead);
    }

    public static ByteBuffer encodeToBuffer(ClientData clientData) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        buffer.putInt(clientData.getLogin().length());
        buffer.put(clientData.getLogin().getBytes(Constants.CHARSET));
        buffer.putInt(clientData.getPassword().length());
        buffer.put(clientData.getPassword().getBytes(Constants.CHARSET));
        buffer.putInt(clientData.getConnectMode());
        return buffer;
    }

    public static List<String> decodeBufferToList(int listSize, ByteBuffer buffer) {
        List<String> clientsNames = new ArrayList<>(listSize);
        byte[] array = new byte[Constants.RECORD_LENGTH];
        for (int i = 0; i < listSize; i++) {
            int bytesToRead = buffer.getInt();
            clientsNames.add(getStringFromArray(buffer, array, bytesToRead));
        }
        return clientsNames;
    }

}
