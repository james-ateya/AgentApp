package com.paltouch.agentapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends Activity {
    Button btn_deposit,btn_withdraw,btn_settings,btn_reports,btn_info,btn_customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_deposit = (Button) findViewById(R.id.btn_deposit);
        btn_deposit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent customerdeposits = new Intent(MainActivity.this, CustomerDeposits.class);
                    startActivity(customerdeposits);
                    MainActivity.this.finish();
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
                Intent iscustomer_reg = new Intent(MainActivity.this, Customer_Registration.class);
                startActivity(iscustomer_reg);
                MainActivity.this.finish();
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
}