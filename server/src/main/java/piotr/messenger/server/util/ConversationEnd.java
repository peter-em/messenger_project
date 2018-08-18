package piotr.messenger.server.util;

public class ConversationEnd {
	private int portNr;
    private ConversationPair convPair;

	public ConversationEnd(int portNr, ConversationPair convPair) {
		this.portNr = portNr;
		this.convPair = convPair;
	}

    public int getPortNr() {
        return portNr;
    }

    public ConversationPair getConvPair() {
        return convPair;
    }

}

