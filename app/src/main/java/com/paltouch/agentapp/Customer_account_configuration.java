package com.paltouch.agentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class Customer_account_configuration extends Activity {
    Spinner spn_account_type,spn_lock_mode;
    CheckBox chk_priority;
    EditText edt_target_amount,edt_date_maturity,edt_payee_account,edt_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_account_configuration);
        spn_account_type = (Spinner) findViewById(R.id.spn_account_type);
        chk_priority = (CheckBox) findViewById(R.id.chk_priority);
        spn_lock_mode = (Spinner) findViewById(R.id.spn_lock_mode);
        edt_target_amount = (EditText) findViewById(R.id.edt_target_amount);
        edt_date_maturity = (EditText) findViewById(R.id.edt_date_maturity);
        edt_payee_account = (EditText) findViewById(R.id.edt_payee_account);
        edt_ref = (EditText) findViewById(R.id.edt_ref);

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
