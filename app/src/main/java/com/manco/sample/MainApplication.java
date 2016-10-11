package com.manco.sample;

import android.app.Application;

import com.manco.sample.util.WiFiHandler;

/**
 * Created by Manco on 2016/10/9.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WiFiHandler.instance().init(this);
    }
}
