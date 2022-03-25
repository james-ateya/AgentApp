package com.paltouch.agentapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AgentDailyReport extends AppCompatActivity {
    private ListView listView_report_data;
    private TextView txt_start_date,txt_totalcollection;
    private Button btn_veiwreport;
    private DatePickerDialog.OnDateSetListener mDateSetListener_startdate;
    private ArrayList<agent_report_model> agent_report_model_ArrayList;
    DatabaseHelper dbhelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_daily_report);

        dbhelper = new DatabaseHelper(getApplicationContext());
        db = dbhelper.getWritableDatabase();

        listView_report_data = (ListView) findViewById(R.id.listView_report_data);
        txt_totalcollection = (TextView) findViewById(R.id.txt_totalcollection);
        txt_start_date = (TextView) findViewById(R.id.txt_start_date);
        txt_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        AgentDailyReport.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener_startdate,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        btn_veiwreport = (Button) findViewById(R.id.btn_veiwreport);
        btn_veiwreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(txt_start_date.getText().toString().isEmpty()){
                        Toast.makeText(AgentDailyReport.this,"Select Date.",Toast.LENGTH_LONG).show();
                        return;
                    }else {
                        agent_report_model_ArrayList = populateList();
                        agent_report_adapter AgentAdapter = new agent_report_adapter(AgentDailyReport.this, agent_report_model_ArrayList);
                        listView_report_data.setAdapter(AgentAdapter);

                        //Select sum total
                        String total_deposited = null;
                        Cursor cursor = db.rawQuery("SELECT sum(deposited_amount) as total_deposit FROM agent_report where date = '"+ txt_start_date.getText().toString() +"'",null);
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                int index2 = cursor.getColumnIndex("total_deposit");
                                total_deposited = cursor.getString(index2);
                            }
                        }
                        if(!total_deposited.isEmpty() || total_deposited != null){
                            txt_totalcollection.setText("Total "+total_deposited);
                        }else{
                            txt_totalcollection.setText("Total 0.0");
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    return;
                }
            }
        });

        mDateSetListener_startdate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //month = month + 1;
                //String date = month + "/" + day + "/" + year;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy");
                String dateString = dateFormat.format(calendar.getTime());
                txt_start_date.setText(dateString);
            }
        };
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        new SweetAlertDialog(AgentDailyReport.this, SweetAlertDialog.WARNING_TYPE).
                setTitleText("EXIT").setContentText("Do you want to Exit this page?").
                showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        AgentDailyReport.super.onBackPressed();
                        Intent main = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(main);
                        AgentDailyReport.this.finish();
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
    public void DailyReportBackbutton_click(View v){
        new SweetAlertDialog(AgentDailyReport.this, SweetAlertDialog.WARNING_TYPE).
                setTitleText("EXIT").setContentText("Do you want to Exit this page?").
                showCancelButton(true).setConfirmText("YES").setCancelText("NO").
                setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        AgentDailyReport.super.onBackPressed();
                        Intent main = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(main);
                        AgentDailyReport.this.finish();
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

    private ArrayList<agent_report_model> populateList(){

        ArrayList<agent_report_model> list = new ArrayList<>();

        String[] columns = {DatabaseHelper.FULL_NAME, DatabaseHelper.ACCOUNT_NAME,DatabaseHelper.DEPOSITED_AMOUNT};
        String[] args = {txt_start_date.getText().toString()};
        Cursor cursor = db.query(DatabaseHelper.AGENT_REPORT_TABLE_NAME, columns,
                DatabaseHelper.DATE + "= '" + txt_start_date.getText().toString() +"'", null, null,
                null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                agent_report_model agent_model = new agent_report_model();

                int index2 = cursor.getColumnIndex(DatabaseHelper.FULL_NAME);
                agent_model.setFull_name(cursor.getString(index2));

                int index3 = cursor.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
                agent_model.setAccount_name(cursor.getString(index3));

                int index4 = cursor.getColumnIndex(DatabaseHelper.DEPOSITED_AMOUNT);
                agent_model.setAmount(cursor.getString(index4));

                list.add(agent_model);
            }
        }
        else {
            //Nothing
        }
        cursor.close();
        return list;
    }
}