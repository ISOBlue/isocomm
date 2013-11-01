package edu.purdue.isocomm;

import java.util.ArrayList;

import org.isoblue.isoblue.ISOBlueDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

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
	    builder.setTitle(R.string.pick_device);
	    
	    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View modifview = inflater.inflate(R.layout.choose_device_title,null);
	    builder.setCustomTitle(modifview);
	    
//	    TODO: Use builder.setView();
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	
	               public void onClick(DialogInterface dialog, int which) {
	            	   // The 'which' argument contains the index position
//

	            	   //obtain ISOBLUE DEVICE
	            	   //it is also set in the BTconnector itself
	            	   ISOBlueDevice ibd = BTconnector.getIBDevice(btdevices.get(which));
	            	   
	               }
	    });
	    return builder.create();
	}
}
