package piotr.messenger.server;

public class ConversationEnd {
	public int portNr;
	public ConversationPair convPair;
	public HandleConversation worker;

	ConversationEnd(int portNr, ConversationPair convPair,
								  HandleConversation worker) {
		this.portNr = portNr;
		this.convPair = convPair;
		this.worker = worker;
	}
}

