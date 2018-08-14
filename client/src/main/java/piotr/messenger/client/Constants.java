package piotr.messenger.client;

import java.awt.*;
import java.nio.charset.Charset;

class Constants {
    static final String APP_NAME = "Chat-o-Matt";
    static final String SRVR_ADDRESS = "127.0.0.1";
    static final int PORT_NR = 6125;
    static final int BUFFER_SIZE = 1024;
    static final Charset CHARSET = Charset.forName("UTF-8");
    static final int BLOCKING_SIZE = 128;
    static final String APP_INFO =
            "Welcome in Chatt-o-Matt\n\n" +
            "It is simple messenger app,\n" +
            "created as university project.\n" +
            "I use it as learning platform.\n" +
            "Technologies involved:\n" +
            "Maven, Spring, SLF4J, SQL.\n\n" +
            "Please enter Your \n" +
            "username and password";
    static final String LOGIN_ERROR = "Invalid username and/or password!";
    static final String SIGNUP_ERROR = "This username already exists!";
    static final String TOO_SHORT = "Username too short (min. 3 letters)";
    static final String PSWD_EMPTY = "Please enter password";

    static final Color TEXT_AREA_COLOR = new Color(84, 88, 90);
    static final Color TEXT_COLOR = new Color(242,242,242);
}
