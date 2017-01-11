package com.centerm.mediaplayer.three.sqlite;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class SqlHelper {

	private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String DB_PATH = SDCARD_PATH + "/config/centerm_media_db.db";
	private static final String TAG = SqlHelper.class.getSimpleName();

	public static void execusSql(String sql){
		File file = new File(DB_PATH);
		if(!file.exists()){
			Log.e(TAG, ".db is not found.");
			return;
		}

		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
				DB_PATH, null);
		database.execSQL(sql);
		database.close();
	}

	public static void updateSql(String tableName, String id){
		File file = new File(DB_PATH);
		if(!file.exists()){
			Log.e(TAG, ".db is not found.");
			return;
		}

		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
				DB_PATH, null);
		database.beginTransaction();
		Cursor cursor = database.query(tableName, null, "id=?", new String[]{id}, null, null, null);
		cursor.moveToFirst();
		String click = cursor.getString(cursor.getColumnIndex("click"));
		int newValue = 0;
		if(click == null){
			newValue = 1;
		}else{
			newValue = Integer.parseInt(click)+1;
		}
		String newClick = String.valueOf(newValue);
		//update
		ContentValues values = new ContentValues();
		values.put("click", newClick);
		database.update(tableName, values, "id=?", new String[]{id});
		database.setTransactionSuccessful();
		database.endTransaction();
		database.close();
	}
}
