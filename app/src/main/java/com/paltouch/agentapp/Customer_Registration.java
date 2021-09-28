package com.paltouch.agentapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class Customer_Registration extends Activity {
    TextView txt_dateofdate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);

        txt_dateofdate = (TextView) findViewById(R.id.txt_dateofbirth);
        txt_dateofdate.setOnClickListener(new View.OnClickListener() {
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
                month = month + 1;
                String date = month + "/" + day + "/" + year;
                txt_dateofdate.setText(date);
            }
        };
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent i = new Intent(Customer_Registration.this, MainActivity.class);
        startActivity(i);
        Customer_Registration.this.finish();
    }

    public void CustRegBackbutton_click(View v){
        Intent i = new Intent(Customer_Registration.this, MainActivity.class);
        startActivity(i);
        Customer_Registration.this.finish();
    }

    public void CustRegNextbutton_click(View v){
        Intent i = new Intent(Customer_Registration.this, Customer_account_configuration.class);
        startActivity(i);
        //Customer_Registration.this.finish();
    }
}