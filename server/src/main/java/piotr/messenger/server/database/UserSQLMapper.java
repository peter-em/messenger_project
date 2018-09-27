package piotr.messenger.server.database;

import org.springframework.jdbc.core.RowMapper;
import piotr.messenger.library.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSQLMapper implements RowMapper<UserSQL> {

    @Override
    public UserSQL mapRow(ResultSet resultSet, int i) throws SQLException {
        UserSQL user = new UserSQL();
        user.setLogin(resultSet.getString(Constants.COL_LOGIN));
        user.setPassword(resultSet.getString(Constants.COL_PSSWRD));
        user.setRegistered(resultSet.getTimestamp(Constants.COL_REGISER));
        return user;
    }
}
