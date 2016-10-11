package com.manco.sample.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Vector;

/**
 * Created by Manco on 2016/10/9.
 */
public class Courier {
    private static Handler mainHander;
    private static Vector<Message> abandonMessage = new Vector<Message>();

    public static void setMainHander(Handler hander) {
        mainHander = hander;
    }

    public static void send(Message message) {
        Log.d("WIFIX","send message :" + message.what);
        if (null != mainHander) {
            mainHander.sendMessage(message);
        } else {
            abandonMessage.add(message);
        }
    }

    public static void send(int what) {
        Message message = Message.obtain();
        message.what = what;
        send(message);
    }

    public static void send(int what,Object obj) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        send(message);
    }

    public static void recycle() {
        mainHander = null;
        abandonMessage.clear();
    }
}
