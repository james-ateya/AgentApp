package com.paltouch.agentapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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

public class ForgetPassword extends Activity {
    EditText edtphone, edtemail, edtcode, edtpasswrd, edtconfirpass;
    Button btnconfirm, btngetcode;
    RadioGroup rdogrp;
    RadioButton rdophone, rdomail;

    String selected_mode;
    boolean yes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        edtphone = (EditText) findViewById(R.id.edtconf_phone);
        edtemail = (EditText) findViewById(R.id.edtconf_email);
        edtcode = (EditText) findViewById(R.id.edtcode);
        btnconfirm = (Button) findViewById(R.id.btnconfirm);
        btngetcode = (Button) findViewById(R.id.btngetcode);
        rdogrp = (RadioGroup) findViewById(R.id.rdogrp);
        rdomail = (RadioButton) findViewById(R.id.rdoemail);
        rdophone = (RadioButton) findViewById(R.id.rdophone);
        edtpasswrd = (EditText) findViewById(R.id.edtfirstpassowrd);
        edtconfirpass = (EditText) findViewById(R.id.edtconfirmpass);

        //Fetch User Details using the username entered at login
        if ((mobiledata() == NetworkInfo.State.CONNECTED)
                || (mobiledata() == NetworkInfo.State.CONNECTING)
                || (wifidata() == NetworkInfo.State.CONNECTED)
                || (wifidata() == NetworkInfo.State.CONNECTING))
        {
            //Transfer code to asynctask
            load_userdata usrdata = new load_userdata();
            usrdata.execute();
        }else{
            //No Internet Connection
            new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.SUCCESS_TYPE).
                    setTitleText("NO INTERNET!").setContentText("Make sure you have internet connection.").
                    setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(login);
                            ForgetPassword.this.finish();
                        }
                    }).
                    show();
        }

        btngetcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rdomail.isChecked()) {
                    GlobalVariables.UseSMSforSendingCode = false;
                    selected_mode = "EMAIL";
                    //Do Something
                    if ((mobiledata() == NetworkInfo.State.CONNECTED)
                            || (mobiledata() == NetworkInfo.State.CONNECTING)
                            || (wifidata() == NetworkInfo.State.CONNECTED)
                            || (wifidata() == NetworkInfo.State.CONNECTING))
                    {
                        confirmid confirm = new confirmid();
                        confirm.execute();
                        //Transfer code to asynctask
                    }else{
                        //No Internet Connection
                        new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.SUCCESS_TYPE).
                                setTitleText("NO INTERNET!").setContentText("Make sure you have internet connection.").
                                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(login);
                                        ForgetPassword.this.finish();
                                    }
                                }).
                                show();
                    }
                } else if (rdophone.isChecked()) {
                    GlobalVariables.UseSMSforSendingCode = true;
                    selected_mode = "SMS";
                    //Do Something
                    if ((mobiledata() == NetworkInfo.State.CONNECTED)
                            || (mobiledata() == NetworkInfo.State.CONNECTING)
                            || (wifidata() == NetworkInfo.State.CONNECTED)
                            || (wifidata() == NetworkInfo.State.CONNECTING))
                    {
                        confirmid confirm = new confirmid();
                        confirm.execute();
                        //Transfer code to asynctask
                    }else{
                        //No Internet Connection
                        new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.SUCCESS_TYPE).
                                setTitleText("NO INTERNET!").setContentText("Make sure you have internet connection.").
                                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(login);
                                        ForgetPassword.this.finish();
                                    }
                                }).
                                show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select Mail Or Phone Number", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtcode.getText().toString().length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your code", Toast.LENGTH_LONG).show();
                } else if (edtpasswrd.getText().toString().length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please Enter Password to use", Toast.LENGTH_LONG).show();
                } else if (edtconfirpass.getText().toString().length() < 1) {
                    Toast.makeText(getApplicationContext(), "Please Confirm Your Password", Toast.LENGTH_LONG).show();
                } else if (!checkPass()) {
                    Toast.makeText(getApplicationContext(), "Please Make sure password is the same", Toast.LENGTH_LONG).show();
                } else {
                    GlobalVariables.secretecode = edtcode.getText().toString();
                    GlobalVariables.fstpasswrd = edtpasswrd.getText().toString();
                    GlobalVariables.confpasswrd = edtconfirpass.getText().toString();

                    //Do Something
                    if ((mobiledata() == NetworkInfo.State.CONNECTED)
                            || (mobiledata() == NetworkInfo.State.CONNECTING)
                            || (wifidata() == NetworkInfo.State.CONNECTED)
                            || (wifidata() == NetworkInfo.State.CONNECTING))
                    {
                        confirmcode concode = new confirmcode();
                        concode.execute();
                        //Transfer code to asynctask
                    }else{
                        //No Internet Connection
                        new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    }
                }

            }
        });
        rdogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdophone) {
                    GlobalVariables.UseSMSforSendingCode = true;
                } else if (checkedId == R.id.rdoemail) {
                    GlobalVariables.UseSMSforSendingCode = false;
                }
            }
        });

    }

    //KeyBack Pressed
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //start Alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Exit Window?");
            builder.setInverseBackgroundForced(true);

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent menu = new Intent(ForgetPassword.this,LoginActivity.class);
                    startActivity(menu);
                    ForgetPassword.this.finish();
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public Boolean checkPass() {
        Boolean pass = false;
        String newpass = edtpasswrd.getText().toString();
        String confpass = edtconfirpass.getText().toString();

        if (newpass.equalsIgnoreCase(confpass)) {
            pass = true;
        }
        return pass;
    }


    NetworkInfo.State mobiledata() {
        Log.v("#######","Trying to get mobile data");
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(0).getState();
    }

    NetworkInfo.State wifidata() {
        Log.v("#######","Trying to get wifi");
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(1).getState();
    }

    private class load_userdata extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(ForgetPassword.this);
            pDialog.setMessage("Getting details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            //Get data and store in List
            String serviceurl = GlobalVariables.surl + "/SystemAccounts/Authentication/UserProfile/ForgotPasswordSimple";
            JSONObject object1;
            object1 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                yes = false;
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Malformed URL, Explanation: "+e.getMessage());
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            }
            try {
                object1.put("UserName", GlobalVariables.touseusername);
                object1.put("PassWord", "");
                object1.put("AccountName","");
                object1.put("Branch","");
                object1.put("ShowCommandButtons", "true");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
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
                String serverAuth = conn.getHeaderField("Authorization");
                GlobalVariables.session_token = serverAuth;
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    System.out.println("" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    JSONObject jclientdetails = JsonResultVeriy.getJSONObject("Result");
                    JSONObject jclientdetails2 = jclientdetails.getJSONObject("Result");

                    GlobalVariables.conf_phone = jclientdetails2.getString("PhoneNumber");
                    GlobalVariables.conf_email = jclientdetails2.getString("EmailAdress");
                    yes = true;
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                }
                else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    yes = false;
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        sb.append(line1 + "\n");
                    }
                    br1.close();
                    System.out.println("%%%%> " + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResulterror = new JSONObject(JsonResult);
                    //JSONObject error_object = JsonResulterror.getJSONObject("Result");
                    String response_errormessage = JsonResulterror.getString("Message");
                    System.out.println("Message >>>>>>" + response_errormessage);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", response_errormessage);
                    Message msg1 = mhandler.obtainMessage();
                    msg1.setData(bundle1);
                    msg1.what=5;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                yes = false;
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "System Encountered a connection problem, please try again.");
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            } catch (JSONException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                yes = false;
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: "+e.getMessage());
                msg1.setData(bundle1);
                msg1.what=5;
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
            if(yes == true){
                edtemail.setText("" + GlobalVariables.conf_email);
                edtphone.setText("" + GlobalVariables.conf_phone);
            }

        }
    }

    private class confirmid extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(ForgetPassword.this);
            pDialog.setMessage("Change Password...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            //Get data and store in List
            String serviceurl = GlobalVariables.surl + "/SystemAccounts/Authentication/UserProfile/SendRenewpasswordSecretCode";
            JSONObject object1,object2;
            object1 = new JSONObject();
            object2 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                pDialog.dismiss();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Malformed URL, Explanation: "+e.getMessage());
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            }
            try {
                object2.put("UseSMSforSendingCode", GlobalVariables.UseSMSforSendingCode);
                object1.put("RenewPasswordModel", object2);
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
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", "Secret code sent to your "+selected_mode);
                    Message msg1 = mhandler.obtainMessage();
                    msg1.setData(bundle1);
                    msg1.what=6;
                    mhandler.sendMessage(msg1);

                } else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        sb.append(line1 + "\n");
                    }
                    br1.close();
                    System.out.println("%%%%> " + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResulterror = new JSONObject(JsonResult);
                    JSONObject error_object = JsonResulterror.getJSONObject("Result");
                    String response_errormessage = error_object.getString("Message");
                    System.out.println("Message >>>>>>" + response_errormessage);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", response_errormessage);
                    Message msg1 = mhandler.obtainMessage();
                    msg1.setData(bundle1);
                    msg1.what=5;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "System Encountered a connection problem, please try again.");
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            } catch (JSONException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: "+e.getMessage());
                msg1.setData(bundle1);
                msg1.what=5;
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

    private class confirmcode extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(ForgetPassword.this);
            pDialog.setMessage("Please Wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            //Get data and store in List
            String serviceurl = GlobalVariables.surl + "/SystemAccounts/Authentication/UserProfile/RenewFirstTime";
            JSONObject object1,object2;
            object1 = new JSONObject();
            object2 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                pDialog.dismiss();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Malformed URL, Explanation: "+e.getMessage());
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            }
            try {
                object2.put("UseSMSforSendingCode", GlobalVariables.UseSMSforSendingCode);
                //object2.put("OldPassWord", GlobalVariables.oldpasswrd);
                object2.put("NewPassWord1", GlobalVariables.fstpasswrd);
                object2.put("NewPassWord2", GlobalVariables.confpasswrd);
                object2.put("SecretCode", GlobalVariables.secretecode);
                object1.put("RenewPasswordModel",object2);
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
                    System.out.println("" + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    JSONObject jclientdetails = JsonResultVeriy.getJSONObject("Result");

                    String response_message = jclientdetails.getString("Message");
                    Message msg = mhandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG_KEY", response_message);
                    msg.setData(bundle);
                    msg.what=4;
                    mhandler.sendMessage(msg);

                } else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        sb.append(line1 + "\n");
                    }
                    br1.close();
                    System.out.println("%%%%> " + sb.toString());
                    String JsonResult = sb.toString();
                    JSONObject JsonResulterror = new JSONObject(JsonResult);
                    JSONObject error_object = JsonResulterror.getJSONObject("Result");
                    String response_errormessage = error_object.getString("Message");
                    System.out.println("Message >>>>>>" + response_errormessage);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("MSG_KEY", response_errormessage);
                    Message msg1 = mhandler.obtainMessage();
                    msg1.setData(bundle1);
                    msg1.what=5;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "System Encountered a connection problem, please try again.");
                Message msg1 = mhandler.obtainMessage();
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            } catch (JSONException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: "+e.getMessage());
                msg1.setData(bundle1);
                msg1.what=5;
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

    Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Bundle bundle1 = msg.getData();
                    String string1 = bundle1.getString("MSG_KEY");
                    new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string1).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(login);
                            ForgetPassword.this.finish();
                        }
                    }).show();

                    break;

                case 2:
                    Bundle bundle2 = msg.getData();
                    String string2 = bundle2.getString("MSG_KEY");
                    new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string2).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(login);
                            ForgetPassword.this.finish();
                        }
                    }).show();

                    break;
                case 3:

                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("MSG_KEY");
                    new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(login);
                                    ForgetPassword.this.finish();
                                }
                            }).
                            show();

                    break;
                case 5:
                    Bundle bundle5 = msg.getData();
                    String string5 = bundle5.getString("MSG_KEY");
                    new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").
                            setContentText(string5).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent login=new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(login);
                            ForgetPassword.this.finish();
                        }
                    }).show();
                    break;

                case 6:
                    Bundle bundle6 = msg.getData();
                    String string6 = bundle6.getString("MSG_KEY");
                    new SweetAlertDialog(ForgetPassword.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string6).
                            show();
                    break;

            }

        };
    };
}
