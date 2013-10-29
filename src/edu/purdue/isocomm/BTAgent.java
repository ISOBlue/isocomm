package edu.purdue.isocomm;

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
import android.app.Activity;
import android.app.ListActivity;

public class BTAgent extends ListActivity {
	private final BluetoothAdapter mBluetoothAdapter;
	private final Handler mHandler;

	public BTAgent(Context context, Handler handler) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			// Add Toast Message here
			Log.e("ISOCOMM","No bluetooth on this device.");
		}
		if (!mBluetoothAdapter.isEnabled()) {
			int REQUEST_ENABLE_BT = 1;
		    Intent requestBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(requestBT, REQUEST_ENABLE_BT);
		}

	}
	
	public ArrayAdapter<String> getPairedDevices(){
		ArrayAdapter<String> devices = new ArrayAdapter<String>(null, 0);
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		    	devices.add(device.getName() + "\n" + device.getAddress());
		    }
		}
		
		return devices;

	}
	
	
}
