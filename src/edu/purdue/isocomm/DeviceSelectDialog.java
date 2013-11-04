package edu.purdue.isocomm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;
import org.isoblue.isobus.PGN;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import edu.purdue.isocomm.NMEAUtil;

public class DeviceSelectDialog extends DialogFragment {
	public CharSequence[] items = null;
	public Context mContext;
	private ArrayList<BluetoothDevice> btdevices;
	private BTAgent BTconnector;
	
	public void bindToRealDevices(ArrayList<BluetoothDevice> b, BTAgent m){
		btdevices = b;
		BTconnector = m;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    
	    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View modifview = inflater.inflate(R.layout.choose_device_title,null);
	    builder.setCustomTitle(modifview);
	    
//	    TODO: Use builder.setView();
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	
	               public void onClick(DialogInterface dialog, int which) {
	            	   // The 'which' argument contains the index position
	            	   
	            	   //obtain ISOBLUE DEVICE, ibd
	            	   ISOBlueDevice ibd = BTconnector.getIBDevice(btdevices.get(which));
	            	   NMEAUtil parser = new NMEAUtil(mContext, null);
	            	   ISOBUSSocket impSocket = null;
	            	   
						try {
							impSocket = new ISOBUSSocket(ibd.getImplementBus(), null, null);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				
					   parser.processMessages(impSocket);

	               }
	    });
	    return builder.create();
	}
}
