package piotr.messenger.library.util;

import java.util.Objects;

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
        return "ClientData[" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", connectMode=" + connectMode + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientData that = (ClientData) o;
        return getConnectMode() == that.getConnectMode() &&
                Objects.equals(getLogin(), that.getLogin()) &&
                Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getLogin(), getPassword(), getConnectMode());
    }
}
