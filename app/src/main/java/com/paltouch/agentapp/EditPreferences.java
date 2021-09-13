package com.paltouch.agentapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class EditPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);


        Preference printerPreference = getPreferenceScreen().findPreference("printermac");
        printerPreference.setOnPreferenceChangeListener(printerCheckListener);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent i = new Intent(EditPreferences.this, MainActivity.class);
        startActivity(i);
        EditPreferences.this.finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case 1:
                    String mac1 = data.getData().toString();
                    SharedPreferences.Editor editor1 = PreferenceManager
                            .getDefaultSharedPreferences(getBaseContext()).edit();
                    editor1.putString("printermac", mac1);
                    editor1.apply();

                    // update the display
                    Preference p1 = findPreference("printermac");
                    p1.setSummary(mac1);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    Preference.OnPreferenceChangeListener printerCheckListener = new OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Check that the string is an integer
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Boolean check = mBluetoothAdapter.checkBluetoothAddress(String.valueOf(newValue));
            if (check) {
                return true;
            } else {
                Toast.makeText(getApplicationContext(),
                        "Incorrect mac address value", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
    };



}
