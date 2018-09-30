package piotr.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.library.service.ClientDataConverter;
import piotr.messenger.server.util.ConversationPair;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;


@Component
public class ConversationService {

    private ConversationsExecutor executor;
    private ClientsConnectionService connectionService;
    private ConnectionParameters parameters;
    private DataFlowService service;

    public void setDataFlowService(DataFlowService service) {
        this.service = service;
    }

    public void handleData(ByteBuffer readBuffer, SocketChannel clientRead) {

        //Veryfied client tries to create new conversation
        List<String> request = ClientDataConverter.decodeListFromServer(3, readBuffer);
        readBuffer.clear();
        //request.get(0) holds info about type of message (ask for, refuse, confirm conversation)
        //request.get(1) holds info about receiver of this request
        //request.get(2) holds login of request sender

        if (request.get(0).equals(Constants.C_REQUEST)) {

            ConversationPair pair = new ConversationPair(clientRead, connectionService.getChannel(request.get(1)));
            if (pair.hasNullClient()) {
                return;
            }

            if (executor.createNewWorker(pair)) {

                readBuffer.putInt(-40);     // insert messege type
                String portAsString = Integer.toString(parameters.getLastUsedPort());
                readBuffer.putInt(portAsString.length())
                            .put(portAsString.getBytes(Constants.CHARSET));    // insert conv port of created handler
                readBuffer.putInt(parameters.getHostAddress().length())
                            .put(parameters.getHostAddress().getBytes(Constants.CHARSET));    // insert address of handler
                readBuffer.putInt(request.get(1).length())
                            .put(request.get(1).getBytes(Constants.CHARSET));
                readBuffer.putInt(request.get(2).length())
                            .put(request.get(2).getBytes(Constants.CHARSET));

                readBuffer.flip();
                service.send(pair.getClient1(), readBuffer.array());
                service.send(pair.getClient2(), readBuffer.array());
            }
        }
    }

    @Autowired
    public void setExecutor(ConversationsExecutor executor) {
        this.executor = executor;
    }

    @Autowired
    public void setConnectionService(ClientsConnectionService connectionService ) {
        this.connectionService = connectionService;
    }

    @Autowired
    public void setParameters(ConnectionParameters parameters) {
        this.parameters = parameters;
    }
}
