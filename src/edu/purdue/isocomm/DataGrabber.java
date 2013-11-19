package edu.purdue.isocomm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	@Override
	//You need to pass it all 7 messages from the BBB
	//The PGN for these values must be 129029, and NOT 129025 as it is not yet supported
	//As of right now only latitude and longitude are included. 
	// not sure how we want save teh final latitude and longitude
	public boolean GNSSData(Message[] msgs, PGNPosData )
	{
		long latitude = 0, longitude = 0; //variables to hold the values from the data block
		
		//rebuilds the latitude from data block from pgn 129029
		latitude = msgs[1].data[2];
		latitude = latitude | ((int)msgs[1].data[3] << 8);
		latitude = latitude | ((int)msgs[1].data[4] << 16);
		latitude = latitude | ((int)msgs[1].data[5] << 24);
		latitude = latitude | ((int)msgs[1].data[6] << 32);
		latitude = latitude | ((int)msgs[1].data[7] << 40);
		latitude = latitude | ((int)msgs[2].data[1] << 48);
		latitude = latitude | ((int)msgs[2].data[2] << 56);
		
		//rebuilds the longitude from data block from pgn 129029
		longitude = msgs[2].data[3];
		longitude = longitude | ((int)msgs[2].data[4] << 8);
		longitude = longitude | ((int)msgs[2].data[5] << 16);
		longitude = longitude | ((int)msgs[2].data[6] << 24);
		longitude = longitude | ((int)msgs[2].data[7] << 32);
		longitude = longitude | ((int)msgs[3].data[2] << 40);
		longitude = longitude | ((int)msgs[3].data[3] << 48);
		longitude = longitude | ((int)msgs[3].data[4] << 56);
		
		//Convert latitude and longitude to double form
		double final_longitude = longitude * 0.0000000000000001;
		double final_latitude = latitude * 0.0000000000000001;
		
		
		//Should save the longitude and latitude in a file, along with a time stamp to help with replaying.
		
		//Also should pass the values somehow to be used with google maps. 
		
		return true;
	}

	//Given PGN 65488 message block (it is only one message)
	//Will return the yield data stored in units bushels/sec
	public double yieldData(Message msg)
	{
		int yield = 0;
		yield = msg.data[0];
		yield = yield | msg.data[1] << 8;

		return (double) (yield * .0000189545096358038);

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
