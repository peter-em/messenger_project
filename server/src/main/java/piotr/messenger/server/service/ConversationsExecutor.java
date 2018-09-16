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

    private ExecutorService handlersExecutor;
	private List<ConversationPair> pendingPairs;
	private List<ConversationPair> activePairs;
	private ArrayBlockingQueue<ConversationEnd> endDataQueue;
	private int activeWorkersCounter;
	private ConnectionParameters parameters;

    public ConversationsExecutor(ConnectionParameters parameters) {
        pendingPairs = new ArrayList<>(); 	//or maybe LinkedList<> should be used
        activePairs = new ArrayList<>();
        handlersExecutor = Executors.newFixedThreadPool(Constants.CONV_MAX);
        endDataQueue = new ArrayBlockingQueue<>(Constants.CONV_MAX*2);
        activeWorkersCounter = 0;
        this.parameters = parameters;
    }

    public boolean createNewWorker(ConversationPair pair) {

        //conversation request accepted
        pendingPairs.remove(pair);
        activePairs.add(pair);

        if (activeWorkersCounter < Constants.CONV_MAX) {

            //create new handler and send usersDatabase connection data
            //if limit of conversations was not reached
            ConversationWorker worker = new ConversationWorker(parameters.getHostAddress(), parameters.getWorkerPort(), endDataQueue, pair);
            --activeWorkersCounter;
            handlersExecutor.execute(worker);
            return true;
        }
        activePairs.remove(pair);
        return false;
    }

    public void cleanAfterWorker() throws InterruptedException {
        while (!endDataQueue.isEmpty()) {
            ConversationEnd data = endDataQueue.take();
//            handlersPorts.remove(new Integer(data.getPortNr()));
            parameters.deletePort(data.getPortNr());
            activePairs.remove(data.getConvPair());
            --activeWorkersCounter;
        }
    }

    public void addPendingPair(ConversationPair pair) {
        pendingPairs.add(pair);
    }

    public void removePendingPair(ConversationPair pair) {
        pendingPairs.remove(pair);
    }

    public boolean hasPair(ConversationPair pair) {
        return pendingPairs.contains(pair) || activePairs.contains(pair);
    }

    public void terminateExecutor() {
        handlersExecutor.shutdown();
    }

}
