package piotr.messenger.library.service;

import piotr.messenger.library.Constants;
import piotr.messenger.library.util.ClientData;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ClientDataConverter {

    private ClientDataConverter() {}

    public static String getStringFromArray(ByteBuffer buffer, byte[] array, int size) {
        buffer.get(array, 0, size);
        return new String(array, 0, size, Constants.CHARSET);
    }

    public static ClientData decodeAuthFromBuffer(ByteBuffer buffer) {
        int bytesToRead = buffer.getInt();
        byte[] array = new byte[bytesToRead];
        String login = getStringFromArray(buffer, array, bytesToRead);
        bytesToRead = buffer.getInt();
        array = new byte[bytesToRead];
        String passwd = getStringFromArray(buffer, array, bytesToRead);
        bytesToRead = buffer.getInt();
        if (bytesToRead == Constants.LOGIN_MODE
                || bytesToRead == Constants.REGISTER_MODE) {
            return new ClientData(login, passwd, bytesToRead);
        } else {
            throw new BufferUnderflowException();
        }
    }

    public static ByteBuffer encodeAuthToBuffer(ClientData clientData) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        byte[] data = clientData.getLogin().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
        data = clientData.getPassword().getBytes(Constants.CHARSET);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.putInt(clientData.getConnectMode());
        return buffer;
    }

    public static List<String> decodeBufferToList(int listSize, ByteBuffer buffer) {
        List<String> clientsNames = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            int bytesToRead = buffer.getInt();
            byte[] array = new byte[bytesToRead];
            clientsNames.add(getStringFromArray(buffer, array, bytesToRead));
        }
        return clientsNames;
    }

}
