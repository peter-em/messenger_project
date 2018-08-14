package piotr.messenger.server;

import java.nio.charset.Charset;

class Constants {

    // connection parameters
    static final int PORT = 6125;
    static final String HOST_NAME = "127.0.0.1";

    // server constants
    static final int CONV_MAX = 40;
    static final int BUFF_SIZE = 1024;
    static final String C_ASK = "a";
    static final String C_REFUSE = "n";
    static final String C_ACCEPT = "y";
    static final String C_CONFIRM = "c";
    static final Charset CHARSET = Charset.forName("UTF-8");
}
