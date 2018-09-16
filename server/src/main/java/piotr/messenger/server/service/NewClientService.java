package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.library.util.ClientData;
import piotr.messenger.server.database.UsersDatabase;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Component
public class NewClientService {

    private UsersDatabase usersDatabase;
    private DataFlowService service;


    public NewClientService(UsersDatabase usersDatabase) {
        this.usersDatabase = usersDatabase;
    }

    public void setDataFlowService(DataFlowService service) {
        this.service = service;
    }

    public boolean handleData(ByteBuffer readBuffer, SocketChannel clientRead) {

        ClientData clientData = ClientDataConverter.decodeFromBuffer(readBuffer);

        //veryfication successful, returning list of connected clients
        //or -1 if provided login is already in use
        boolean response = false;
        if (clientData.getConnectMode() == Constants.LOGIN_MODE) {
            response = usersDatabase.verifyClient(clientData);
        } else if (clientData.getConnectMode() == Constants.REGISTER_MODE) {
            response = usersDatabase.registerClient(clientData);
        }

        // send client a response to veryfication/registration request
        // success (0) or failure (-1)
        readBuffer.clear();
        if (response) {
            usersDatabase.addUser(clientData.getLogin(), clientRead);
            readBuffer.putInt(0);
        } else {
            readBuffer.putInt(-1);
        }
        readBuffer.flip();
        service.send(clientRead, readBuffer.array());

        return response;
    }


}
