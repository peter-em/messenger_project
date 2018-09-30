package piotr.messenger.library;

import java.awt.*;
import java.nio.charset.Charset;

public class Constants {

    // COMMON
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int BUFFER_SIZE = 1024;
    public static final int RECORD_LENGTH = 32;

    // CONNECTION PARAMETERS
    public static final int PORT_NR = 6125;
    public static final String HOST_ADDRESS = "127.0.0.1";

    // DATA TRANSFER
    public static final String C_REQUEST = "a";
    public static final String C_TERMINATE = "t";
//    public static final String C_REFUSE = "n";
//    public static final String C_ACCEPT = "y";
    //    public static final String C_CONFIRM = "c";

    // SERVER
    public static final int CONV_MAX = 40;
    public static final String COL_LOGIN = "login";
    public static final String COL_PSSWRD = "password";
    public static final String COL_REGISER = "registered";

    // CLIENT
    public static final String APP_NAME = "Chat-o-Matt";
    public static final String APP_INFO =
                    "Welcome in Chatt-o-Matt\n\n" +
                    "This simple messenger app,\n" +
                    "which originated as university\n" +
                    "project, is under development.\n" +
                    "I use it as learning platform.\n" +
                    "Technologies involved:\n" +
                    "Maven, Spring, SQL, Spock.\n\n" +
                    "Please enter Your \n" +
                    "username and password";
    public static final String LOGIN_ERROR = "Invalid username and/or password!";
    public static final String REGISTER_ERROR = "This username already exists!";
    public static final String HASLOGGED_ERROR = "This user has already logged in!";
    public static final String TOO_SHORT = "Username too short (min. 3 letters)";
    public static final String PSWD_EMPTY = "Please enter password";
    public static final String SWITCH_TO_LOGIN = "Already registered? Click to Log In";
    public static final String SWITCH_TO_REGISTER = "Dont't have account? Register here";
    public static final String LOGIN_BUTTON = "Log In";
    public static final String REGISTER_BUTTON = "Register";
    public static final int BLOCKING_SIZE = 16;
    public static final int LOGIN_MODE = 1;
    public static final int REGISTER_MODE = 2;

    public static final Color TEXT_AREA_COLOR = new Color(84, 88, 90);
    public static final Color TEXT_COLOR = new Color(242,242,242);
    public static final Color DATA_ERROR = new Color(187,0,10);
    public static final Color DARK_BLUE = new Color(0,0,240);
    public static final Color BLACK = new Color(0,0,0);
    public static final Color LIST_ELEMENT = new Color(0,74,12);

    public static final Font AREA_FONT = new Font("Consolas", Font.PLAIN, 13);

}
