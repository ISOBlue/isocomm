package edu.purdue.isocomm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;
import org.isoblue.isobus.PGN;
import org.isoblue.isobus.PGN.InvalidPGNException;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

//import edu.purdue.isocomm.NMEAUtil;

public class DeviceSelectDialog extends DialogFragment {
	public CharSequence[] items = null;
	public Context mContext;
	public Handler postman;
	private ArrayList<BluetoothDevice> btdevices;
	private BTAgent BTconnector;

	public void bindToRealDevices(ArrayList<BluetoothDevice> b, BTAgent m){
		btdevices = b;
		BTconnector = m;
	}
	
	public void setHandler(Handler h){
		postman = h;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    
	    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View modifview = inflater.inflate(R.layout.choose_device_title,null);
	    builder.setCustomTitle(modifview);
	    
	    //TODO: Use builder.setView();
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	
	        public void onClick(DialogInterface dialog, int which) {
	        	BTconnector.getIBDevice(btdevices.get(which));
	        }
	    });
	    return builder.create();
	}
}
