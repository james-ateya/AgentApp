package com.paltouch.agentapp;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class ScanPrinter extends Preference {
    private static BluetoothSocket btsocket;
    public ScanPrinter(Context context, AttributeSet attrs) {
        super(context, attrs);
        // define what happens when we click the preference
        setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                // TODO Auto-generated method stub
                connect();
                return false;
            }

        });
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String current = sharedPref.getString("printermac", "");
        this.setSummary(current);
    }

    public void connect() {
        if (btsocket == null) {
            Intent BTIntent = new Intent(getContext(), PrintScanList.class);
            ((Activity) getContext()).startActivityForResult(BTIntent, 1);
        }
    }

}
