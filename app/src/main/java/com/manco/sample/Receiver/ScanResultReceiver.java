package com.manco.sample.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.manco.sample.entity.AccessPoint;
import com.manco.sample.util.Courier;
import com.manco.sample.util.Global;
import com.manco.sample.util.WiFiHandler;

import java.util.List;

/**
 * Created by Manco on 2016/10/9.
 */
public class ScanResultReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WIFIX",intent.getAction());
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            boolean isScanned = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
            if (isScanned) {
                List<AccessPoint> aps = WiFiHandler.instance().getScanAp();
                if (aps != null) {
                    Courier.send(Global.AP_AVAILABLE,WiFiHandler.mergeRelativeAPs(aps));
                }
            }
        }
    }
}
