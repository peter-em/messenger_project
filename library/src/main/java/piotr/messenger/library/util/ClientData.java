package piotr.messenger.library.util;

public class ClientData {

    private String login;
    private String password;
    private int connectMode;    // as login or register


    public ClientData(String login, String password, int connectMode) {
        this.login = login;
        this.password = password;
        this.connectMode = connectMode;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectMode() {
        return connectMode;
    }

    public void setConnectMode(int connectMode) {
        this.connectMode = connectMode;
    }

    @Override
    public String toString() {
        return "ClientData{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", connectMode=" + connectMode +
                '}';
    }
}
