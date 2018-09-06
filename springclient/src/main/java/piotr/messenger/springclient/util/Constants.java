package piotr.messenger.springclient.util;

import java.awt.*;
import java.nio.charset.Charset;

public class Constants {
    public static final String APP_NAME = "Chat-o-Matt";
    public static final String SRVR_ADDRESS = "127.0.0.1";
    public static final int PORT_NR = 6125;
    public static final int BUFFER_SIZE = 1024;
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int BLOCKING_SIZE = 128;
    public static final String APP_INFO =
            "Welcome in Chatt-o-Matt\n\n" +
            "It is simple messenger app,\n" +
            "created as university project.\n" +
            "I use it as learning platform.\n" +
            "Technologies involved:\n" +
            "Maven, Spring, SLF4J, SQL.\n\n" +
            "Please enter Your \n" +
            "username and password";
    public static final String LOGIN_ERROR = "Invalid username and/or password!";
    public static final String SIGNUP_ERROR = "This username already exists!";
    public static final String TOO_SHORT = "Username too short (min. 3 letters)";
    public static final String PSWD_EMPTY = "Please enter password";
    public static final String SWITCH_TO_LOGIN = "Already registered? Click to Log In";
    public static final String SWITCH_TO_REGISTER = "Dont't have account? Register here";
    public static final String LOGIN_BUTTON = "Log In";
    public static final String REGISTER_BUTTON = "Register";
    public static final int LOGIN_MODE = 1;
    public static final int REGISTER_MODE = 2;

    public static final Color TEXT_AREA_COLOR = new Color(84, 88, 90);
    public static final Color TEXT_COLOR = new Color(242,242,242);
    public static final Color DATA_ERROR = new Color(187,0,10);
    public static final Color DARK_BLUE = new Color(0,0,240);
    public static final Color BLACK = new Color(0,0,0);

    public static final Font AREA_FONT = new Font("Consolas", Font.PLAIN, 13);

}
