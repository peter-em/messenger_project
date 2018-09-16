package piotr.messenger.server.database;

import java.sql.Timestamp;

public class UserSQL {

    private String login;
    private String password;
    private Timestamp registered;

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

    public Timestamp getRegisterDate() {
        return registered;
    }

    public void setRegisterDate(Timestamp registered) {
        this.registered = registered;
    }

    @Override
    public String toString() {
        return "User[" + login + ", " + password +
                ", " + registered + ']';
    }
}
