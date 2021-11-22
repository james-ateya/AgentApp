package com.paltouch.agentapp;

import static com.paltouch.agentapp.GlobalVariables.apk_version;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

public class LoginActivity extends Activity implements PermissionUtils.PermissionResultCallback {
    private static ProgressDialog pd;
    private static android.app.AlertDialog.Builder alertDialog;
    private static Context mContext;
    private static Activity activity;
    public Button btn_login;
    public EditText edt_username;
    public EditText edt_password;
    private TextView LostPassword;
    ArrayList<String> permissions=new ArrayList<>();
    PermissionUtils permissionUtils;

    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        permissionUtils=new PermissionUtils(LoginActivity.this);

        permissions.add(Manifest.permission.BLUETOOTH);
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

        permissionUtils.check_permission(permissions,"Needed Permissions",1);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            new AlertDialog.Builder(LoginActivity.this).setTitle("BLUETOOTH UNAVAILABLE").setMessage("Sorry,Your device does not support bluetoth").show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            //Do Nothing, already enabled.
        }

        pd = new ProgressDialog(this);
        alertDialog = new android.app.AlertDialog.Builder(LoginActivity.this);
        mContext = this;
        activity = this;

        btn_login = (Button) findViewById(R.id.btnSingIn);
        edt_username = (EditText) findViewById(R.id.etUserName);
        edt_password = (EditText) findViewById(R.id.etPass);

        LostPassword = (TextView) findViewById(R.id.lost_password);
        LostPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Change Password?");
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
                        if(edt_username.getText().toString().isEmpty()) {
                            Toast.makeText(LoginActivity.this,"Please Provide your username.",Toast.LENGTH_LONG).show();
                            edt_username.requestFocus();
                        }else{
                            GlobalVariables.touseusername = edt_username.getText().toString();
                            Intent f_password = new Intent(LoginActivity.this, ForgetPassword.class);
                            startActivity(f_password);
                            LoginActivity.this.finish();
                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_username.getText().toString().length() < 1) {
                    new SweetAlertDialog(LoginActivity.this,
                            SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("ERROR...")
                            .setContentText("Please Enter your Username").show();

                } else if (edt_password.getText().toString().length() < 1) {
                    new SweetAlertDialog(LoginActivity.this,
                            SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("ERROR...")
                            .setContentText("Please Enter your password").show();
                } else {
                    GlobalVariables.username=edt_username.getText().toString();
                    GlobalVariables.password=edt_password.getText().toString();

                    //Intent confirm=new Intent(getApplicationContext(), MainActivity.class);
                    //startActivity(confirm);
                    //LoginActivity.this.finish();

                    login_User login = new login_User();
                    login.execute();

                    //Intent a = new Intent(LoginActivity.this, APK_Update.class);
                    //startActivity(a);
                }
            }
        });
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

                break;
            case R.id.action_kiosk:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class login_User extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please Wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... params) {
            //Get data and store in List
            String serviceurl = GlobalVariables.surl + "/SystemAccounts/Authentication/Login/SimpleSubmit";
            JSONObject object1;
            object1 = new JSONObject();
            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                pDialog.dismiss();
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Malformed URL, Explanation: "+e.getMessage());
                msg1.setData(bundle1);
                msg1.what=5;
                mhandler.sendMessage(msg1);
            }
            try {
                //UserName: "eric", PassWord: "Masai*201514", AccountName: "", Branch: "", UseSMSForOTP: true
                object1.put("UserName", GlobalVariables.username);
                object1.put("Password", GlobalVariables.password);
                object1.put("AccountName","");
                object1.put("Branch","");
                object1.put("UseSMSForOTP","false");
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
                System.out.println("ATEYA" + serverAuth);
                GlobalVariables.session_token = serverAuth;
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
                    String response_message = jresponse.getString("Message");

                    Message msg = mhandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG_KEY", response_message);
                    msg.setData(bundle);
                    msg.what=4;
                    mhandler.sendMessage(msg);

                }
                else {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    try {
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
                        Message msg1 = mhandler.obtainMessage();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("MSG_KEY", response_errormessage);
                        msg1.setData(bundle1);
                        msg1.what = 5;
                        mhandler.sendMessage(msg1);
                    }catch (Exception e){
                        Message msg1 = mhandler.obtainMessage();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("MSG_KEY", e.getMessage());
                        msg1.setData(bundle1);
                        msg1.what = 5;
                        mhandler.sendMessage(msg1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", e.getMessage());
                msg1.setData(bundle1);
                msg1.what = 5;
                mhandler.sendMessage(msg1);
            } catch (JSONException e) {
                e.printStackTrace();
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "There seems to be a problem with the current user, please contact system admin.");
                msg1.setData(bundle1);
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

    private class check_apk_updates extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
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
                    JSONObject jresponse = JsonResultVeriy.getJSONObject("Result");

                    apk_version = jresponse.getString("apk_version");
                    GlobalVariables.apk_name = jresponse.getString("apk_name");
                    GlobalVariables.server_path = jresponse.getString("server_path");

                } else {
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
    // Permission check functions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // redirects to utils
        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION","GRANTED");
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY","GRANTED");
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION","DENIED");
    }

    @Override
    public void NeverAskAgain(int request_code) {
        Log.i("PERMISSION","NEVER ASK AGAIN");
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
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string1).show();

                    break;

                case 2:
                    Bundle bundle2 = msg.getData();
                    String string2 = bundle2.getString("MSG_KEY");
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string2).show();

                    break;
                case 3:

                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("MSG_KEY");
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent confirm=new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(confirm);
                                    LoginActivity.this.finish();
                                }
                            }).
                            show();

                    break;
                case 5:
                    Bundle bundle5 = msg.getData();
                    String string5 = bundle5.getString("MSG_KEY");
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string5).show();

            }

        };
    };
}
