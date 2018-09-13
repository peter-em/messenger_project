package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.core.ConversationWorker;
import piotr.messenger.server.util.ClientState;
import piotr.messenger.server.util.ConversationPair;
import piotr.messenger.server.util.UsersDatabase;

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

        String clientData = new String(readBuffer.array(), Constants.CHARSET);
        String clientId = clientData.split(";")[0];


        readBuffer.clear();

        //veryfication successful, returning list of connected clients
        //or -1 if provided login is already in use
        boolean state = false;
        if (usersDatabase.hasUser(clientId)) {
            readBuffer.putInt(-1);
//			    logger.debug("ID IN USE");

        } else {
//			    logger.debug("ID FREE");
            readBuffer.putInt(0);
            usersDatabase.addUser(clientId, clientRead);
            //change client state from waiting for veryfication
            //into enabling conversations
//            state.setState(ClientState.SERVECLIENT);
            state = true;
//            updateClietsUserList(true);
            service.toggleUpdateClients();
        }
        readBuffer.flip();
        service.send(clientRead, readBuffer.array());
        return state;
    }


}
