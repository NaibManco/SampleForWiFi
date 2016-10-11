package com.manco.sample.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.manco.sample.entity.AccessPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Manco on 2016/10/9.
 */
public class WiFiHandler {
    /**
     * an entrance to access wifi
     */
    private WifiManager wifiManager;

    private static WiFiHandler _instance;

    private WiFiHandler(){}

    public static WiFiHandler instance() {
        if (null == _instance) {
            synchronized (WiFiHandler.class) {
                if (null == _instance) {
                    _instance = new WiFiHandler();
                }
            }
        }
        return _instance;
    }

    /**
     * init WifiManager
     *
     * @param context
     * @return true if WifiManager is inited ,false or not
     */
    public boolean init(Context context) {
        if (null == context) {
            return false;
        }

        if (null == wifiManager) {
            wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
        }

        if (null != wifiManager) {
            return true;
        }
        return false;
    }

    /**
     * open wifi
     * @return true if operation success,false or not
     */
    public boolean openWifi() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }

        switch (wifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_ENABLED:
                return true;
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
            case WifiManager.WIFI_STATE_DISABLED:
            default:
                return wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * close Wifi
     *
     * @return true if operation success,false or not
     */
    public boolean closeWifi() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }

        switch (wifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_DISABLING:
                return true;
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_ENABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
            default:
                return wifiManager.setWifiEnabled(false);
        }
    }

    public boolean isWifEnabled() {
        if (wifiManager == null) {
            return false;
        }
        return wifiManager.isWifiEnabled();
    }

    public void startScan() {
        if (null != wifiManager) {
            wifiManager.startScan();
        } else {
            Log.e("WIFIX","WifiManager is null");
        }
    }

    public List<AccessPoint> getScanAp() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return null;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        if (results == null || results.size() <= 0) {
            Log.e("WIFIX","ScanResults are null");
            wifiManager.startScan();
            return null;
        }

        List<AccessPoint> aps = new ArrayList<AccessPoint>();
        for (ScanResult result : results) {
            if (TextUtils.isEmpty(result.SSID)) {
                continue;
            }

            AccessPoint accessPoint = new AccessPoint();
            accessPoint.setSsid(result.SSID);
            accessPoint.setBssid(result.BSSID);
            accessPoint.setEncryptionType(result.capabilities);
            try {
                double level = calculateSignalLevel(result.level, 5.0f) / 5.0;
                /**
                 * in some language such as portuguese,format will fail for unknown reason
                 */
                Locale.setDefault(Locale.US);
                DecimalFormat df = new DecimalFormat("#.##");
                level = Double.parseDouble(df.format(level));
                accessPoint.setSignalStrength((float) level * 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int networkId = isConfigured(accessPoint);
            if (networkId > -1) {
                accessPoint.setNetworkId(networkId);
            }
            aps.add(accessPoint);
        }
        return aps;
    }

    public static List<AccessPoint> mergeRelativeAPs(List<AccessPoint> aps) {
        ArrayList<AccessPoint> resultAPs = new ArrayList<AccessPoint>();
        while (aps.size() > 0) {
            for (int i = 0; i < aps.size(); i++) {
                AccessPoint tempAp = aps.get(i);
                ArrayList<AccessPoint> relativeAPs = new ArrayList<AccessPoint>();
                relativeAPs.add(tempAp);
                for (int j = i + 1; j < aps.size(); j++) {
                    AccessPoint tempAp1 = aps.get(j);
                    if (tempAp.getSsid().trim()
                            .equals(tempAp1.getSsid().trim())) {
                        if (!tempAp.getBssid().equals(tempAp1.getBssid())) {
                            relativeAPs.add(tempAp1);
                        }
                    }
                }

                aps.removeAll(relativeAPs);
                if (relativeAPs.size() > 1) {
                    AccessPoint mainAp = relativeAPs.get(0);
                    relativeAPs.remove(0);
                    mainAp.setRelativeAPs(relativeAPs);
                    resultAPs.add(mainAp);
                    break;
                } else {
                    resultAPs.add(tempAp);
                    break;
                }
            }
        }

        return resultAPs;
    }

    /**
     * get a list of accesspoints which were connected to and configured before
     *
     * @return List
     */
    public List<WifiConfiguration> getConfigedAp() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return null;
        }

        return wifiManager.getConfiguredNetworks();
    }

    /**
     * create a WifiConfiguration for connection operation
     *
     * @param ap A Wifi hotspot
     * @return
     */
    public WifiConfiguration createConfiguration(AccessPoint ap) {
        String SSID = ap.getSsid();
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + SSID + "\"";

        String encryptionType = ap.getEncryptionType();
        String password = ap.getPassword();
        if (encryptionType.contains("wep")) {
//            config.hiddenSSID = false;
            /**
             * special handling according to password length is a must for wep
             */
            int i = password.length();
            if (((i == 10 || (i == 26) || (i == 58))) && (password.matches("[0-9A-Fa-f]*"))) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = "\"" + password + "\"";
            }

            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (encryptionType.contains("wpa")) {
            config.preSharedKey = "\"" + password + "\"";
//            config.hiddenSSID = false;

            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            config.status = WifiConfiguration.Status.CURRENT;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    public boolean connect(int networkId) {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }

        if (wifiManager.enableNetwork(networkId, true)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean connect(AccessPoint ap) {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }

        WifiConfiguration config = createConfiguration(ap);

        /**
         * networkId is bigger than 0 in most time, 0 in few time and smaller than 0 in no time
         */
        int networkId = networkId = wifiManager.addNetwork(config);
        if (networkId < 0) {
            return false;
        }

        if (wifiManager.enableNetwork(networkId, true)) {
            /**
             * connect operation success,not trully connected
             */
            return true;
        } else {
            /**
             * some useless help to reconnect while enableNetwork fail
             */
            if (wifiManager.saveConfiguration()) {
                if (wifiManager.reconnect()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * check if a hotspot has ever connected or not been removed
     *
     * @param ap
     * @return configured networkId,or -1 if not found
     */
    public int isConfigured(AccessPoint ap) {
        List<WifiConfiguration> configurations = getConfigedAp();
        if (configurations == null || configurations.size() <= 0) {
            Log.d("WIFIX","Config Aps are empty");
            return -1;
        }

        for (WifiConfiguration configuration : configurations) {
            /**
             * ssid in WifiConfiguration is always like "CCMC",and bssid is always null
             */
            if (configuration.SSID.replace("\"","").trim().equals(ap.getSsid())) {
                return configuration.networkId;
            }
        }
        return -1;
    }

    /**
     * check if the certain ap is connected
     *
     * @param ap
     * @return
     */
    public boolean isConnected(AccessPoint ap) {
        WifiInfo info = getConnectionInfo();
        if (!wifiManager.isWifiEnabled()) {
            return false;
        }
        if (info == null) {
            return false;
        }
        if (info.getSSID().replace("\"","").equals(ap.getSsid())) {
            return true;
        }
        return false;
    }

    /**
     * disconnect the current connected hotspot
     *
     * @return
     */
    public boolean disconnect() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }
        return wifiManager.disconnect();
    }

    /**
     * forget a ever connected hotspot
     *
     * @param networkId
     * @return
     */
    public boolean remove(int networkId) {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return false;
        }
        /**
         * remove operation always fails above api 21,so we try many times
         */
        boolean isRemoved = wifiManager.removeNetwork(networkId);
        if (!isRemoved) {
            int index = 0;
            while (!isRemoved && index < 10) {
                index ++;
                isRemoved = wifiManager.removeNetwork(networkId);
            }
        }

        if (isRemoved) {
            wifiManager.saveConfiguration();
        }

        return isRemoved;
    }

    /**
     * return a nonull WifiInfo when some wifi is connected,and null when wifi
     * disconnected
     *
     * @return
     */
    public WifiInfo getConnectionInfo() {
        if (null == wifiManager) {
            Log.e("WIFIX","WifiManager is null");
            return null;
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        if (SupplicantState.COMPLETED != info.getSupplicantState()) {
            return null;
        }
        if (-1 == info.getNetworkId()) {
            return null;
        }
        if (0 == info.getIpAddress()) {
            return null;
        }

        return info;
    }

    public static float calculateSignalLevel(int rssi, float numLevels) {
        int MIN_RSSI = -100;
        int MAX_RSSI = -55;
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels;
        } else {
            float inputRange = (MAX_RSSI - MIN_RSSI);
            float outputRange = numLevels;
            return (float) (rssi - MIN_RSSI) * outputRange / inputRange;
        }
    }
}
