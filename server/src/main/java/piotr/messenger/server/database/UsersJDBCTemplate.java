package piotr.messenger.server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import piotr.messenger.library.Constants;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableAutoConfiguration
public class UsersJDBCTemplate implements UsersDAO<UserSQL> {

    private JdbcTemplate jdbc;
    private final Logger logger = LoggerFactory.getLogger(UsersJDBCTemplate.class);

    public UsersJDBCTemplate(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    private void init() {
        String query = "CREATE TABLE IF NOT EXISTS users(" +
                "  userID       INT AUTO_INCREMENT PRIMARY KEY," +
                "  login        VARCHAR(" + Constants.RECORD_LENGTH + ") NOT NULL UNIQUE," +
                "  password     VARCHAR(" + Constants.RECORD_LENGTH + ") NOT NULL," +
                "  registered   TIMESTAMP NULL" + ")" +
                "  ENGINE = InnoDB;";
        jdbc.execute(query);
    }

    @Override
    public void registerUser(String login, String password) {

        logger.info("REGISTER: '{}' '{}'", login, password);
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO " +
                "users (login, password, registered) " +
                "VALUES (?, ?, ?)",
                login, password, timestamp);
    }

    @Override
    public UserSQL getUser(String login) {

        String query = "SELECT * FROM users WHERE login = ?";
        List<UserSQL> resultList = jdbc.query(query, new String[]{login}, new UserSQLMapper());
        if (resultList.isEmpty()) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    @Override
    public boolean hasUser(String login) {

        String query = "SELECT * FROM users WHERE login = ?";
        return !jdbc.query(query, new String[]{login}, new UserSQLMapper()).isEmpty();

    }

}
