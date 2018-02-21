package piotr.messengerproject.server;

/**
 * Created by Piotr on 09.01.2017.
 */
public class ConversationEnd {
	public int portNr;
	public ConversationPair convPair;
	public HandleConversation worker;

	public ConversationEnd(int portNr, ConversationPair convPair,
								  HandleConversation worker) {
		this.portNr = portNr;
		this.convPair = convPair;
		this.worker = worker;
	}
}

