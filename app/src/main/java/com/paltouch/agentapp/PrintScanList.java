package com.paltouch.agentapp;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

//import com.google.android.gms.common.api.GoogleApiClient;

public class PrintScanList extends ListActivity {
    static public final int REQUEST_CONNECT_BT = 0x2300;
    static private final int REQUEST_ENABLE_BT = 0x1000;
    static private ArrayAdapter<String> mArrayAdapter = null;
    static private ArrayAdapter<BluetoothDevice> btDevices = null;
    private static final UUID SPP_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    // UUID.fromString(�00001101-0000-1000-8000-00805F9B34FB�);
    static private BluetoothSocket mbtSocket = null;
    BluetoothDevice device;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String mac = "";
    Boolean registered = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Bluetooth Devices");
        mArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        btDevices = new ArrayAdapter<BluetoothDevice>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1);
        setListAdapter(mArrayAdapter);
        mBluetoothAdapter.startDiscovery();
        Toast.makeText(getApplicationContext(), "Getting all available Bluetooth Devices", Toast.LENGTH_SHORT).show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        registered = true;

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static BluetoothSocket getSocket() {
        return mbtSocket;
    }

    private void flushData() {
        try {
            if (mbtSocket != null) {
                mbtSocket.close();
                mbtSocket = null;
            }

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
            }

            if (btDevices != null) {
                btDevices.clear();
                btDevices = null;
            }

            if (mArrayAdapter != null) {
                mArrayAdapter.clear();
                mArrayAdapter.notifyDataSetChanged();
                mArrayAdapter.notifyDataSetInvalidated();
                mArrayAdapter = null;
            }

            finalize();

        } catch (Exception ex) {
        } catch (Throwable e) {
        }

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent intent) {
        super.onActivityResult(reqCode, resultCode, intent);

        switch (reqCode) {
            case REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {
                    Set<BluetoothDevice> btDeviceList = mBluetoothAdapter
                            .getBondedDevices();
                    try {
                        if (btDeviceList.size() > 0) {

                            for (BluetoothDevice device : btDeviceList) {
                                if (btDeviceList.contains(device) == false) {

                                    btDevices.add(device);

                                    mArrayAdapter.add(device.getName() + "\n"
                                            + device.getAddress());
                                    mArrayAdapter.notifyDataSetInvalidated();
                                }
                            }
                        }
                    } catch (Exception ex) {
                    }
                }

                break;
        }

        mBluetoothAdapter.startDiscovery();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {

                    // if (btDevices.getPosition(device) < 0) {
                    btDevices.add(device);
                    mArrayAdapter.add(device.getName() + "\n"
                            + device.getAddress() + "\n");
                    mArrayAdapter.notifyDataSetChanged();
                    btDevices.notifyDataSetChanged();
                    // }
                } catch (Exception ex) {
                    // ex.fillInStackTrace();
                    Log.v("%%%","Exception"+ex.getMessage());
                }
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mBluetoothAdapter == null) {
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mac = btDevices.getItem(position).getAddress();
        Toast.makeText(getApplicationContext(), "Connecting to " + btDevices.getItem(position).getName() + "," + btDevices.getItem(position).getAddress(),
                Toast.LENGTH_SHORT).show();
        final Thread connectThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                    Log.d("uuid", uuid.toString());
                    mbtSocket = btDevices.getItem(position).createRfcommSocketToServiceRecord(uuid);
                    Thread.sleep(500);
                    mbtSocket.connect();
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                            Intent result = new Intent();
                            result.setData(Uri.parse(mac));
                            setResult(Activity.RESULT_OK, result);
                            finish();
                        }
                    });
                    try {
                        mbtSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mbtSocket = null;
                } catch (IOException ex) {
                    //Retry
                    try {

                        mbtSocket = (BluetoothSocket) btDevices.getItem(position)
                                .getClass()
                                .getMethod("createRfcommSocket",
                                        new Class[] { int.class })
                                .invoke(btDevices.getItem(position), 1);
                        mbtSocket.connect();

                    } catch (Exception e2) {
                        runOnUiThread(socketErrorRunnable);
                        try {
                            mbtSocket.close();
                        } catch (IOException e) {
                            // e.printStackTrace();
                        }
                        mbtSocket = null;
                        return;
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (registered == true) {
                                unregisterReceiver(mReceiver);
                                registered = false;
                            }

                            finish();
                        }
                    });
                }
            }
        });

        connectThread.start();
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
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        unregisterReceiver(mReceiver);
        Intent i = new Intent(PrintScanList.this, EditPreferences.class);
        startActivity(i);
        PrintScanList.this.finish();
    }

    private Runnable socketErrorRunnable = new Runnable() {

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(),
                    "Cannot establish connection", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.startDiscovery();

        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("PrintScanList Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();
        //AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //AppIndex.AppIndexApi.end(client, getIndexApiAction());
        //client.disconnect();
    }
}
