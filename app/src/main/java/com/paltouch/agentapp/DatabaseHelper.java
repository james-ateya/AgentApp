package com.paltouch.agentapp;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

	Context mcontext;
	SQLiteDatabase mdb;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION_ID);
		mcontext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		mdb = db;
		try {
			db.execSQL(CREATE_TABLE_TRANSACTIONS);
			db.execSQL(CREATE_TABLE_AGENT_REPORT);
			Toast.makeText(mcontext, "tables created", Toast.LENGTH_SHORT)
					.show();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// // TODO Auto-generated catch block

	}
	// DATABASE DETAILS
	public static final String DATABASE_NAME = "lipasasadb";
	public static final String TRANSACTION_TABLE_NAME = "transactions";
	public static final String AGENT_REPORT_TABLE_NAME = "agent_report";
	public static final int VERSION_ID = 1;

	//transactions table
	public static final String TRANSACTION_ID = "_id";
	public static final String OTHER_REF = "AccountRefNo";
	public static final String AMOUNT_IN = "AmountIn";
	public static final String AMOUNT_OUT = "AmountOut";
	public static final String APPROVAL_DATE = "ApprovalDate";
	public static final String BALANCE = "Balance";
	public static final String RECEIPT_NO = "ReceiptNumber";

	//agent_report table
	public static final String TABLE_ID = "_id";
	public static final String FULL_NAME = "full_name";
	public static final String NAT_ID = "nat_id";
	public static final String ACCOUNT_NUMBER = "account_no";
	public static final String ACCOUNT_NAME = "account_name";
	public static final String DEPOSITED_AMOUNT = "deposited_amount";
	public static final String DATE = "date";
	public static final String TIME = "time";


	// CREATE TABLE STATEMENTS
	public static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE IF NOT EXISTS "
			+ TRANSACTION_TABLE_NAME
			+ " (" + TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ OTHER_REF + " TEXT NOT NULL,"
			+ AMOUNT_IN + " TEXT NOT NULL,"
			+ AMOUNT_OUT + " TEXT NOT NULL,"
			+ APPROVAL_DATE + " TEXT NOT NULL, "
			+ BALANCE + " TEXT NOT NULL,"
			+ RECEIPT_NO + " TEXT NOT NULL)";

	//CREATE TABLE AGENT STATEMENT
	public static final String CREATE_TABLE_AGENT_REPORT = "CREATE TABLE IF NOT EXISTS "
			+ AGENT_REPORT_TABLE_NAME
			+ " (" + TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ FULL_NAME + " TEXT NOT NULL,"
			+ NAT_ID + " TEXT NOT NULL,"
			+ ACCOUNT_NUMBER + " TEXT NOT NULL,"
			+ ACCOUNT_NAME + " TEXT NOT NULL, "
			+ DEPOSITED_AMOUNT + " TEXT NOT NULL,"
			+ DATE + " TEXT NOT NULL,"
			+ TIME + " TEXT NOT NULL)";
}
