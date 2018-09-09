package piotr.messenger.client.util;

//@Component
public class LoginData {

    private String login;
    private String password;
    private int mode;


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

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isInLogInMode() {
        return mode == 1;
    }

    public void clearData() {
        login = "";
        password = "";
    }
}
