package com.paltouch.agentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Customer_Registration extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);
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