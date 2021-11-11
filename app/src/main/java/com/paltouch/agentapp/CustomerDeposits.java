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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> list1 = new ArrayList<String>();
    boolean data_back = false;
    String title[];
    private String client_name,phone_no,member_no;
    private String account_name,account_no;
    String selected_account_name,selected_account_no;
    JSONArray allocations;
    JSONObject collected_data;
    ArrayList<String> total_amount = new ArrayList<String>();
    StringBuffer sbitems = new StringBuffer();
    Double totalC = 0.0;
    JSONArray s = new JSONArray();

    Boolean useprinter;
    Button btnprintreceipt,btnsavedetails,btn_generate,btncompletetransaction;
    EditText edt_amount,edadditems;
    EditText edt_searchclient;
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

        collected_data = new JSONObject();
        allocations = new JSONArray();

        edt_amount = (EditText) findViewById(R.id.edt_amount);
        edadditems = (EditText) findViewById(R.id.edadditems);
        edt_searchclient = (EditText) findViewById(R.id.edt_searchclient);

        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do Search Event
                list.clear();
                list1.clear();
                title = new String[0];
                boolean network_state = getnetwork_state();
                if(network_state){
                    if(!edt_searchclient.getText().toString().isEmpty()) {
                        GetClientInfo getinfo = new GetClientInfo();
                        getinfo.execute();
                    }else{
                        Toast.makeText(CustomerDeposits.this,"Supply Client ID Number",Toast.LENGTH_LONG).show();
                        edt_searchclient.requestFocus();
                    }
                }else {
                    //No Connection
                    Toast.makeText(CustomerDeposits.this,"Internet Disconnected",Toast.LENGTH_LONG).show();
                }
            }
        });
        txt_clientname = (TextView) findViewById(R.id.txt_clientname);
        spn_accounts = (Spinner) findViewById(R.id.spn_accounts);
        spn_accounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selected_account_name = (String) title[position];
                    selected_account_no = list1.get(position);
                    //selected_account_name = spn_accounts.getSelectedItem().toString();
                }catch (Exception e){
                    Log.v("Error: ",e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        printerswitch = (Switch) findViewById(R.id.printerswitch);
        applyStyle(printerswitch.getTextOn(), printerswitch.getTextOff(),
                "printer");
        printerswitch.setOnCheckedChangeListener(CustomerDeposits.this);

        btncompletetransaction = (Button) findViewById(R.id.btncompletetransaction);
        btncompletetransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save batch
                if(edt_searchclient.getText().toString().isEmpty()){
                    Toast.makeText(CustomerDeposits.this,"Supply Client ID Number",Toast.LENGTH_LONG).show();
                    edt_searchclient.requestFocus();
                } else if(spn_accounts == null || spn_accounts.getSelectedItem() == null){
                    Toast.makeText(CustomerDeposits.this,"Select Customer Account.",Toast.LENGTH_LONG).show();
                }
                else if (edt_amount.getText().toString().isEmpty() || Integer.parseInt(edt_amount.getText().toString()) <= 0) {
                    Toast.makeText(CustomerDeposits.this,"Supply Amount.",Toast.LENGTH_LONG).show();
                    edt_amount.requestFocus();
                }
                else {
                    try {
                        collected_data.put("account_no", selected_account_no);
                        collected_data.put("amount", edt_amount.getText().toString());
                        allocations.put(collected_data);
                        collected_data = new JSONObject();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String total_amount1 = edt_amount.getText().toString();
                    total_amount.add(total_amount1);

                    List<String> Account_name = new ArrayList<String>();
                    Account_name.add(selected_account_name);

                    List<String> Amount = new ArrayList<String>();
                    Amount.add(edt_amount.getText().toString());

                    StringBuffer sbitems2 = new StringBuffer();

                    for (int i = 0; i < Account_name.size(); i++) {
                        sbitems.append(Account_name.get(i) + ": " + Amount.get(i) + "\n");
                    }
                    totalC = 0.0;
                    for (int i = 0; i < total_amount.size(); i++) {
                        totalC = totalC + Double.valueOf(total_amount.get(i));
                    }
                    sbitems2.append("Total: " + totalC);
                    edadditems.setText("");
                    edadditems.setText(sbitems.toString() + "\n" + sbitems2.toString());

                }
            }
        });

        btnsavedetails = (Button) findViewById(R.id.btnsavedetails);
        btnsavedetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getnetwork_state()){
                    SaveCollections savedata =  new SaveCollections();
                    savedata.execute();
                }else {
                    new SweetAlertDialog(CustomerDeposits.this,
                            SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("ERROR...")
                            .setContentText("Please Connect to the internet first.").show();
                    return;
                }
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
        new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.WARNING_TYPE).
                setTitleText("EXIT").setContentText("Do you want to Exit this page?").
                showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        CustomerDeposits.super.onBackPressed();
                        Intent main = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(main);
                        CustomerDeposits.this.finish();
                    }
                }).
                setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        return;
                    }
                }).
                show();
    }

    @Override
    public void onResume(){
        if(useprinter){
            super.onResume();
            printerswitch.setVisibility(View.VISIBLE);
            btnprintreceipt.setVisibility(View.VISIBLE);
        }else {
            super.onResume();
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
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CustomerDeposits.this);
            pDialog.setMessage("Loading client details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/Client/GetDetails/"+edt_searchclient.getText().toString();
            JSONObject object1,object2,object3;
            JSONArray s;
            s = new JSONArray();
            object1 = new JSONObject();
            object2 = new JSONObject();
            object3 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            /*
                object1.put("take","10000");
                object1.put("skip","0");
                object2.put("logic","AND");
                object3.put("value",edt_searchclient.getText().toString());
                object3.put("field","member_no");
                object3.put("operator","equals");
                s.put(object3);
                object2.put("filters",s);
                object1.put("filter",object2);

                 */


            try {
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(object1.toString());
                writer.flush();
                writer.close();
                out.close();

                conn.connect();

                //display what returns the POST request
                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println("ATEYA" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    JSONObject jresponse = JsonResultVeriy.getJSONObject("Result");

                    client_name = jresponse.getString("client_name");
                    phone_no = jresponse.getString("phone_no");
                    member_no = jresponse.getString("client_no");

                    JSONArray accountlists = jresponse.getJSONArray("account_list");
                    int tr = accountlists.length();
                    if (tr >= 1) {
                        for (int i = 0; i < accountlists.length(); i++) {

                            //result = verifyresult3.getJSONObject(i).toString(i);
                            JSONObject verifyresult2 = accountlists.getJSONObject(i);

                            account_name = verifyresult2.getString("account_name");
                            account_no = verifyresult2.getString("account_no");

                            data_back = true;
                            list.add(account_name);
                            list1.add(account_no);
                        }

                        //Read the list
                        if (list.size() <= 0) {
                            //Throw Error. No Record Found
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            data_back = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while fetching stations. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 2;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title = list.toArray(new String[list.size()]);
                            data_back = true;
                        }
                    }else{
                        //Throw Error. No Record Found
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        data_back = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while fetching user stations.");
                        msg.setData(bundle);
                        msg.what = 2;
                        mhandler.sendMessage(msg);
                    }

                } else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    data_back = false;
                    System.out.println("*****> " + conn.getErrorStream().toString());
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        sb.append(line1 + "\n");
                    }
                    br1.close();
                    System.out.println("ATEYA" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResulterror = new JSONObject(JsonResult);
                    JSONObject error_object = JsonResulterror.getJSONObject("Result");
                    String response_errormessage = error_object.getString("Message");
                    System.out.println("Message >>>>>>" + response_errormessage);
                    Message msg1 = mhandler.obtainMessage();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", response_errormessage);
                    msg1.setData(bundle1);
                    msg1.what = 5;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException | JSONException e) {
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                e.printStackTrace();
                e.printStackTrace();
                data_back = false;
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: " + e.getMessage());
                msg1.setData(bundle1);
                msg1.what = 5;
                mhandler.sendMessage(msg1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            if (data_back) {
                //Load Data to Interface
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                edt_searchclient.setText(member_no);
                txt_clientname.setText(client_name);
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(CustomerDeposits.this, android.R.layout.simple_list_item_1, title);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_accounts.setAdapter(AccountsApapdter); // causes nullpointerexception

            }else{
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class SaveCollections extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CustomerDeposits.this);
            pDialog.setMessage("Saving Client Deposits...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/Allocation/Add";
            JSONObject object1;
            object1 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                object1.put("client_no",member_no);
                object1.put("amount",totalC);
                object1.put("allocations",allocations);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(object1.toString());
                writer.flush();
                writer.close();
                out.close();

                conn.connect();

                //display what returns the POST request
                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println("ATEYA" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    data_back = true;

                }
                else {
                    data_back = false;
                    System.out.println("*****> " + conn.getErrorStream().toString());
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        sb.append(line1 + "\n");
                    }
                    br1.close();
                    System.out.println("ATEYA" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResulterror = new JSONObject(JsonResult);
                    //JSONObject error_object = JsonResulterror.getJSONObject("Result");
                    String response_errormessage = JsonResulterror.getString("Message");
                    System.out.println("Message >>>>>>" + response_errormessage);
                    Message msg1 = mhandler.obtainMessage();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", response_errormessage);
                    msg1.setData(bundle1);
                    msg1.what = 5;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                e.printStackTrace();
                data_back = false;
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: " + e.getMessage());
                msg1.setData(bundle1);
                msg1.what = 5;
                mhandler.sendMessage(msg1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            collected_data = new JSONObject();
            allocations = new JSONArray();
            total_amount.clear();
            totalC = 0.0;
            edt_amount.setText("");
            edt_searchclient.setText("");
            txt_clientname.setText("");

            if(data_back){
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Transaction Completed Successfully.");
                msg1.setData(bundle1);
                msg1.what = 4;
                mhandler.sendMessage(msg1);
            }
            else{
                return;
            }
        }
    }

    Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Bundle bundle1 = msg.getData();
                    String string1 = bundle1.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string1).show();

                    break;

                case 2:
                    Bundle bundle2 = msg.getData();
                    String string2 = bundle2.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string2).show();

                    break;
                case 3:

                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(main);
                                    CustomerDeposits.this.finish();
                                }
                            }).
                            show();

                    break;
                case 5:
                    Bundle bundle5 = msg.getData();
                    String string5 = bundle5.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string5).show();

                    break;

                case 6:
                    Bundle bundle6 = msg.getData();
                    String string6 = bundle6.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string6).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            }).
                            show();

                    break;

                case 7:
                    Bundle bundle7 = msg.getData();
                    String string7 = bundle7.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string7).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(main);
                                    CustomerDeposits.this.finish();
                                }
                            }).
                            show();

                    break;
                case 8:
                    if (getnetwork_state())
                    {
                        //Do Something
                    }else{
                        //No Internet Connection
                        new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    }

                    break;
            }

        };
    };

}