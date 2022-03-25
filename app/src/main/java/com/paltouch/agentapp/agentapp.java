package com.paltouch.agentapp;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class agentapp extends Application {
    DatabaseHelper dbhelper;
    SQLiteDatabase db;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        dbhelper = new DatabaseHelper(getApplicationContext());
        db = dbhelper.getWritableDatabase();
    }
}

