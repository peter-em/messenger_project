package piotr.messenger.server.service;

import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;
import piotr.messenger.server.core.ConversationWorker;
import piotr.messenger.server.util.ConversationEnd;
import piotr.messenger.server.util.ConversationPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ConversationsExecutor {

    private final ExecutorService handlersExecutor;
	private final List<ConversationPair> activePairs;
	private final ArrayBlockingQueue<ConversationEnd> endDataQueue;
	private final ConnectionParameters parameters;
	private int activeWorkersCounter;

    public ConversationsExecutor(ConnectionParameters parameters) {
        activePairs = new ArrayList<>();
        handlersExecutor = Executors.newFixedThreadPool(Constants.CONV_MAX);
        endDataQueue = new ArrayBlockingQueue<>(Constants.CONV_MAX*2);
        activeWorkersCounter = 0;
        this.parameters = parameters;
    }

    public boolean createNewWorker(ConversationPair pair) {

        //conversation request accepted
        if (!activePairs.contains(pair) && activeWorkersCounter < Constants.CONV_MAX) {
            activePairs.add(pair);

            //create new handler and send usersDatabase connection data
            //if limit of conversations was not reached
            ConversationWorker worker = new ConversationWorker(parameters.getHostAddress(), parameters.getWorkerPort(), endDataQueue, pair);
            --activeWorkersCounter;
            handlersExecutor.execute(worker);
            return true;
        }
        return false;
    }

    public void cleanAfterWorker() throws InterruptedException {
        while (!endDataQueue.isEmpty()) {
            ConversationEnd data = endDataQueue.take();
            parameters.deletePort(data.getPortNr());
            activePairs.remove(data.getConvPair());
            --activeWorkersCounter;
        }
    }

    public void terminateExecutor() {
        handlersExecutor.shutdown();
    }

}
