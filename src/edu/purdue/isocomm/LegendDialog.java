package edu.purdue.isocomm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LegendDialog extends DialogFragment {
	public Context mContext;
	public Handler postman;
	public String[] items = {"Yield Data","Moisture Density","Soil Conductivity","Combustibility"};
	
	public void setHandler(Handler h){
		postman = h;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    
	    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//	    getDialog().getWindow().setLayout(00, 500)
        final View modifview = inflater.inflate(R.layout.choose_device_title,null);
        TextView titleX = (TextView)modifview.findViewById(R.id.textView1);
        titleX.setText("Legends");
	    builder.setCustomTitle(modifview);
	    
	    //TODO: Use builder.setView();
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	
	        public void onClick(DialogInterface dialog, int which) {
	        	 Log.i("Legend Dialog","Hello World");
	        }
	    });
	    
	    builder.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.legend_listrow, R.id.legendTitle, items), 
                new DialogInterface.OnClickListener(){
            			@Override
            			public void onClick(DialogInterface dialog, int item){
            	
            			}
	    			});
	    builder.setPositiveButton("Update Map", null);
	    builder.setNegativeButton("Default", null);
	    
	    return builder.create();
	}
}
