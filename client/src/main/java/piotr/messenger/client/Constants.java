package piotr.messenger.client;

import java.awt.*;
import java.nio.charset.Charset;

class Constants {
    static final String APP_NAME = "Chat-o-Matt";
    static final String SRVR_ADDRESS = "127.0.0.1";
    static final int PORT_NR = 6125;
    static final int BUFFER_SIZE = 1024;
    static final String CLIENT_PATTERN = "MessageServerClient";
    static final Charset CHARSET = Charset.forName("UTF-8");
    static final int BLOCKING_SIZE = 128;

    static final Color TEXT_AREA_COLOR = new Color(84, 88, 90);
    static final Color TEXT_COLOR = new Color(242,242,242);
}
