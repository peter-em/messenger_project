package piotr.messengerproject.server;


public class ClientState {

	public static final int WERIFYAPP = 0;
	public static final int WAITFORUID = 1;
	public static final int SERVECLIENT = 3;
	//public static final int HASCONV = 4;

	private int state;

	ClientState() {
		state = WERIFYAPP;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
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

