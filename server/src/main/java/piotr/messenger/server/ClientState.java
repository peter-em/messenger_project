package piotr.messenger.server;

public class ClientState {

	static final int WAITFORUID = 1;
	static final int SERVECLIENT = 3;
	//public static final int HASCONV = 4;

	private int state;

	ClientState() {
        state = WAITFORUID;
	}

	int getState() {
		return state;
	}

	void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClientState that = (ClientState) o;

		return getState() == that.getState();
	}

	@Override
	public int hashCode() { return getState(); }
}

