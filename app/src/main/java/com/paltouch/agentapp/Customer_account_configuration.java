package com.paltouch.agentapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Customer_account_configuration extends Activity {
    private EditText date_maturity;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_account_configuration);

        date_maturity = (EditText) findViewById(R.id.date_maturity);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Intent i = new Intent(Customer_account_configuration.this, Customer_Registration.class);
        startActivity(i);
        Customer_account_configuration.this.finish();
    }
    public void CustAccBackbutton_click(View v){
        Intent i = new Intent(Customer_account_configuration.this, Customer_Registration.class);
        startActivity(i);
    }
}
