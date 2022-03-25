package com.paltouch.agentapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CustomerAccountBalance extends Activity implements AdapterView.OnItemSelectedListener {
    EditText edt_clientid;
    Button btn_generate_id;
    Spinner spn_clientaccounts;
    TextView txt_total_moneyin,txt_clientname_id,txt_total_moneyout,txt_balance;
    DatabaseHelper dbhelper;
    SQLiteDatabase db;

    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> list1 = new ArrayList<String>();
    ArrayList<String> totalmoneyin = new ArrayList<String>();
    ArrayList<String> totalmoneyout = new ArrayList<String>();
    ArrayList<String> other_refs = new ArrayList<String>();
    boolean data_back = false;
    String title[];
    private String client_name,member_no;
    String selected_account_name,selected_balance,selected_other_ref;
    private ArrayList<member_profile_model> member_profile_models_arraylist;
    private ListView listView_items;
    private ArrayList<String> ref_no = new ArrayList<>();
    private ArrayList<String> amount_in = new ArrayList<>();
    private ArrayList<String> amount_out = new ArrayList<>();
    private ArrayList<String> appr_date = new ArrayList<>();
    private ArrayList<String> bal = new ArrayList<>();
    private ArrayList<String> r_no = new ArrayList<>();
    private ArrayList<member_profile_model> member_profile_model_ArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_account_balance);

        dbhelper = new DatabaseHelper(getApplicationContext());
        db = dbhelper.getWritableDatabase();

        edt_clientid = (EditText) findViewById(R.id.edt_clientid);
        btn_generate_id = (Button) findViewById(R.id.btn_generate_id);
        btn_generate_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call async task to load client details
                list.clear();
                list1.clear();
                totalmoneyin.clear();
                totalmoneyout.clear();
                other_refs.clear();
                title = new String[0];
                boolean network_state = getnetwork_state();
                if(network_state){
                    if(!edt_clientid.getText().toString().isEmpty()) {
                        GetClientInfo getinfo = new GetClientInfo();
                        getinfo.execute();
                    }else{
                        Toast.makeText(CustomerAccountBalance.this,"Supply Client ID Number",Toast.LENGTH_LONG).show();
                        edt_clientid.requestFocus();
                    }
                }else {
                    //No Connection
                    Toast.makeText(CustomerAccountBalance.this,"Internet Disconnected",Toast.LENGTH_LONG).show();
                }
            }
        });
        txt_clientname_id = (TextView) findViewById(R.id.txt_clientname_id);
        txt_total_moneyin = (TextView) findViewById(R.id.txt_total_moneyin);
        txt_total_moneyout = (TextView) findViewById(R.id.txt_total_moneyout);
        txt_balance = (TextView) findViewById(R.id.txt_balance);
        listView_items = (ListView) findViewById(R.id.listView_items);
        spn_clientaccounts = (Spinner) findViewById(R.id.spn_clientaccounts);
        spn_clientaccounts.setOnItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.WARNING_TYPE).
                setTitleText("EXIT").setContentText("Do you want to Exit this page?").
                showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        CustomerAccountBalance.super.onBackPressed();
                        Intent main = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(main);
                        CustomerAccountBalance.this.finish();
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
    }
    public void Backbutton_click(View v){
        Intent i = new Intent(CustomerAccountBalance.this, MainActivity.class);
        startActivity(i);
        CustomerAccountBalance.this.finish();
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        //Call asynctask from here to laod account Balance
        try {
            txt_balance.setText("");
            txt_total_moneyin.setText("");
            txt_total_moneyout.setText("");
            selected_account_name = (String) title[pos];
            selected_balance = list1.get(pos);
            selected_other_ref = other_refs.get(pos);
            txt_balance.setText("Bal: "+selected_balance);
            txt_total_moneyin.setText("In: "+totalmoneyin.get(pos));
            txt_total_moneyout.setText("Out: "+totalmoneyout.get(pos));

            member_profile_model_ArrayList = populateList();

            member_profile_adapter foodAdapter = new member_profile_adapter(this,member_profile_model_ArrayList);
            listView_items.setAdapter(foodAdapter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }

    private ArrayList<member_profile_model> populateList(){

        ArrayList<member_profile_model> list = new ArrayList<>();

        String[] columns = {DatabaseHelper.OTHER_REF, DatabaseHelper.AMOUNT_IN,DatabaseHelper.AMOUNT_OUT,DatabaseHelper.APPROVAL_DATE, DatabaseHelper.BALANCE,DatabaseHelper.RECEIPT_NO};
        Cursor cursor = db.query(DatabaseHelper.TRANSACTION_TABLE_NAME, columns,
                    DatabaseHelper.OTHER_REF + "= '" + selected_other_ref +"'", null, null,
                    null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                member_profile_model profile_model = new member_profile_model();

                int index2 = cursor.getColumnIndex(DatabaseHelper.AMOUNT_IN);
                profile_model.setMoney_in(cursor.getString(index2));

                int index3 = cursor.getColumnIndex(DatabaseHelper.AMOUNT_OUT);
                profile_model.setMoney_out(cursor.getString(index3));

                int index4 = cursor.getColumnIndex(DatabaseHelper.APPROVAL_DATE);
                profile_model.setV_date(cursor.getString(index4));

                int index5 = cursor.getColumnIndex(DatabaseHelper.BALANCE);
                profile_model.setBalance(cursor.getString(index5));

                int index6 = cursor.getColumnIndex(DatabaseHelper.RECEIPT_NO);
                profile_model.setReceipt_number(cursor.getString(index6));

                list.add(profile_model);
            }
        } else {
            //Nothing
        }

        return list;
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

    private class GetClientInfo extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CustomerAccountBalance.this);
            pDialog.setMessage("Loading client details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/MemberProfile/MemberProfile/GetMemberDetail";
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

            try {
                object1.put("FilterValue", edt_clientid.getText().toString());
                object1.put("BiometricsValidated", "false");
                object1.put("TryValidateBiomExpected", "true");
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
                    JSONObject jresponse = JsonResultVeriy.getJSONObject("Result");
                    JSONObject memberdetails = jresponse.getJSONObject("MemberDetails");

                    client_name = memberdetails.getString("full_name");
                    member_no = memberdetails.getString("member_no");
                    db.delete(DatabaseHelper.TRANSACTION_TABLE_NAME,null,null);

                    JSONArray accounts_list = memberdetails.getJSONArray("member_accounts");
                    int tr1 = accounts_list.length();
                    String other_ref;
                    if(tr1 >0) {
                        for (int j = 0; j < accounts_list.length(); j++) {
                            JSONObject accounts = accounts_list.getJSONObject(j);

                            //JSONObject accounts = verifyresult3.getJSONObject("MemberAccount");
                            //JSONObject sacco_accounts = accounts.getJSONObject("sacco_accounts");
                            list.add(accounts.getString("AccountName"));
                            list1.add(accounts.getString("CurrentBalance"));
                            totalmoneyin.add(accounts.getString("TotalAmountIn"));
                            totalmoneyout.add(accounts.getString("TotalAmountOut"));
                            other_refs.add(accounts.getString("AccountRefNo"));
                            other_ref = "";
                            other_ref = accounts.getString("AccountRefNo");

                            JSONArray AccountTransactions = accounts.getJSONArray("AccountTransactions");
                            int tr2 = AccountTransactions.length();
                            if(tr2 >0) {
                                for (int k = 0; k < AccountTransactions.length(); k++) {
                                    JSONObject verifyresult4 = AccountTransactions.getJSONObject(k);
                                    ContentValues cv = new ContentValues();
                                    cv.put(DatabaseHelper.OTHER_REF, other_ref);
                                    cv.put(DatabaseHelper.AMOUNT_IN, verifyresult4.getString("AmountIn"));
                                    cv.put(DatabaseHelper.AMOUNT_OUT,verifyresult4.getString("AmountOut"));
                                    cv.put(DatabaseHelper.APPROVAL_DATE,verifyresult4.getString("ApprovalDate"));
                                    cv.put(DatabaseHelper.BALANCE,verifyresult4.getString("Balance"));
                                    cv.put(DatabaseHelper.RECEIPT_NO,verifyresult4.getString("ReceiptNumber"));
                                    db.insert(DatabaseHelper.TRANSACTION_TABLE_NAME, null, cv);
                                }
                            }
                            else{
                                //Nothing
                            }
                        }
                    }
                    else{
                        if(pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        Message msg1 = mhandler.obtainMessage();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("MSG_KEY", "No result from the server");
                        msg1.setData(bundle1);
                        msg1.what = 5;
                        mhandler.sendMessage(msg1);
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
                            bundle.putString("MSG_KEY", "No data returned from the server. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 2;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title = list.toArray(new String[list.size()]);
                            data_back = true;
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
                edt_clientid.setText(member_no);
                txt_clientname_id.setText(client_name);
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(CustomerAccountBalance.this, android.R.layout.simple_list_item_1, title);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_clientaccounts.setAdapter(AccountsApapdter); // causes nullpointerexception

            }else{
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
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
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string1).show();

                    break;

                case 2:
                    Bundle bundle2 = msg.getData();
                    String string2 = bundle2.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string2).show();

                    break;
                case 3:

                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(main);
                                    CustomerAccountBalance.this.finish();
                                }
                            }).
                            show();

                    break;
                case 5:
                    Bundle bundle5 = msg.getData();
                    String string5 = bundle5.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string5).show();

                    break;

                case 6:
                    Bundle bundle6 = msg.getData();
                    String string6 = bundle6.getString("MSG_KEY");
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.SUCCESS_TYPE).
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
                    new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string7).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(main);
                                    CustomerAccountBalance.this.finish();
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
                        new SweetAlertDialog(CustomerAccountBalance.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    }

                    break;
            }

        };
    };
}