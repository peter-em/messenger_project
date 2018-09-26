package piotr.messenger.server.database

import piotr.messenger.library.Constants
import spock.lang.Specification
import spock.lang.Subject

import java.sql.ResultSet
import java.sql.Timestamp

class UserSQLMapperTest extends Specification {

    def login = "login"
    def password = "password11"
    def timestamp = "2018-01-06 13:37:00"
    @Subject UserSQLMapper mapper = new UserSQLMapper()

    def "Should return UserSQL object from resultset"() {
        given:"valid resultset"
        def rs = getRS()

        when:"mapper 'mapRow' method is invoked"
        def user = mapper.mapRow(rs,0)

        then:"user should have fields set properly"
        user.getLogin() == login
        user.getPassword() == password
        user.getRegistered() == Timestamp.valueOf(timestamp)
    }

    def getRS() {
        ResultSet rs = Stub()
        rs.getString(Constants.COL_LOGIN) >> login
        rs.getString(Constants.COL_PSSWRD) >> password
        rs.getTimestamp(Constants.COL_REGISER) >> Timestamp.valueOf(timestamp)
        rs
    }

}
