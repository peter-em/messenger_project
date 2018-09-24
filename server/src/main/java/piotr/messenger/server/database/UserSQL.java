package piotr.messenger.server.database;

import lombok.Data;

import java.sql.Timestamp;

@Data
class UserSQL {

    private String login;
    private String password;
    private Timestamp registered;

}
