package piotr.messenger.server.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.library.util.ClientData;
import piotr.messenger.server.database.UsersDatabase;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Component
@AllArgsConstructor
public class NewClientService {

    private final UsersDatabase usersDatabase;
    private final ClientsConnectionService connectionService;


    public int handleData(ByteBuffer readBuffer, SocketChannel clientRead) {

        ClientData clientData;
        try {
            clientData = ClientDataConverter.decodeAuthFromBuffer(readBuffer);
        } catch (BufferUnderflowException | IndexOutOfBoundsException ex) {
            return -4;
        }

        if (clientData.getConnectMode() == Constants.LOGIN_MODE
                && connectionService.isAuthenticated(clientData.getLogin())) {
            return -2;
        }

        //perform client verification
        boolean isVerified = false;
        if (clientData.getConnectMode() == Constants.LOGIN_MODE) {
            isVerified = usersDatabase.verifyClient(clientData);
        } else if (clientData.getConnectMode() == Constants.REGISTER_MODE) {
            isVerified = usersDatabase.registerClient(clientData);
        }


        if (isVerified) {
            connectionService.addUser(clientData.getLogin(), clientRead);
            return 0;
        }
        return -1;
    }

}
