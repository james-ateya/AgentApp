package com.paltouch.agentapp;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CustomerDeposits extends Activity implements CompoundButton.OnCheckedChangeListener {
    String currentprinter;
    BluetoothDevice printerdevice;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    Switch printerswitch;
    ProgressDialog pd;
    BluetoothDevice mmDevice;
    volatile boolean stopWorker;
    private OutputStream mmOutStream = null;
    private static OutputStream btoutputstream = null;
    InputStream mmInStream,mmInputStream;
    private BluetoothSocket mmSocket = null;
    BluetoothSocket mmSocket1;
    Timer timer1;
    TimerTask timerTask1;
    byte[] readBuffer;
    int readBufferPosition;
    Thread workerThread;
    String msgprnt;
    byte FONT_TYPE;
    //SharedPreferences sharedPref;

    Boolean useprinter;
    Button btnprintreceipt,btnsavedetails,btn_generate,btncompletetransaction;
    EditText edt_amount,edt_searchclient,edadditems;
    TextView txt_clientname;
    Spinner spn_accounts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_deposits);

        SharedPreferences spref6 = PreferenceManager
                .getDefaultSharedPreferences(this);
        useprinter = spref6.getBoolean("useprinter", true);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            new AlertDialog.Builder(CustomerDeposits.this).setTitle("BLUETOOTH UNAVAILABLE").setMessage("Sorry,Your device does not support bluetoth").show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            //Do Nothing, already enabled.
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CustomerDeposits.this);
        currentprinter = sharedPref.getString("printermac", "00:02:5B:B3:8A:BF");
        printerdevice = mBluetoothAdapter.getRemoteDevice(currentprinter);

        edt_amount = (EditText) findViewById(R.id.edt_amount);
        edt_searchclient = (EditText) findViewById(R.id.edt_searchclient);
        edadditems = (EditText) findViewById(R.id.edadditems);
        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do Search Event
                boolean network_state = getnetwork_state();
                if(network_state){
                    //Network Connected
                    Toast.makeText(CustomerDeposits.this,"Internet Connected",Toast.LENGTH_LONG).show();
                }else {
                    //No Connection
                    Toast.makeText(CustomerDeposits.this,"Internet Disconnected",Toast.LENGTH_LONG).show();
                }
            }
        });
        txt_clientname = (TextView) findViewById(R.id.txt_clientname);
        spn_accounts = (Spinner) findViewById(R.id.spn_accounts);


        printerswitch = (Switch) findViewById(R.id.printerswitch);
        applyStyle(printerswitch.getTextOn(), printerswitch.getTextOff(),
                "printer");
        printerswitch.setOnCheckedChangeListener(CustomerDeposits.this);

        btnsavedetails = (Button) findViewById(R.id.btnsavedetails);
        btnsavedetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload details to the server

            }
        });

        btncompletetransaction = (Button) findViewById(R.id.btncompletetransaction);
        btncompletetransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save batch transactions
                List<String> Account_name = new ArrayList<String>();
                Account_name.add("RENT");
                Account_name.add("NHIF");
                Account_name.add("SCHOOL FEES");

                List<String> Amount = new ArrayList<String>();
                Amount.add("1");
                Amount.add("2");
                Amount.add("3");

                StringBuffer sbitems = new StringBuffer();
                for(int i = 0;i<Account_name.size();i++ ) {
                    sbitems.append(Account_name.get(i) + ": " + Amount.get(i) + "\n");
                }
                edadditems.setText("");
                edadditems.setText("");
                edadditems.setText(sbitems.toString());
            }
        });

        btnprintreceipt = (Button) findViewById(R.id.btnprintreceipt);
        btnprintreceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printerswitch.isChecked()){
                    //Print receipt to the client
                    printdetails();
                }else {
                    new SweetAlertDialog(CustomerDeposits.this,
                            SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("ERROR...")
                            .setContentText("Please Connect to the printer first.").show();
                    return;
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent i = new Intent(CustomerDeposits.this, MainActivity.class);
        startActivity(i);
        CustomerDeposits.this.finish();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(useprinter){
            printerswitch.setVisibility(View.VISIBLE);
            btnprintreceipt.setVisibility(View.VISIBLE);
        }else {
            printerswitch.setVisibility(View.GONE);
            btnprintreceipt.setVisibility(View.GONE);
        }
    }

    public void togglestatehandler(View v) {
        Switch switchbtn = (Switch) v;
        boolean isChecked = switchbtn.isChecked();
    }

    public void Backbutton_click(View v){
        Intent i = new Intent(CustomerDeposits.this, MainActivity.class);
        startActivity(i);
        CustomerDeposits.this.finish();
    }

    public void applyStyle(CharSequence switchTxtOn, CharSequence switchTxtOff,
                           String type) {
        if (type.equals("printer")) {
            Spannable styleText = new SpannableString(switchTxtOn);
            StyleSpan style = new StyleSpan(Typeface.BOLD);
            styleText.setSpan(style, 0, switchTxtOn.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            styleText.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                    switchTxtOn.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            printerswitch.setTextOn(styleText);
            styleText = new SpannableString(switchTxtOff);
            styleText.setSpan(style, 0, switchTxtOff.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            printerswitch.setTextOff(styleText);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.printerswitch:
                if (isChecked) {
                    pd = new ProgressDialog(CustomerDeposits.this);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setMessage("Searching for printer...");
                    pd.setIndeterminate(true);
                    pd.setCancelable(false);
                    pd.show();
                    try {

                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        String PrinterMacAddress = currentprinter;
                        // search target device in list of paired devices
                        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                        Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
                        while (iterator.hasNext()) {
                            BluetoothDevice device = iterator.next();
                            if (device.getAddress().equals(PrinterMacAddress)) {//WC Scale//"BTM0304C1H"
                                mmDevice = device;
                                //openBT();
                                PrinterThread pt = new PrinterThread(device);
                                pt.start();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        if(pd.isShowing()){
                            pd.dismiss();
                        }
                        try {
                            closeBT();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                } else {
                    if(pd.isShowing()){
                        pd.dismiss();
                    }
                    try {
                        closeBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                return;
        }
    }

    // Connect to the server device i.e. scale
    private class PrinterThread extends Thread {

        public PrinterThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            mmSocket1 = null;
            mmInputStream = null;
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.e(TAG, "mmDevice" + mmDevice);
            try {
                // MY_UUID is the app's UUID string, also used by the server// code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
            }
            mmSocket1 = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            // mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket1.connect();
                btoutputstream = mmSocket1.getOutputStream();
                Log.e(TAG, "Socket Connect");
                startTimer1();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {

                    mmSocket1 = (BluetoothSocket) mmDevice
                            .getClass()
                            .getMethod("createRfcommSocket",
                                    new Class[] { int.class }).invoke(mmDevice, 2);
                    mmSocket1.connect();
                    btoutputstream = mmSocket1.getOutputStream();
                    Log.e(TAG, "Socket Connect");
                    startTimer1();
                } catch (Exception e2) {
                    Log.v("No Bluetooth conn!", e2.getMessage());
                    CustomerDeposits.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(CustomerDeposits.this, "Connection to the printer Lost, Please Turn on Your printer Or Restart it.", Toast.LENGTH_LONG).show();
                            Log.v("Connection Lost....", "Connection to the printer Lost, Please Turn on Your Scale.");
                        }
                    });
                    pd.dismiss();
                    try {
                        closeBT();
                    } catch (IOException e3) {
                        Log.v("cant close() socket", e3.getMessage());
                    }
                }
            }
            // Do work to manage the connection (in a separate thread)
            // manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket1.close();
            } catch (IOException e) {
            }
        }
    }

    public void startTimer1() {
        //set a new Timer
        timer1 = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask1();
        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
        timer1.schedule(timerTask1, 1000, 100); //
    }
    public void stoptimertask1() {
        //stop the timer, if it's not already null
        if (timer1 != null) {
            timer1.cancel();
            timer1 = null;
        }
    }
    public void initializeTimerTask1() {
        timerTask1 = new TimerTask() {
            public void run() {
                if(pd.isShowing()){
                    pd.dismiss();
                }
                //beginListenForData();
                stoptimertask1();
            }
        };
    }
    /*
     * after opening a connection to bluetooth printer device,
     * we have to listen and check if a data were sent to be printed.
     */
    void beginListenForData() {
        try {
            // this is the ASCII code for a newline character
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            if(pd.isShowing()){
                pd.dismiss();
            }
            try {
                closeBT();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void closeBT() throws IOException {
        try {
            stopWorker = true;
            if(mmOutStream != null){
                try {mmOutStream.close();} catch (Exception e) {}
                mmOutStream = null;
            }

            if(btoutputstream != null){
                try {btoutputstream.close();} catch (Exception e) {}
                btoutputstream = null;
            }

            if(mmInStream != null){
                try {mmInStream.close();} catch (Exception e) {}
                mmInStream = null;
            }
            if(mmSocket != null){
                try {mmSocket.close();} catch (Exception e) {}
                mmSocket = null;
            }
            safeClose(mmSocket);

            if(mmSocket1 != null){
                try {mmSocket1.close();} catch (Exception e) {}
                mmSocket1 = null;
            }
            safeClose(mmSocket1);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void safeClose(Closeable c) {
        if (c == null)
            return;
        for (int retries = 3; retries > 0; retries--)
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
    }

    void printdetails(){

        msgprnt = "Receipt No " + getreceiptno() + "\n" + getday()
                + " " + gettime() + "\n" + "\n"
                + "Name  :"
                + txt_clientname.getText().toString()
                + "\n" + "Account: "
                + " "
                + "\n" + "Acc No  :"
                + edt_searchclient.getText().toString()
                + "\n"
                + "Amount KS:"
                + edt_amount.getText().toString()
                + "\n" + "You were served by DEMO"
                + "\n\n\n";

        byte[] printformat = {0x1B, 0x21, FONT_TYPE};
        byte[] format_underline = {27, 33, 0};
        byte[] format_title = {27, 33, 0};
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CustomerDeposits.this);
        String company = sharedPref.getString("printcompany", "DEMO COMPANY");
        String box = sharedPref.getString("printbox", "P.O BOX");
        String tel = sharedPref.getString("printtel", "07........");
        String header1 = company + "\n" + box + " \n" + tel + "\n";

        try {
                //btoutputstream = (bluetoothSocket2.getUnderlyingSocket()).getOutputStream();
                btoutputstream.write(format_title);
                btoutputstream.write(header1.getBytes());
                btoutputstream.write(printformat);
                btoutputstream.write(msgprnt.getBytes());
                btoutputstream.write(0x0D);
                btoutputstream.write(0x0D);
                btoutputstream.write(0x0D);
                btoutputstream.flush();
        } catch (IOException e) {
                e.printStackTrace();
        }

        //Reset variables
        edt_searchclient.setText("");
        txt_clientname.setText("");
        edt_amount.setText("");
    }

    String getreceiptno() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
        Random rnd = new Random(System.currentTimeMillis());
        int num = rnd.nextInt(100);
        String receipt = sdf.format(now) + String.valueOf(num);
        return receipt;
    }

    String getday() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy");
        return sdf.format(now);
    }

    String gettime() {
        return DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
    }

    Boolean getnetwork_state(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                return true;
            }else{
                return false;
            }
        } else {
            // not connected to the internet
            return false;
        }
    }

    //Async task
    private class GetClientInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {

        }
    }

    private class SaveCollections extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {

        }
    }

}