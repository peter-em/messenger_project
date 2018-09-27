package piotr.messenger.server.database;

import java.util.List;

public interface UsersDAO<T> {

    /**
     * This is the method to be used to register
     * a new user in the table Users.
     */
    public void registerUser(String login, String password);

    /**
     * This is the method to be used to select
     * record from table Users, corresponding
     * to a passed user's login.
     */
    public T getUser(String login);

    /**
     * This is the method to be used to check
     * if table Users has already record corresponding
     * to a passed user's login.
     */
    public boolean hasUser(String login);

    /**
     * This is the method to be used to list down
     * all the records from the Users table.
     */
//    public List<T> listUsers();

    /**
     * This is the method to be used to delete
     * a record from the Users table corresponding
     * to a passed user login.
     */
//    public void delete(String login);

    /**
     * This is the method to be used to update
     * a record into the Users table.
     */
//    public void update(String login, String password);
}
