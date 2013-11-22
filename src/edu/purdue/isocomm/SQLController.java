package edu.purdue.isocomm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLController extends SQLiteOpenHelper {
	public SQLController(Context ac) {
		  super(ac, "isocomm.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		 String query;
		 query = "CREATE TABLE isodata ( id INTEGER PRIMARY KEY, PGN TEXT, data TEXT, timestamp LONG)";
		 db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	public void saveMessage(org.isoblue.isobus.Message msg) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put("data", msg.getData().toString());
		values.put("PGN", msg.getPgn().toString());
		values.put("timestamp", msg.getTimeStamp());

		database.insert("isodata", null, values);
		database.close();
	}
	
	
	
}
