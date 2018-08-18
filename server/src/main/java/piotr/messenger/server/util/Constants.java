package piotr.messenger.server.util;

import java.nio.charset.Charset;

public class Constants {

    // connection parameters
    public static final int PORT = 6125;
    public static final String HOST_NAME = "127.0.0.1";

    // server constants
    public static final int CONV_MAX = 40;
    public static final int BUFF_SIZE = 1024;
    public static final String C_ASK = "a";
    public static final String C_REFUSE = "n";
    public static final String C_ACCEPT = "y";
//    public static final String C_CONFIRM = "c";
    public static final Charset CHARSET = Charset.forName("UTF-8");
}
