package edu.purdue.isocomm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Map extends Activity {
	private GoogleMap mMap;
	private HashMap<String, LatLng> places;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		places = new HashMap<String, LatLng>();
		places.put("birck",new LatLng(40.42262549999998, -86.92454150000002));
		places.put("ee",new LatLng(40.428903899999995, -86.91123760000002));
		
		initMap();
		
	}
	
	
	View.OnClickListener HandleBluetoothDeviceSelection(){
    	return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
            }
			
			};
	}
	
	private void initMap(){
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		labelStuff();
	}
	
	//Handles incoming GPS coordinates from BBB sent over bluetooth
	public void markPlace(LatLng point, String lbl){
		Log.i("isoblue","hello");
		mMap.addMarker(new MarkerOptions()
        .position(point)
        .title(lbl));
	}
	
	public void labelStuff(){
		markPlace(places.get("birck"),"Hello Yield!");
		markPlace(places.get("ee"),"Google is evil!");
		
		mMap.addPolygon(new PolygonOptions()
        .add(new LatLng(40.42262549999998, -86.92454150000002), 
        		new LatLng(41,-88), 
        		new LatLng(39,-86), 
        		new LatLng(42,-84))
        .strokeWidth(1.00f));
	}
	
	private void Handle_SearchDevice(){
		//final ProgressDialog msg_discovering = ProgressDialog.show(Map.this, "","Discovering...", true, true);
		final DeviceSelectDialog devListbox = new DeviceSelectDialog();
		
		//foundDevices will contain list of devices found given by the BTAgent
		ArrayList<String> foundDevices = new ArrayList<String>();
		//populate foundDevices with (fake) device names {placeholder}
		foundDevices.add("Pat's iPhone");
		foundDevices.add("Aaron's Macbook Pro");
		foundDevices.add("Decarlo's Macbook Pro");
		foundDevices.add("Joseph's Spaceship");

		//Assign it to devListBox so that it can populate its list
		devListbox.items = foundDevices.toArray(new CharSequence[foundDevices.size()]);
		
		//Android requires that all UI activities do not hang up the app's main thread
		//so it must be processed on the separate UI thread
		//TODO: this could get a bit messy overtime, there may be more elegant way to accomplish this.
		runOnUiThread(new Runnable() {
            public void run() {
            	//msg_discovering.show();
            	devListbox.show(getFragmentManager(), "btdev");
            }
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.map_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("ISOCOMM","selected item");
	    	switch(item.getItemId()){
	    		case R.id.action_search:
	    			Handle_SearchDevice();
	    		break;
	    	}
	    	return true;
	    }

}
