package piotr.messenger.server.util;

import lombok.Data;

@Data
public class ConversationEnd {

	private final int portNr;
    private final ConversationPair convPair;

}

