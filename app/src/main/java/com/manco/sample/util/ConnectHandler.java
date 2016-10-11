package com.manco.sample.util;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.manco.sample.R;
import com.manco.sample.entity.AccessPoint;

/**
 * Created by Manco on 2016/10/10.
 */
public class ConnectHandler {
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private AccessPoint accessPoint;

    private View contentView;
    private EditText password;
    private TextView passwordTip;

    public ConnectHandler(Context context,AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
        if (accessPoint.getNetworkId() < 0) {
            this.contentView = LayoutInflater.from(context).inflate(R.layout.layout_connect_dialog, null);
            password = (EditText) contentView.findViewById(R.id.password);
            setPasswordListener();
            passwordTip = (TextView) contentView.findViewById(R.id.password_tip);

            builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);
            builder.setTitle(accessPoint.getSsid());
            builder.setView(contentView);
            builder.setPositiveButton("Connect", positiveListener);
            builder.setNegativeButton("Cancel", null);
        }
    }

    public void start() {
        if (builder != null) {
            dialog = builder.create();
            dialog.show();
        } else {
            WiFiHandler.instance().connect(accessPoint.getNetworkId());
        }
    }

    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            accessPoint.setPassword(password.getText().toString());
            WiFiHandler.instance().connect(accessPoint);
        }
    };

    private void setPasswordListener() {
        final int lengthLimit = accessPoint.getEncryptionType().contains("WPA") ? 8 : 5;
        password.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                String acceptPsds = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()=-+_;',./?><|[]{}";
                char[] chars = acceptPsds.toCharArray();
                return chars;
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        });
        password.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == s || s.toString().length() < lengthLimit) {
                    if (dialog != null) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF999999);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (null != s && s.toString().length() >= lengthLimit) {
                    if (dialog != null) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF0078ff);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        });
    }
}
