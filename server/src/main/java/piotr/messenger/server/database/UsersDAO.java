package piotr.messenger.server.database;


public interface UsersDAO<T> {

    /**
     * This is the method to be used to register
     * a new user in the table Users.
     */
    void registerUser(String login, String password);

    /**
     * This is the method to be used to select
     * record from table Users, corresponding
     * to a passed user's login.
     */
    T getUser(String login);

    /**
     * This is the method to be used to check
     * if table Users has already record corresponding
     * to a passed user's login.
     */
    boolean hasUser(String login);

}
