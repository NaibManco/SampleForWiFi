package com.manco.sample.util;

/**
 * Created by Manco on 2016/10/9.
 */
public class Global {
    public static final int UNKNOWN = -10;
    public static final int AP_AVAILABLE = 0;

    public static final class WiFiState {
        public static final int WIFI_ENABLED = 1;
        public static final int WIFI_ENABLEING = 2;
        public static final int WIFI_DISABLED = 3;
    }

    public static final class ConnectState {
        public static final int AUTHENTICATE_FAILURE =4;
        public static final int CONNECT_SUCCESS = 5;
        public static final int CONNECT_FAILURE = 6;
    }
}
