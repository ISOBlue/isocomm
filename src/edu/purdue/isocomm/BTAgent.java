package edu.purdue.isocomm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.PGN;
import org.isoblue.isobus.PGN.InvalidPGNException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;

public class BTAgent {
	private final BluetoothAdapter mBluetoothAdapter;
	public int ConnectorStatus;
	private final Handler mHandler;
	private final Context mContext;
	public ISOBlueDevice ibdevice;
	public static int STATUS_NO_BT = 2;
	public static int STATUS_BT_OFF = 1;
	public static int STATUS_BT_ON = 0;

	public BTAgent(Context context, Handler handler) {
		mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		if (mBluetoothAdapter == null) {
			Log.i("ISOCOMM","Bluetooth is not supported.");
			ConnectorStatus = STATUS_NO_BT;
		}else{
			if (!mBluetoothAdapter.isEnabled()) {
		        //bluetooth is off
		 	    ConnectorStatus = STATUS_BT_OFF;
		    }else{ 
		    	//bluetooth is on
		    	Log.i("ISOCOMM","Bluetooth is ON. We are good to go.");
		    	ConnectorStatus = STATUS_BT_ON;
		    }		
		}

	}
	
	public void populateWithPairedDevices(ArrayList<BluetoothDevice> j){
		if(ConnectorStatus == STATUS_BT_OFF){
			Toast toast = Toast.makeText(mContext, 
					"Bluetooth is off. Please turn it on.",
					Toast.LENGTH_SHORT);
	 	    toast.show();
			return;
		}
		if(ConnectorStatus == STATUS_NO_BT){
			Toast toast = Toast.makeText(mContext, 
					"Bluetooth is not supported by your device. Please consult your manufacturer or the IRS.",
					Toast.LENGTH_SHORT);
	 	    toast.show();
			return;
		}
		
		Log.i("ISOCOMM","Listing paired devices..");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		//If there are paired devices
		if (pairedDevices.size() > 0) {
			Log.i("ISOCOMM","Turns out there are!");
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		    	Log.i("ISOCOMM",device.getName());
		    	j.add(device);
		    }
		}
	}
	
	public boolean getIBDevice(final BluetoothDevice mdev){
//		ISOBlueDevice ibd = null;
//		
		
		
		Thread KONNECT = new Thread() {
		    public void run() {
		    	try {
		    		mHandler.obtainMessage(Map.SHOW_PROGRESSBOX,
		    				-1, -1, "Connecting please wait..").sendToTarget();

				    //Creating the ISOBlueDevice initiates the connection with the ISOBlue
		    		ibdevice = new ISOBlueDevice(mdev);
		        	Set<PGN> pgns = new HashSet<PGN>();
		        	
		        	try {
		        	    pgns.add(new PGN(129029));
		        	    pgns.add(new PGN(65488));
		        	} catch(InvalidPGNException e) {
		        		Log.i("ISOBLUE","Error while adding PGN to filter");
		        	}
		        	
		        	try {
						ISOBUSSocket impSocket = new ISOBUSSocket(ibdevice.getImplementBus(), null, pgns);
						ISOBUSSocket engSocket = new ISOBUSSocket(ibdevice.getEngineBus(), null, pgns);
						
						ArrayList<ISOBUSSocket> sockets = new  ArrayList<ISOBUSSocket>();
						sockets.add(impSocket);
						sockets.add(engSocket);
						
						//Dispatch messages to Map 
						mHandler.obtainMessage(Map.BEGIN_COMMUNICATE,
								-1, -1, sockets).sendToTarget();
					
						
						mHandler.obtainMessage(Map.SHOW_TOAST,
	         					-1, -1, "Connection Estabilished").sendToTarget();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    
				} catch(IOException e) {
					Log.i("ISOBLUE","UNABLE CONNECT - SERVICE NOT FOUND");
					mHandler.obtainMessage(Map.SHOW_TOAST,
         					-1, -1, "Cannot connect to selected ISOBLUE device").sendToTarget();
					
		     	    ibdevice = null;
				}
		    	
		    }  
		};
  	   	
		

		KONNECT.start();
		
		
		return true;
	}
	
	public void beginHandshake(){
		
	
	}
}
