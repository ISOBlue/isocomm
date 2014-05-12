/*
 * Fast packet GNSS Decoder
 *
 *
 * Author: Joseph Watkins <jlwatkins@purdue.edu>
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.R.menu;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DataGrabber {

	//You need to pass it all 7 messages from the BBB
	//The PGN for these values must be 129029, and NOT 129025 as it is not yet supported
	//As of right now only latitude and longitude are included. 
	// not sure how we want save teh final latitude and longitude
	public LatLng GNSSData(org.isoblue.isobus.Message[] msgs)
	{
		long latitude = 0, longitude = 0; //variables to hold the values from the data block
		
		//data blocks extracted from the messages
		byte[] dataBlock1 = msgs[0].getData();
		byte[] dataBlock2 = msgs[1].getData();
		byte[] dataBlock3 = msgs[2].getData();
		byte[] dataBlock4 = msgs[3].getData();
		byte[] dataBlock5 = msgs[4].getData();
		byte[] dataBlock6 = msgs[5].getData();
		byte[] dataBlock7 = msgs[6].getData();
		
		//rebuilds the latitude from data blocks extracted from pgn 129029's 7 messages

		byte[] latData = new byte[] {dataBlock3[2], dataBlock3[1], dataBlock2[7], dataBlock2[6], dataBlock2[5], dataBlock2[4], dataBlock2[3],dataBlock2[2]};
	    ByteBuffer latBuffer = ByteBuffer.wrap(latData);
	    
	   
		double lat = latBuffer.getLong()*0.0000000000000001f;
//	    Log.i("test2", lat + " LAT");	    
//	    
//		latitude = dataBlock2[2];
//		latitude = latitude | (dataBlock2[3] << 8);
//		latitude = latitude | (dataBlock2[4] << 16);
//		latitude = latitude | (dataBlock2[5] << 24);
//		latitude = latitude | (dataBlock2[6] << 32);
//		latitude = latitude | (dataBlock2[7] << 40);
//		latitude = latitude | (dataBlock3[1] << 48);
//		latitude = latitude | (dataBlock3[2] << 56);
		
//	    
//		//rebuilds the longitude very much like the latitiude
		byte[] longData = new byte[] {dataBlock4[3], dataBlock4[2], dataBlock4[1], dataBlock3[7], dataBlock3[6], dataBlock3[5], dataBlock3[4], dataBlock3[3]};
	    ByteBuffer longBuffer = ByteBuffer.wrap(longData);
//	    longBuffer.order(ByteOrder.BIG_ENDIAN);
	    
//	    int f = 0;
//	    for(f = 0; f < longData.length; f++){
//	    	Log.i("test",String.format("0x%20x", longData[f]));
//	    }
	    
	    
	    double longg = longBuffer.getLong()*0.0000000000000001f;
//	    Log.i("test2", longg  + " LONG");	    
	    
////		longitude = dataBlock3[3];
////		longitude = longitude | (dataBlock3[4] << 8);
////		longitude = longitude | (dataBlock3[5] << 16);
////		longitude = longitude | (dataBlock3[6] << 24);
////		longitude = longitude | (dataBlock3[7] << 32);
////		longitude = longitude | (dataBlock4[2] << 40);
////		longitude = longitude | (dataBlock4[3] << 48);
////		longitude = longitude | (dataBlock4[4] << 56);
//		
//		//Convert latitude and longitude to double form
//		double final_longitude = longBuffer.getLong() * 0.0000000000000001f;
//		double final_latitude = latBuffer.getLong() * 0.0000000000000001;
//		
		LatLng point = new LatLng(lat, longg);
		return point;
	}

	//Given PGN 65488 message block (it is only one message)
	//Will return the yield data stored in units bushels/sec
	public double yieldData(org.isoblue.isobus.Message msg)
	{
		
		byte[] yieldData = msg.getData();
		byte[] yield = new byte[] {yieldData[3],yieldData[2],yieldData[1],yieldData[0]};
	    ByteBuffer yieldBuffer = ByteBuffer.wrap(yield);
	    
//		byte[] yields = new byte[] {msg.getData()[1], msg.getData()[0]};
//	    ByteBuffer yieldBuffer = ByteBuffer.wrap(yields);
//	    yieldBuffer.order(ByteOrder.LITTLE_ENDIAN);
//	    double y = yieldBuffer.getLong() * .0000189545096358038f;
	    
	    double y = yieldBuffer.getInt() * .0000189545096358038f;
	    return y;
	    
		//YIELD PGN 65488
		//FIRST TWO BYTES IN DATA FIELD ARE YIELD DATA
		//multiply by 1.89545096358038e-5	bushels / sec

	}
	
	//new function to save pgns

	//if PGN == 129029
	//collect or store all 7 messages of the packet or pass them to the next function
	//grab latitude and longitude from the data packets from the messages
	//pass the time stamp as well to the data
	//store the latitude, longitude, and timestamp to a file of some sort
	//plot the data from this file using markers or tiles. 

	//store lat, long, and timestamp

	//plot the data

	
}
/*
public class PGNPosData {
	
	public byte SID; //SID
	public int date; //days since january 1, 1970
	public int time; //second since midnight
	public long latitude; //latitude
	public long longitude; //longitude
	public long altitude; //altitude
	public byte GNSStypeAndMethod; //GNSS type and GNSS Method
	public byte integrityAndReserved; //Integreity and Reserved Variables
	public byte numOfSVs; //number of satellites used
	public int HDOP; //horizontal dilution of precision
	public int PDOP; //probable dilution of precision
	public int GeoidalSep; //Geoidal Seperation
	public int ref; //Reference Stations number, type, and ID
	public int age; //age of DGNSS Corrections
	
	/* See Document for alex for more on these values above */	
//}
