package com.paltouch.agentapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends Activity {
    Button btn_deposit,btn_withdraw,btn_settings,btn_reports,btn_info,btn_customer;
    private boolean Active_session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_deposit = (Button) findViewById(R.id.btn_deposit);
        btn_deposit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if(getnetwork_state()) {
                        Active_session = true;
                        //check_loggedin check_session = new check_loggedin();
                        //check_session.execute();
                        if (Active_session) {
                            Intent customerdeposits = new Intent(MainActivity.this, CustomerDeposits.class);
                            startActivity(customerdeposits);
                            MainActivity.this.finish();
                        }
                    //}else {
                    //    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    //}
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
                //if(getnetwork_state()) {
                //    Active_session = false;
                //    check_loggedin check_session = new check_loggedin();
                //    check_session.execute();
                //    if (Active_session) {
                        Intent iscustomer_reg = new Intent(MainActivity.this, Customer_Registration.class);
                        startActivity(iscustomer_reg);
                        MainActivity.this.finish();
                //    }
                //}else{
                //    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                //}
            }
        });

    }

    @Override
    public void onBackPressed() {
        new SweetAlertDialog(MainActivity.this,
                SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Logout")
                .setContentText("Do you want to logout?").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
                Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
                MainActivity.this.finish();
            }
        }).show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
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
                    bundle1.putString("MSG_KEY", "Session Active");
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
                    Bundle bundle6 = msg.getData();
                    String string6 = bundle6.getString("MSG_KEY");
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string6).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    Active_session = true;
                                    sDialog.dismissWithAnimation();
                                }
                            }).
                            show();

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