package com.paltouch.agentapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CustomerDeposits extends Activity {
    String agentfloat_amount;
    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> list1 = new ArrayList<String>();
    boolean data_back = false;
    String title[];
    private String client_name,phone_no,member_no;
    private String account_name,account_no;
    String selected_account_name,selected_account_no;
    JSONArray allocations;
    JSONArray datatosave;
    JSONObject collected_data;
    JSONObject collected_data_tosave;
    ArrayList<String> total_amount = new ArrayList<String>();
    StringBuffer sbitems = new StringBuffer();
    Double totalC = 0.0;
    JSONArray s = new JSONArray();

    Button btnsavedetails,btn_generate,btncompletetransaction;
    EditText edt_amount,edadditems;
    EditText edt_searchclient;
    TextView txt_clientname,txtfloat;
    Spinner spn_accounts;
    String response_message;

    DatabaseHelper dbhelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_deposits);

        dbhelper = new DatabaseHelper(getApplicationContext());
        db = dbhelper.getWritableDatabase();

        collected_data = new JSONObject();
        collected_data_tosave = new JSONObject();
        allocations = new JSONArray();
        datatosave = new JSONArray();

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
        txtfloat = (TextView) findViewById(R.id.txtfloat);
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
                        if(selected_account_name.contains("Collection Account")){
                            collected_data.put("IsNHIF", "True");
                        }
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

                    //to save
                    try {
                        collected_data_tosave.put("account_no",selected_account_no);
                        collected_data_tosave.put("amount",edt_amount.getText().toString());
                        collected_data_tosave.put("account_name",selected_account_name);
                        collected_data_tosave.put("full_name",txt_clientname.getText().toString());
                        collected_data_tosave.put("nat_id",edt_searchclient.getText().toString());
                        collected_data_tosave.put("collection_date",getday());
                        collected_data_tosave.put("collected_time",gettime());
                        datatosave.put(collected_data_tosave);
                        collected_data_tosave = new JSONObject();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


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

                    edt_amount.setText("");
                    selected_account_no = null;
                    selected_account_name = null;
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
        super.onResume();
        SharedPreferences spref6 = PreferenceManager
                .getDefaultSharedPreferences(this);
        agentfloat_amount = spref6.getString("AgentFloat", "0.00");
        txtfloat.setText(agentfloat_amount);
    }


    public void Backbutton_click(View v){
        Intent i = new Intent(CustomerDeposits.this, MainActivity.class);
        startActivity(i);
        CustomerDeposits.this.finish();
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

                            account_name = verifyresult2.getString("true_account_name");
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

                }
                else {
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
                    response_message = JsonResultVeriy.getString("Message");
                    JSONObject result_object = JsonResultVeriy.getJSONObject("Result");
                    String agent_float;
                    agent_float = result_object.getString("balance");
                    SharedPreferences.Editor editorper = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                    editorper.putString("AgentFloat", agent_float);
                    editorper.apply();
                    System.out.println("Message >>>>>>" + response_message);

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
                //Save collection to sql-lite
                try {
                int tr1 = datatosave.length();
                if(tr1 >0) {
                    for (int j = 0; j < datatosave.length(); j++) {
                        JSONObject accounts = datatosave.getJSONObject(j);
                        ContentValues cv = new ContentValues();
                        cv.put(DatabaseHelper.FULL_NAME, accounts.getString("full_name"));
                        cv.put(DatabaseHelper.NAT_ID, accounts.getString("nat_id"));
                        cv.put(DatabaseHelper.ACCOUNT_NUMBER,accounts.getString("account_no"));
                        cv.put(DatabaseHelper.ACCOUNT_NAME,accounts.getString("account_name"));
                        cv.put(DatabaseHelper.DEPOSITED_AMOUNT,accounts.getString("amount"));
                        cv.put(DatabaseHelper.DATE,accounts.getString("collection_date"));
                        cv.put(DatabaseHelper.TIME,accounts.getString("collected_time"));
                        db.insert(DatabaseHelper.AGENT_REPORT_TABLE_NAME, null, cv);
                    }
                }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                datatosave = new JSONArray();

                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", response_message);
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
                    new SweetAlertDialog(CustomerDeposits.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string5)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                            startActivity(main);
                                            CustomerDeposits.this.finish();
                                        }
                                    }).show();

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

    String getday() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy");
        return sdf.format(now);
    }

    String gettime() {
        return DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
    }

}