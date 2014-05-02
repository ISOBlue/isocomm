/* Note: Not Intended for use in this feature-limited release (see other branch of this repository)
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
        final View modifview = inflater.inflate(R.layout.choose_device_title,null);
        TextView titleX = (TextView)modifview.findViewById(R.id.textView1);
        titleX.setText("Legends");
	    builder.setCustomTitle(modifview);
	    
	    //TODO: Use builder.setView();
	    builder.setItems(items, new DialogInterface.OnClickListener() {
	    	
	        public void onClick(DialogInterface dialog, int which) {
	        	 Log.i("Legend Dialog","Not Ready");
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
