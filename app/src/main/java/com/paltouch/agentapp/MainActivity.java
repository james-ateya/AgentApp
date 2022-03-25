package com.paltouch.agentapp;

import static com.paltouch.agentapp.GlobalVariables.apk_version;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    Button btn_deposit,btn_withdraw,btn_settings,btn_reports,btn_info,btn_customer;
    private boolean Active_session;
    private boolean deposit,withdraw,reports,info,customer;
    TextView txtfloat2;
    String agentfloat_amount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deposit = false;
        withdraw=false;
        reports=false;
        info=false;
        customer = false;

        txtfloat2 = (TextView) findViewById(R.id.txtfloat2);

        btn_deposit = (Button) findViewById(R.id.btn_deposit);
        btn_deposit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getnetwork_state()) {
                        //Active_session = true;
                        deposit = true;
                        withdraw=false;
                        reports=false;
                        info=false;
                        customer = false;
                        check_loggedin check_session = new check_loggedin();
                        check_session.execute();
                    }else {
                       new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    }
                }
        });

        btn_settings = (Button) findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent isettings = new Intent(MainActivity.this, EditPreferences.class);
                    startActivity(isettings);
                    MainActivity.this.finish();
                }
        });

        btn_customer = (Button) findViewById(R.id.btn_customer);
        btn_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getnetwork_state()) {
                    deposit = false;
                    withdraw=false;
                    reports=false;
                    info=false;
                    customer = true;
                    check_loggedin check_session = new check_loggedin();
                    check_session.execute();
                }else{
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                }
            }
        });

        btn_info = (Button) findViewById(R.id.btn_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getnetwork_state()) {
                    deposit = false;
                    withdraw=false;
                    reports=false;
                    info=true;
                    customer = false;
                    check_loggedin check_session = new check_loggedin();
                    check_session.execute();
                }else{
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                }
            }
        });

        btn_reports = (Button) findViewById(R.id.btn_reports);
        btn_reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call Daily report activity
                if(getnetwork_state()) {
                    deposit = false;
                    withdraw=false;
                    reports=true;
                    info=false;
                    customer = false;
                    check_loggedin check_session = new check_loggedin();
                    check_session.execute();
                }else{
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        new SweetAlertDialog(MainActivity.this,
                SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Logout")
                .setContentText("Do you want to logout?").
                showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(login);
                        MainActivity.this.finish();
                    }
                }).
                setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
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
        txtfloat2.setText(agentfloat_amount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {

            case R.id.action_updateapk:
                check_apk_updates updates = new check_apk_updates();
                updates.execute();
                break;
            case R.id.action_kiosk:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class check_apk_updates extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Checking System Updates");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/GlobalVariables/ApkUpdates/RebindGrid";
            JSONObject object1;
            object1 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
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
                    JSONArray jresponse = JsonResultVeriy.getJSONArray("Result");
                    int tr = jresponse.length();
                    if (tr >= 1) {
                        for (int i = 0; i < jresponse.length(); i++) {
                            JSONObject verifyresult2 = jresponse.getJSONObject(i);

                            apk_version = verifyresult2.getString("apk_version");
                            GlobalVariables.apk_name = verifyresult2.getString("apk_name");
                            GlobalVariables.server_path = verifyresult2.getString("server_path");
                        }
                    }

                }
                else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
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
                }
            } catch (IOException | JSONException e) {
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            check_apk_update_func();
        }
    }

    void check_apk_update_func(){
        //Run query to check if APK has updated
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Double server_apk = Double.parseDouble(apk_version);
        Double installed_apk = Double.parseDouble(info.versionName);

        if(server_apk > installed_apk){
            Intent a = new Intent(this, APK_Update.class);
            startActivity(a);
        }
    }


    ///SystemAccounts/Authentication/UserProfile/CheckLoggedIn
    private class check_loggedin extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Checking Login Credentials");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/SystemAccounts/Authentication/UserProfile/CheckLoggedIn";
            JSONObject object1;
            object1 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                pDialog.dismiss();
                Message msg = mhandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("MSG_KEY", "Malformed URL. Explanation: "+e.getMessage());
                msg.setData(bundle);
                msg.what=2;
                mhandler.sendMessage(msg);
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
                    //Still logged in
                    Message msg1 = mhandler.obtainMessage();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", "Session Active.");
                    msg1.setData(bundle1);
                    msg1.what=1;
                    mhandler.sendMessage(msg1);

                } else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    Message msg1 = mhandler.obtainMessage();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", "Authorization has been denied, please login.");
                    msg1.setData(bundle1);
                    msg1.what=2;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "System encountered a problem while checking login credentials. Please try again.");
                msg1.setData(bundle1);
                msg1.what=2;
                mhandler.sendMessage(msg1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            // dismiss the dialog once done
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
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

    Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(deposit == true) {
                        Intent customerdeposits = new Intent(MainActivity.this, CustomerDeposits.class);
                        startActivity(customerdeposits);
                        MainActivity.this.finish();
                    }else if(customer){
                        Intent iscustomer_reg = new Intent(MainActivity.this, Customer_Registration.class);
                        startActivity(iscustomer_reg);
                        MainActivity.this.finish();
                    } else if(reports){
                        Intent is_reports = new Intent(MainActivity.this, AgentDailyReport.class);
                        startActivity(is_reports);
                        MainActivity.this.finish();
                    }else{
                        Intent iscustomer_bal = new Intent(MainActivity.this, CustomerAccountBalance.class);
                        startActivity(iscustomer_bal);
                        MainActivity.this.finish();
                    }

                    break;

                case 2:
                    Bundle bundle7 = msg.getData();
                    String string7 = bundle7.getString("MSG_KEY");
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string7).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent Login = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(Login);
                                    MainActivity.this.finish();
                                    Active_session = false;
                                }
                            }).
                            show();

                    break;
            }

        };
    };
}