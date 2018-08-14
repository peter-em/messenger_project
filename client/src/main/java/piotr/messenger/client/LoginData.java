package piotr.messenger.client;

public class LoginData {
    private String login;
    private String password;

    /*LoginData(String login, String password) {
        this.login = login;
        this.password = password;
    }*/

    String getLogin() {
        return login;
    }

    void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }
}
