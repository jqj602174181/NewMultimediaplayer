package com.centerm.mediaplayer.three.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1; 

	private static final String DB_NAME = "centerm_media_db.db";

	private static final String TAG = MySQLHelper.class.getSimpleName();
	private static MySQLHelper mSqlHelper = null;
	private static final String CREATETABLE_1 = "create table if not exists media_title_table(id integer primary key, title varchar, indexs varchar)";
	private static final String CREATETABLE_2 = "create table if not exists media_file_table(id integer primary key, file varchar, indexs varchar)";
	
	private MySQLHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	private MySQLHelper(Context context, String name, int version){  
		this(context, name, null, version);
	}  

	private MySQLHelper(Context context, String name){  
		this(context, name, VERSION);  
	}  

	public static MySQLHelper getInstance(Context context){
		if(mSqlHelper == null){
			mSqlHelper = new MySQLHelper(context, DB_NAME);
		}
		return mSqlHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "create a database");  
		//execSQL”√”⁄÷¥––SQL”Ôæ‰  
		db.execSQL(CREATETABLE_1); 
		db.execSQL(CREATETABLE_2); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "upgrade a database");  
	}
}
