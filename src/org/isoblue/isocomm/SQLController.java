/*
 * Note: Feature removed (see past commit somewhere)
 * 
 * Author: Pat Sabpisal <ssabpisa@purdue.edu>
 *
 * Copyright (C) 2014 Purdue University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package org.isoblue.isocomm;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLController extends SQLiteOpenHelper {
	private static int COL_ID = 0;
	private static int COL_PGN = 1;
	private static int COL_DATA = 2;
	private static int COL_TIMESTAMP = 3;

	
	public SQLController(Context ac) {
		  super(ac, "isochives24.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		 String query;
		 query = "CREATE TABLE isodata (id INTEGER PRIMARY KEY, PGN TEXT, data BLOB, timestamp LONG)"; //PGN should be int!
		 db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	public void saveMessage(org.isoblue.isobus.Message msg) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		values.put("data", msg.getData());
		values.put("PGN", msg.getPgn().toString());
		values.put("timestamp", msg.getTimeStamp());

		database.insert("isodata", null, values);
		database.close();
	}
	
	public ArrayList<IRecord> allMessages(){
		ArrayList<IRecord> recs = new ArrayList<IRecord>();
		try
        {
			SQLiteDatabase database = this.getReadableDatabase();
			Cursor d = database.rawQuery("SELECT * FROM isodata",null);
			d.moveToFirst();
			while (d.moveToNext()) {
				//Log.i("isocommdb",d.getString(COL_PGN) + ", [TS: " + d.getString(COL_TIMESTAMP) + "], DATA: " + d.getBlob(COL_DATA) );
				recs.add( new IRecord(d.getInt(COL_ID),d.getString(COL_PGN), d.getLong(COL_TIMESTAMP), d.getBlob(COL_DATA)));
			}
			
			d.close();
			database.close();
        }
        catch(Exception e)
        {
            Log.i("isocommdb","Exception: " + e);
        }
		return recs;
	}
	
}
