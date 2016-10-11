package com.manco.sample;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.manco.sample.Receiver.ConnectedStateReceiver;
import com.manco.sample.Receiver.ScanResultReceiver;
import com.manco.sample.Receiver.WiFiStateReceiver;
import com.manco.sample.entity.APAdapter;
import com.manco.sample.entity.AccessPoint;
import com.manco.sample.entity.ListPopupWindowAdapter;
import com.manco.sample.util.ConnectHandler;
import com.manco.sample.util.Courier;
import com.manco.sample.util.Global;
import com.manco.sample.util.WiFiHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WiFiStateReceiver wiFiStateReceiver;
    private ScanResultReceiver scanResultReceiver;
    private ConnectedStateReceiver connectedStateReceiver;

    private WiFiHandler wiFiHandler;

    private TextView wifiSwitch;
    private View wifiOffView;
    private RecyclerView recyclerView;
    private APAdapter apAdapter;
    private List<AccessPoint> currentAps;

    private HashMap<String,Integer> configuredAP;

    private Handler mainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("WIFIX", "message : " + msg.what);
            switch (msg.what) {
                case Global.AP_AVAILABLE:
                    if (currentAps == null) {
                        currentAps = new ArrayList<AccessPoint>();
                    }
                    currentAps.clear();
                    currentAps.addAll((List<AccessPoint>)msg.obj);

                    if (apAdapter == null) {
                        apAdapter = new APAdapter(currentAps);
                        apAdapter.setOnItemClickListener(onItemClickListener);
                        if (null != recyclerView) {
                            recyclerView.setAdapter(apAdapter);
                        }
                    } else {
                        apAdapter.notifyDataSetChanged();
                    }
                    break;
                case Global.ConnectState.AUTHENTICATE_FAILURE:
                    showToast("wrong password");
                case Global.ConnectState.CONNECT_FAILURE:
                case Global.ConnectState.CONNECT_SUCCESS:
                    wiFiHandler.startScan();
                    break;
                case Global.WiFiState.WIFI_ENABLED:
                    wiFiHandler.startScan();
                case Global.WiFiState.WIFI_DISABLED:
                    switchWifi();
                    break;
                default:
                    Log.d("WIFIX","unknown message: " + msg.what);
                    break;
            }
        }
    };

    private ListPopupWindow listMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initReceiver();
        initView();
    }

    private void initData() {
        Courier.setMainHander(mainHandler);
        wiFiHandler = WiFiHandler.instance();
        wiFiHandler.startScan();

        currentAps = new ArrayList<>();
        configuredAP = new HashMap<>();
    }

    private void initReceiver() {
        wiFiStateReceiver = new WiFiStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wiFiStateReceiver, filter);

        scanResultReceiver = new ScanResultReceiver();
        IntentFilter scanFilter = new IntentFilter();
        scanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(scanResultReceiver, scanFilter);

        connectedStateReceiver = new ConnectedStateReceiver();
        IntentFilter connectFilter = new IntentFilter();
        connectFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        connectFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(connectedStateReceiver, connectFilter);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.ap_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wifiOffView = findViewById(R.id.wifi_off_view);
        wifiSwitch = (TextView) findViewById(R.id.wifi_switch);
        wifiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wiFiHandler.isWifEnabled()) {
                    wiFiHandler.closeWifi();
                } else {
                    wiFiHandler.openWifi();
                }
            }
        });
    }

    private void switchWifi() {
        if (wiFiHandler.isWifEnabled()) {
            recyclerView.setVisibility(View.VISIBLE);
            wifiOffView.setVisibility(View.INVISIBLE);
            wifiSwitch.setText("ON");
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            wifiOffView.setVisibility(View.VISIBLE);
            wifiSwitch.setText("OFF");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wiFiStateReceiver);
        unregisterReceiver(scanResultReceiver);
        unregisterReceiver(connectedStateReceiver);
        Courier.recycle();
    }

    private APAdapter.OnItemClickListener onItemClickListener = new APAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View view, int position) {
            if (listMenu == null) {
                listMenu = new ListPopupWindow(MainActivity.this);
                listMenu.setWidth(500);
                listMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                listMenu.setDropDownGravity(Gravity.BOTTOM);
                listMenu.setHorizontalOffset(10);
                listMenu.setModal(true);
            }

            final AccessPoint ap = currentAps.get(position);
            final ArrayList<String> data = new ArrayList<String>();

            if (wiFiHandler.isConnected(ap)) {
                data.add("Disconnect");
            } else {
                data.add("Connect");
            }

            if (ap.getNetworkId() > -1) {
                data.add("Remove");
            }

            ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(data,MainActivity.this);
            listMenu.setAdapter(adapter);
            listMenu.setAnchorView(view);
            listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (data.get(position).contains("Remove")) {
                        String toast = "";
                        toast = wiFiHandler.remove(ap.getNetworkId()) ? "Remove Success" : "Remove Failed";
                        showToast(toast);
                        wiFiHandler.startScan();
                    }
                    if (data.get(position).contains("Disconnect")) {
                        wiFiHandler.disconnect();
                        wiFiHandler.startScan();
                    }
                    if (data.get(position).contains("Connect")) {
                        new ConnectHandler(MainActivity.this,ap).start();
                    }
                    listMenu.dismiss();
                }
            });
            if (!listMenu.isShowing()) {
                listMenu.show();
            }
        }
    };

    private void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
