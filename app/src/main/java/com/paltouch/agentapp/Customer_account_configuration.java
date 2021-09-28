package com.paltouch.agentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Customer_account_configuration extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_account_configuration);


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
