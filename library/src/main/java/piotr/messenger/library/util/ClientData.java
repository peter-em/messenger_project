package piotr.messenger.library.util;

import lombok.Data;


@Data
public class ClientData {

    private String login;
    private String password;
    private int connectMode;    // as login or register

    public ClientData() {}

    public ClientData(String login, String password, int connectMode) {
        this.login = login;
        this.password = password;
        this.connectMode = connectMode;
    }
}
