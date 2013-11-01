package edu.purdue.isocomm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.PGN;

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
	private final Handler mHandler;
	private final Context mContext;
	public ISOBlueDevice ibdevice;

	public BTAgent(Context context, Handler handler) {
		mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		if (mBluetoothAdapter == null) {
			// Add Toast Message here
			Log.i("ISOCOMM","Bluetooth is not on.");
		}else{
			Log.i("ISOCOMM","Bluetooth is ON. We are good to go.");
		}

	}
	
	public void populateWithPairedDevices(ArrayList<BluetoothDevice> j){
		Log.i("ISOCOMM","Listing paired devices..");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//		// If there are paired devices
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
	
	public ISOBlueDevice getIBDevice(BluetoothDevice mdev){
		ISOBlueDevice ibd = null;
//        final ProgressDialog msg_discovering = ProgressDialog.show(mContext, "","Connecting", true, true);
//        msg_discovering.show();
        
		try {
		    // Creating the ISOBlueDevice initiates the connection with the ISOBlue
		    ibd = new ISOBlueDevice(mdev);
		    ibdevice = ibd;
			Toast toast = Toast.makeText(mContext, "Connected to " + ibd.getDevice().getName(),Toast.LENGTH_SHORT);
	 	    toast.show();
	 	    
		} catch(IOException e) {
		    // The BluetoothDevice could not be connected to, or it is not an ISOBlue
			Toast toast = Toast.makeText(mContext, "Device offline or is not an ISOBLUE device",Toast.LENGTH_SHORT);
     	    toast.show();
		}
		
		return ibd;
	}
	
	
}
