/* 
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

package edu.purdue.isocomm;

import java.io.IOException;
import java.io.Serializable;
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
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
			Log.i("BTAG","Bluetooth is not supported.");
			ConnectorStatus = STATUS_NO_BT;
		}else{
			if (!mBluetoothAdapter.isEnabled()) {
		 	    ConnectorStatus = STATUS_BT_OFF;
		    }else{ 
		    	Log.i("BTAG","Bluetooth is ON. We are good to go.");
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
		
		Log.i("BTAG","Listing paired devices..");
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		//If there are paired devices
		if (pairedDevices.size() > 0) {
			Log.i("BTAG","Turns out there are!");
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		    	Log.i("BTAG",device.getName());
		    	j.add(device);
		    }
		}
	}
	
	public boolean getIBDevice(final BluetoothDevice mdev){
//		ISOBlueDevice ibd = null;
				
		Thread KONNECT = new Thread() {
		    public void run() {
		    	try {
		    		
			    		mHandler.obtainMessage(Map.SHOW_PROGRESSBOX,
			    				-1, -1, "Connecting please wait..").sendToTarget();
	
					    //Creating the ISOBlueDevice initiates the connection with the ISOBlue
			    		ibdevice = new ISOBlueDevice(mdev);
			    		//all the sockets we want to send to handler
		        		ArrayList<ISOBUSSocket> sockets = new  ArrayList<ISOBUSSocket>();
						ISOBUSSocket impSocket, impBufSocket, engSocket, engBufSocket;

			    		//Prepare PGNs for filtering
			        	Set<PGN> pgns = new HashSet<PGN>();
			        	try {
			        	    pgns.add(new PGN(129029));
			        	    pgns.add(new PGN(65488));
			        	} catch(InvalidPGNException e) {
			        		Log.i("ISOBLUE","Error while adding PGN to filter");
			        	}
			        	
			        	Serializable messageId = 0;
			        	
			        	//Assign Regular Sockets (realtime sockets)
						impSocket = new ISOBUSSocket(ibdevice.getImplementBus(), null, pgns);
						engSocket = new ISOBUSSocket(ibdevice.getEngineBus(), null, pgns);

						//Note: Possible bug in ISOBlue Library: only works after sending small packet of data first
						ISOBUSSocket[] bufSockets = ibdevice.createBufferedISOBUSSockets(messageId); 
						impBufSocket = bufSockets[1];
						engBufSocket = bufSockets[0];
						
						sockets.add(impSocket);
						sockets.add(engSocket);
						sockets.add(impBufSocket);
						sockets.add(engBufSocket);

						//Dispatch sockets to Map Activity
						mHandler.obtainMessage(Map.BEGIN_COMMUNICATE,
								-1, -1, sockets).sendToTarget();
						//Notify UI
						mHandler.obtainMessage(Map.SHOW_TOAST,
	         					-1, -1, "Connection Estabilished").sendToTarget();


				} catch(IOException e) {
					Log.i("ISOBLUE","IO Exception - No Service");
					mHandler.obtainMessage(Map.SHOW_TOAST,
         					-1, -1, "Cannot connect to selected ISOBLUE device").sendToTarget();
					
		     	    ibdevice = null;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		    	
		    }  
		};
  	   	
		KONNECT.start();
		
		return true;
	}

}
