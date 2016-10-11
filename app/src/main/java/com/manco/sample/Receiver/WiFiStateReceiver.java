package com.manco.sample.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Message;

import com.manco.sample.util.Courier;
import com.manco.sample.util.Global;

/**
 * Created by Manco on 2016/10/9.
 */
public class WiFiStateReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_DISABLED);
            Message message = Message.obtain();
            switch (wifistate) {
                case WifiManager.WIFI_STATE_DISABLED:
                    message.what = Global.WiFiState.WIFI_DISABLED;
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    message.what = Global.WiFiState.WIFI_ENABLED;
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    message.what = Global.WiFiState.WIFI_ENABLEING;
                    break;
                default:
                    message.what = Global.UNKNOWN;
                    break;
            }
            Courier.send(message);
        }
    }
}
