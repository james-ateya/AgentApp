package com.paltouch.agentapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Customer_Registration extends Activity {
    TextView txt_dateofbirth;
    EditText Edt_Customername,edt_document_type_id,edt_phonenumber,edt_physical_location;
    Spinner spn_document_type,spn_phonecountry;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatePickerDialog.OnDateSetListener mDateSetListener_maturitydate;
    RelativeLayout customer_static_data,account_configs;
    JSONArray registration_account_list;
    JSONObject account_list;
    boolean data_back_registartion;
    ArrayList<String> readings = new ArrayList<String>();



    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> list1 = new ArrayList<String>();
    boolean data_back = false;
    String title[];
    private String country_name,country_id;
    String phonecountry_selected,phonecountry_id;

    ArrayList<String> list2 = new ArrayList<String>();
    ArrayList<String> list3 = new ArrayList<String>();
    boolean data_back2 = false;
    String title2[];
    String document_type_name,document_type_id,selected_document_type_name,selected_document_type_id;

    ArrayList<String> list4 = new ArrayList<String>();
    ArrayList<String> list5 = new ArrayList<String>();
    boolean data_back3 = false;
    String title3[];
    String AccountTypeName,AccountTypeId,selected_account_name,selected_account_id;

    ArrayList<String> list_timeschedule = new ArrayList<String>();
    ArrayList<String> list2_timeschedule = new ArrayList<String>();
    boolean data_back_timeschdule;
    String title_timeschedule[];
    String timeschdule_name,timeschdule_id,selected_timeschdule_name,selected_timeschdule_id;

    ArrayList<String> list_gendertype = new ArrayList<String>();
    ArrayList<String> list2_gendertype = new ArrayList<String>();
    boolean data_back_gendertype;
    String title_gendertype[];
    String gendertype_name,gendertype_id,selected_gendertype_name,selected_gendertype_id;

    //Second Interface
    Spinner spn_account_type,spn_lock_mode,spn_gendertypes;
    EditText edt_target_amount,edt_configured_accounts;
    TextView txt_date_of_maturity;
    Button btn_update_details,btnsave_details,btn_next;
    boolean account_configuration = false;
    boolean module_back = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_registration);

        registration_account_list = new JSONArray();
        account_list = new JSONObject();

        customer_static_data = (RelativeLayout) findViewById(R.id.wrapping_panel1);
        account_configs = (RelativeLayout) findViewById(R.id.wrapping_panel2);

        txt_dateofbirth = (TextView) findViewById(R.id.txt_dateofbirth);
        txt_dateofbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Customer_Registration.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String dateString = dateFormat.format(calendar.getTime());
                //month = month + 1;
                //String date = month + "/" + day + "/" + year;
                txt_dateofbirth.setText(dateString);
            }
        };

        spn_phonecountry = (Spinner) findViewById(R.id.spn_phonecountry);
        spn_phonecountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Get selected item and ID
                try {
                    phonecountry_selected = (String) title[position];
                    phonecountry_id = list1.get(position);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edt_phonenumber = (EditText) findViewById(R.id.edt_phonenumber);

        Edt_Customername = (EditText) findViewById(R.id.Edt_Customername);
        edt_physical_location = (EditText) findViewById(R.id.edt_physical_location);

        spn_document_type = (Spinner) findViewById(R.id.spn_document_type);
        spn_document_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selected_document_type_name = (String) title2[position];
                    selected_document_type_id = list3.get(position);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edt_document_type_id = (EditText) findViewById(R.id.edt_document_type_id);

        spn_gendertypes = (Spinner) findViewById(R.id.spn_gendertypes);
        spn_gendertypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selected_gendertype_name = (String) title_gendertype[position];
                    selected_gendertype_id = list2_gendertype.get(position);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validations
                if(Edt_Customername.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this,"Provide Client Name.",Toast.LENGTH_LONG).show();
                    Edt_Customername.requestFocus();
                }
                else if(spn_document_type == null || spn_document_type.getSelectedItem() == null){
                    Toast.makeText(Customer_Registration.this,"Select Client Document Type.",Toast.LENGTH_LONG).show();
                }
                else if(edt_document_type_id.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this,"Provide Client Identification Number.",Toast.LENGTH_LONG).show();
                    edt_document_type_id.requestFocus();
                }
                else if(spn_phonecountry == null || spn_phonecountry.getSelectedItem() == null){
                    Toast.makeText(Customer_Registration.this,"Select Client Phone Country.",Toast.LENGTH_LONG).show();
                }
                else if(edt_phonenumber.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this,"Provide client phone number.",Toast.LENGTH_LONG).show();
                    edt_phonenumber.requestFocus();
                }
                else if(edt_physical_location.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this,"Provide Client Physical Location.",Toast.LENGTH_LONG).show();
                    edt_physical_location.requestFocus();
                }
                else if(txt_dateofbirth.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this,"Provide Client Date of Birth.",Toast.LENGTH_LONG).show();
                }
                else if(spn_gendertypes == null || spn_gendertypes.getSelectedItem() == null){
                    Toast.makeText(Customer_Registration.this,"Select Client gender type.",Toast.LENGTH_LONG).show();
                }else {
                    //Check if suppied ID exists
                    if (getnetwork_state()) {
                        if (!module_back) {
                            CheckifIDexists check = new CheckifIDexists();
                            check.execute();
                        } else {
                            customer_static_data.setVisibility(View.GONE);
                            account_configs.setVisibility(View.VISIBLE);
                            account_configuration = true;
                            module_back = false;
                        }
                    }
                    else {
                        new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.WARNING_TYPE).
                                setTitleText("Internet Disconnected").
                                setContentText("Please check your internet connection and try again.").show();
                        return;
                    }
                }
            }
        });
        second_interface_config();

        if(getnetwork_state()){
            GetDocumentTypes getdocumenttypes = new GetDocumentTypes();
            getdocumenttypes.execute();
        }
        else{
            new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.WARNING_TYPE).
                    setTitleText("Internet Failure!").setContentText("Please check your internet connection").
                    setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent main = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(main);
                            Customer_Registration.this.finish();
                        }
                    }).
                    show();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if(account_configuration) {
            customer_static_data.setVisibility(View.VISIBLE);
            account_configs.setVisibility(View.GONE);
            account_configuration = false;
            module_back = true;
        }else{
            new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.WARNING_TYPE).
                    setTitleText("EXIT").setContentText("Do you want to Exit this page?").
                    showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                    setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Customer_Registration.super.onBackPressed();
                            Intent i = new Intent(Customer_Registration.this, MainActivity.class);
                            startActivity(i);
                            Customer_Registration.this.finish();
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
    }

    public void CustRegBackbutton_click(View v){
        Intent i = new Intent(Customer_Registration.this, MainActivity.class);
        startActivity(i);
        Customer_Registration.this.finish();
    }

    public void CustAccBackbutton_click(View v){
        customer_static_data.setVisibility(View.VISIBLE);
        account_configs.setVisibility(View.GONE);
        account_configuration = false;
        module_back = true;
    }

    void second_interface_config(){
        spn_account_type = (Spinner) findViewById(R.id.spn_account_type);
        spn_account_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selected_account_name = (String) title3[position];
                    selected_account_id = list5.get(position);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spn_lock_mode = (Spinner) findViewById(R.id.spn_lock_mode);
        spn_lock_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selected_timeschdule_name = (String) title_timeschedule[position];
                    selected_timeschdule_id = list2_timeschedule.get(position);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        edt_target_amount = (EditText) findViewById(R.id.edt_target_amount);
        txt_date_of_maturity = (TextView) findViewById(R.id.txt_date_of_maturity);
        txt_date_of_maturity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Customer_Registration.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener_maturitydate,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener_maturitydate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //month = month + 1;
                //String date = month + "/" + day + "/" + year;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String dateString = dateFormat.format(calendar.getTime());
                txt_date_of_maturity.setText(dateString);
            }
        };

        edt_configured_accounts = (EditText) findViewById(R.id.edt_configured_accounts);

        btn_update_details = (Button) findViewById(R.id.btn_update_details);
        btn_update_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validate Fields
                if(spn_account_type == null || spn_account_type.getSelectedItem() == null){
                    Toast.makeText(Customer_Registration.this, "Select Client Account.", Toast.LENGTH_LONG).show();
                }
                else if(spn_lock_mode == null || spn_lock_mode.getSelectedItem() == null){
                    Toast.makeText(Customer_Registration.this, "Select Client Time Schedule.", Toast.LENGTH_LONG).show();
                }
                else if(edt_target_amount.getText().toString().isEmpty() || Integer.parseInt(edt_target_amount.getText().toString()) <= 0){
                    Toast.makeText(Customer_Registration.this, "Supply Correct Amount.", Toast.LENGTH_LONG).show();
                    edt_target_amount.requestFocus();
                }
                else if(txt_date_of_maturity.getText().toString().isEmpty()){
                    Toast.makeText(Customer_Registration.this, "Supply Correct Maturity Date.", Toast.LENGTH_LONG).show();
                }else {
                    try {
                        if (readings.isEmpty()) {
                            readings.add(selected_account_name);
                            account_list.put("AccountTypeId", selected_account_id);
                            account_list.put("AccountTypeName", selected_account_name);
                            account_list.put("AccountTargetAmount", edt_target_amount.getText().toString());
                            account_list.put("AccountTargetTimeScheduleId", selected_timeschdule_id);
                            account_list.put("TimeScheduleName", selected_timeschdule_name);
                            account_list.put("FirstMaturityDate", txt_date_of_maturity.getText().toString());
                            account_list.put("MaxPermitedOperations",1);

                            registration_account_list.put(account_list);
                            account_list = new JSONObject();

                            StringBuffer sbitems = new StringBuffer();
                            for (int i = 0; i < readings.size(); i++) {
                                sbitems.append("Account " + (i + 1) + ": " + readings.get(i) + "\n");
                            }
                            edt_configured_accounts.setText("");
                            edt_configured_accounts.setText(sbitems.toString());

                            new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.SUCCESS_TYPE).
                                    setTitleText("UPDATED").
                                    setContentText("Account Details updated successfully.").show();
                        } else if (readings.contains(selected_account_name)) {
                            new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).
                                    setTitleText("Duplicate Account").
                                    setContentText(selected_account_name + " Has Already Been Configured.").show();
                            return;
                        } else {
                            readings.add(selected_account_name);
                            account_list.put("AccountTypeId", selected_account_id);
                            account_list.put("AccountTypeName", selected_account_name);
                            account_list.put("AccountTargetAmount", edt_target_amount.getText().toString());
                            account_list.put("AccountTargetTimeScheduleId", selected_timeschdule_id);
                            account_list.put("TimeScheduleName", selected_timeschdule_name);
                            account_list.put("FirstMaturityDate", txt_date_of_maturity.getText().toString());
                            account_list.put("MaxPermitedOperations",1);

                            registration_account_list.put(account_list);
                            account_list = new JSONObject();

                            StringBuffer sbitems = new StringBuffer();
                            for (int i = 0; i < readings.size(); i++) {
                                sbitems.append("Account " + (i + 1) + ": " + readings.get(i) + "\n");
                            }
                            edt_configured_accounts.setText("");
                            edt_configured_accounts.setText(sbitems.toString());

                            new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.SUCCESS_TYPE).
                                    setTitleText("UPDATED").
                                    setContentText("Account Details updated successfully.").show();
                            return;
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        btnsave_details = (Button) findViewById(R.id.btnsave_details);
        btnsave_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save all the details provided
                if(getnetwork_state()){
                    SaveCustomerDetails savedetails = new SaveCustomerDetails();
                    savedetails.execute();
                }else{
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.WARNING_TYPE).
                            setTitleText("Internet Disconnected").
                            setContentText("Please check your internet connection and try again.").show();
                    return;
                }

            }
        });
    }

    //Async task
    private class GetDocumentTypes extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GetDocumentType";
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
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(false);

                /*
                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(object1.toString());
                writer.flush();
                writer.close();
                out.close();

                 */

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
                    JSONArray Documenttypes = JsonResultVeriy.getJSONArray("Result");
                    int tr = Documenttypes.length();
                    if (tr >= 1) {
                        for (int i = 0; i < Documenttypes.length(); i++) {
                            JSONObject verifyresult2 = Documenttypes.getJSONObject(i);

                            document_type_name = verifyresult2.getString("Name");
                            document_type_id = verifyresult2.getString("Id");

                            data_back2 = true;
                            list2.add(document_type_name);
                            list3.add(document_type_id);
                        }

                        //Read the list
                        if (list2.size() <= 0) {
                            //Throw Error. No Record Found
                            data_back2 = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while getting document types. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 6;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title2 = list2.toArray(new String[list2.size()]);
                            data_back2 = true;
                        }
                    }else{
                        //Throw Error. No Record Found
                        data_back2 = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while fetching getting documrnt types.");
                        msg.setData(bundle);
                        msg.what = 6;
                        mhandler.sendMessage(msg);
                    }

                }
                else {
                    data_back2 = false;
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
                    msg1.what = 6;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                e.printStackTrace();
                data_back2 = false;
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: " + e.getMessage());
                msg1.setData(bundle1);
                msg1.what = 6;
                mhandler.sendMessage(msg1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back2) {
                //Load Data to Interface
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(Customer_Registration.this, android.R.layout.simple_list_item_1, title2);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_document_type.setAdapter(AccountsApapdter); // causes nullpointerexception

                GetPhoneCountryCodes phonecountrycodes = new GetPhoneCountryCodes();
                phonecountrycodes.execute();

            }else{
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class GetPhoneCountryCodes extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GetCountryCode";
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
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(false);
                /*
                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(object1.toString());
                writer.flush();
                writer.close();
                out.close();

                 */

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
                    JSONArray Phonecountrycodes = JsonResultVeriy.getJSONArray("Result");
                    int tr = Phonecountrycodes.length();
                    if (tr >= 1) {
                        for (int i = 0; i < Phonecountrycodes.length(); i++) {
                            JSONObject verifyresult2 = Phonecountrycodes.getJSONObject(i);

                            country_name = verifyresult2.getString("Name");
                            country_id = verifyresult2.getString("Id");

                            data_back = true;
                            list.add(country_name);
                            list1.add(country_id);
                        }

                        //Read the list
                        if (list.size() <= 0) {
                            //Throw Error. No Record Found
                            data_back = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while getting phone country codes. Please consult system admin.");
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
                        if(pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        data_back = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while getting Phone country codes.");
                        msg.setData(bundle);
                        msg.what = 2;
                        mhandler.sendMessage(msg);
                    }

                }
                else {
                    if(pDialog.isShowing()){
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
                if(pDialog.isShowing()){
                    pDialog.dismiss();
                }
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
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back) {
                //Load Data to Interface
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(Customer_Registration.this, android.R.layout.simple_list_item_1, title);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_phonecountry.setAdapter(AccountsApapdter); // causes nullpointerexception

                GetGenderTypes getgendertypes = new GetGenderTypes();
                getgendertypes.execute();

            }else{
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class GetGenderTypes extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GetGenderType";
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
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(false);

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
                    JSONArray timeschdule_list = JsonResultVeriy.getJSONArray("Result");
                    int tr = timeschdule_list.length();
                    if (tr >= 1) {
                        for (int i = 0; i < timeschdule_list.length(); i++) {
                            JSONObject verifyresult2 = timeschdule_list.getJSONObject(i);

                            gendertype_name = verifyresult2.getString("Name");
                            gendertype_id = verifyresult2.getString("Id");

                            data_back_gendertype = true;
                            list_gendertype.add(gendertype_name);
                            list2_gendertype.add(gendertype_id);
                        }

                        //Read the list
                        if (list_gendertype.size() <= 0) {
                            //Throw Error. No Record Found
                            data_back_gendertype = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while getting time schedule. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 2;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title_gendertype = list_gendertype.toArray(new String[list_gendertype.size()]);
                            data_back_gendertype = true;
                        }
                    }else{
                        //Throw Error. No Record Found
                        data_back_gendertype = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while getting time schedules.");
                        msg.setData(bundle);
                        msg.what = 2;
                        mhandler.sendMessage(msg);
                    }

                }
                else {
                    data_back_gendertype = false;
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
                data_back_gendertype = false;
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
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back_gendertype) {
                //Load Data to Interface
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(Customer_Registration.this, android.R.layout.simple_list_item_1, title_gendertype);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_gendertypes.setAdapter(AccountsApapdter); // causes nullpointerexception

                GetDefaultCustomerAccounts getcustomeraccounts = new GetDefaultCustomerAccounts();
                getcustomeraccounts.execute();

            }else{
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class CheckifIDexists extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Checking if I.D. Exists...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GenerateAgentDetailsFromNatId";
            JSONObject object1;
            object1 = new JSONObject();

            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                object1.put("NationalId",edt_document_type_id.getText().toString());
                object1.put("DocumentTypeId",selected_document_type_id);
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
                    //JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    //JSONArray check_id = JsonResultVeriy.getJSONArray("Result");
                    data_back2 = true;
                }
                else {
                    try {
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
                        String response_errormessage = JsonResulterror.getString("TechnicalMessage");
                        System.out.println("TechnicalMessage >>>>>>" + response_errormessage);
                        if (response_errormessage.equalsIgnoreCase("NotFound")) {
                            data_back2 = false;
                        } else {
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            Message msg1 = mhandler.obtainMessage();
                            Bundle bundle1 = new Bundle();
                            bundle1.putString("MSG_KEY", response_errormessage);
                            msg1.setData(bundle1);
                            msg1.what = 6;
                            mhandler.sendMessage(msg1);
                        }
                    }catch (Exception e){
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        Message msg1 = mhandler.obtainMessage();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("MSG_KEY", e.toString());
                        msg1.setData(bundle1);
                        msg1.what = 6;
                        mhandler.sendMessage(msg1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(pDialog.isShowing()){
                    pDialog.dismiss();
                }
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "JSON Exception: " + e.getMessage());
                msg1.setData(bundle1);
                msg1.what = 6;
                mhandler.sendMessage(msg1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void file_url) {
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back2) {
                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "The Customer is already registered, use his/her ID Number as account number.");
                msg1.setData(bundle1);
                msg1.what = 6;
                mhandler.sendMessage(msg1);
            }else{
                customer_static_data.setVisibility(View.GONE);
                account_configs.setVisibility(View.VISIBLE);
                account_configuration = true;
            }
        }
    }

    private class GetDefaultCustomerAccounts extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GetDefaultAccounts";
            //String serviceurl = GlobalVariables.surl +"/Members/AddMemberAccounts/AccountNames";
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
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(false);

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
                    JSONArray CustomerAccounts = JsonResultVeriy.getJSONArray("Result");
                    int tr = CustomerAccounts.length();
                    if (tr >= 1) {
                        for (int i = 0; i < CustomerAccounts.length(); i++) {
                            JSONObject verifyresult2 = CustomerAccounts.getJSONObject(i);

                            AccountTypeName = verifyresult2.getString("AccountTypeName");
                            AccountTypeId = verifyresult2.getString("AccountTypeId");

                            //AccountTypeName = verifyresult2.getString("name");
                            //AccountTypeId = verifyresult2.getString("id");

                            data_back3 = true;
                            list4.add(AccountTypeName);
                            list5.add(AccountTypeId);
                        }

                        //Read the list
                        if (list4.size() <= 0) {
                            //Throw Error. No Record Found
                            data_back3 = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while getting customer accounts. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 2;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title3 = list4.toArray(new String[list4.size()]);
                            data_back3 = true;
                        }
                    }else{
                        //Throw Error. No Record Found
                        data_back3 = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while getting customer accounts.");
                        msg.setData(bundle);
                        msg.what = 2;
                        mhandler.sendMessage(msg);
                    }

                }
                else {
                    data_back3 = false;
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
                    msg1.what = 7;
                    mhandler.sendMessage(msg1);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                data_back3 = false;
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
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back3) {
                //Load Data to Interface
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(Customer_Registration.this, android.R.layout.simple_list_item_1, title3);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_account_type.setAdapter(AccountsApapdter); // causes nullpointerexception

                GetTimeSchedule gettimeshedule = new GetTimeSchedule();
                gettimeshedule.execute();

            }else{
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class GetTimeSchedule extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Loading Data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/GetTimeSchedule";
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
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", GlobalVariables.session_token);
                conn.setDoInput(true);
                conn.setDoOutput(false);

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
                    JSONArray timeschdule_list = JsonResultVeriy.getJSONArray("Result");
                    int tr = timeschdule_list.length();
                    if (tr >= 1) {
                        for (int i = 0; i < timeschdule_list.length(); i++) {
                            JSONObject verifyresult2 = timeschdule_list.getJSONObject(i);

                            timeschdule_name = verifyresult2.getString("Name");
                            timeschdule_id = verifyresult2.getString("Id");

                            data_back_timeschdule = true;
                            list_timeschedule.add(timeschdule_name);
                            list2_timeschedule.add(timeschdule_id);
                        }

                        //Read the list
                        if (list_timeschedule.size() <= 0) {
                            //Throw Error. No Record Found
                            data_back_timeschdule = false;
                            Message msg = mhandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("MSG_KEY", "No data returned from the server while getting time schedule. Please consult system admin.");
                            msg.setData(bundle);
                            msg.what = 2;
                            mhandler.sendMessage(msg);
                        } else {
                            //Convert list to array
                            title_timeschedule = list_timeschedule.toArray(new String[list_timeschedule.size()]);
                            data_back_timeschdule = true;
                        }
                    }else{
                        //Throw Error. No Record Found
                        data_back_timeschdule = false;
                        Message msg = mhandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG_KEY", "No records returned from server while getting time schedules.");
                        msg.setData(bundle);
                        msg.what = 2;
                        mhandler.sendMessage(msg);
                    }

                } else {
                    data_back_timeschdule = false;
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
                e.printStackTrace();
                data_back_timeschdule = false;
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
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back_timeschdule) {
                //Load Data to Interface
                final ArrayAdapter<String> AccountsApapdter =
                        new ArrayAdapter<String>(Customer_Registration.this, android.R.layout.simple_list_item_1, title_timeschedule);
                AccountsApapdter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                AccountsApapdter.notifyDataSetChanged();
                spn_lock_mode.setAdapter(AccountsApapdter); // causes nullpointerexception

            }else{
                //new SweetAlertDialog(Inventory_Register.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO DATA").setContentText("There seems to be an issue, please contact system admin.").show();
                return;
            }
        }
    }

    private class SaveCustomerDetails extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Customer_Registration.this);
            pDialog.setMessage("Saving Client Details...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String serviceurl = GlobalVariables.surl +"/Agency/ClientRegistration/AgencyClientRegistration/Add";
            JSONObject object1;
            JSONArray object2;
            object1 = new JSONObject();
            object2 = new JSONArray();

            URL url = null;
            try {
                url = new URL(serviceurl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                object1.put("NationalId",edt_document_type_id.getText().toString());
                object1.put("Phone",edt_phonenumber.getText().toString());
                object1.put("Name",Edt_Customername.getText().toString());
                object1.put("DateofBirth",txt_dateofbirth.getText().toString());
                object1.put("GenderTypeId",selected_gendertype_id);
                object1.put("DocumentTypeId",selected_document_type_id);
                object1.put("PhoneCountryCodeId",phonecountry_id);
                object1.put("PhysicalAddress",edt_physical_location.getText().toString());
                object1.put("AgencyRegistrationClientAccount",registration_account_list);

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
                    //JSONObject JsonResultVeriy = new JSONObject(JsonResult);
                    data_back_registartion = true;

                } else {
                    data_back_registartion = false;
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
                data_back_registartion = false;
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
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (data_back_registartion){
                edt_target_amount.setText("");
                txt_date_of_maturity.setText("");
                selected_account_id = null;
                selected_account_name = null;
                selected_timeschdule_id = null;
                selected_timeschdule_name = null;
                edt_document_type_id.setText("");
                edt_phonenumber.setText("");
                Edt_Customername.setText("");
                txt_dateofbirth.setText("");
                edt_physical_location.setText("");
                selected_gendertype_id = null;
                selected_document_type_id = null;
                phonecountry_selected = null;
                registration_account_list = new JSONArray();
                readings.clear();
                edt_configured_accounts.setText("");

                Message msg1 = mhandler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putString("MSG_KEY", "Client Registered Successfully.");
                msg1.setData(bundle1);
                msg1.what = 4;
                mhandler.sendMessage(msg1);
            }else {
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
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string1).show();

                    break;

                case 2:
                    Bundle bundle2 = msg.getData();
                    String string2 = bundle2.getString("MSG_KEY");
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).setTitleText("SORRY").setContentText(string2).show();

                    break;
                case 3:

                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String string = bundle.getString("MSG_KEY");
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.SUCCESS_TYPE).
                            setTitleText("SUCCESS!").setContentText(string).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent i = new Intent(Customer_Registration.this, MainActivity.class);
                                    startActivity(i);
                                    Customer_Registration.this.finish();
                                }
                            }).
                            show();

                    break;
                case 5:
                    Bundle bundle5 = msg.getData();
                    String string5 = bundle5.getString("MSG_KEY");
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").
                            setContentText(string5).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    return;
                                }
                            }).
                            show();

                    break;

                case 6:
                    Bundle bundle6 = msg.getData();
                    String string6 = bundle6.getString("MSG_KEY");
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY!").setContentText(string6).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent i = new Intent(Customer_Registration.this, MainActivity.class);
                                    startActivity(i);
                                    Customer_Registration.this.finish();
                                }
                            }).
                            show();

                    break;

                case 7:
                    Bundle bundle7 = msg.getData();
                    String string7 = bundle7.getString("MSG_KEY");
                    new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).
                            setTitleText("SORRY").setContentText(string7).
                            setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(main);
                                    Customer_Registration.this.finish();
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
                        new SweetAlertDialog(Customer_Registration.this, SweetAlertDialog.ERROR_TYPE).setTitleText("NO INTERNET").setContentText("Make sure you have internet connection.").show();
                    }

                    break;
            }

        };
    };

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
}