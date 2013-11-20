package edu.purdue.isocomm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.isoblue.isoblue.ISOBlueDevice;
import org.isoblue.isobus.ISOBUSSocket;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.R.menu;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class Map extends Activity {
	private GoogleMap mMap;
	public HashMap<String, LatLng> places;
	public static final int CONNECTION_SUCCESS = 1;
	public static final int BEGIN_COMMUNICATE = 2;
	public Menu myMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		places = new HashMap<String, LatLng>();
		places.put("birck",new LatLng(40.42262549999998, -86.92454150000002));
		places.put("ee",new LatLng(40.428903899999995, -86.91123760000002));
		
		initMap();
		
	}
	

	//Postman deliver messages from DeviceSelectDialog to Map activity
	@SuppressLint("HandlerLeak")
	private final Handler postman = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				
				case BEGIN_COMMUNICATE:
					myMenu.getItem(2).setTitle("Live");
					myMenu.getItem(2).setIcon(R.drawable.gdot2);
					myMenu.getItem(2).setEnabled(false);		
					
					Log.i("ISOBLUE","postman: I will now do stuff");
					
					@SuppressWarnings("unchecked")
					ArrayList<ISOBUSSocket> socks = (ArrayList<ISOBUSSocket>)msg.obj;
					final ISOBUSSocket imsock = socks.get(0);
//					final ISOBUSSocket ensock = socks.get(1);
						
					Thread ATAX = new Thread(){
							public void run(){
								org.isoblue.isobus.Message message = null;
								org.isoblue.isobus.Message message2 = null;

								int msg_count = 0;
								
								while(msg_count < 1000){
									try {
										message = imsock.read();
										
//										if (message.getPgn().toString().equals("129029") || message.getPgn().toString().equals("65488")){
//											Log.i("postman", message.toString());
//										}
										Log.i("IMPLEMENT", message.toString());
//										Log.i("ENGINE", message2.toString());

										//BBB <--- https://dl.dropboxusercontent.com/u/41564792/data.zip
										
										msg_count++;
										
										final int coord_adder = msg_count;
										final org.isoblue.isobus.Message bfr = message;
										
										runOnUiThread(new Runnable() {
								            public void run() {
//								            	markPlace(new LatLng(Math.random() + 40.12262549999998, Math.random() + -89.92454150000002),
//								            			bfr.getData().toString());
								            }
								        });
										
										
										
										//slow it down, demo only
//										Thread.sleep(500);
									} catch (InterruptedException e) {
										//Interuption not thrown!!
										Log.i("postman","Unable to read from BT");
										e.printStackTrace();
									}
									
									
								}
							}
						};
						
					ATAX.start();
				break;
			}
		}
	};
	
	
	
	private void initMap(){
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); //init map
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(places.get("ee"), 5.00f)); //set default camera zoom and location
		labelStuff();
		
	}
	
	//Handles incoming GPS coordinates from BBB sent over bluetooth
	public void markPlace(LatLng point, String lbl){
		mMap.addMarker(new MarkerOptions()
        .position(point)
        .title(lbl));
	}
	
	//TODO: Remove this and make a generic Class to handle Map operations (extend GoogleMap)
	public void labelStuff(){
		markPlace(places.get("birck"),"Hello Yield!");
		
		
		mMap.addPolygon(new PolygonOptions()
        .add(new LatLng(40.42262549999998, -86.92454150000002), 
        		new LatLng(41,-88), 
        		new LatLng(39,-86), 
        		new LatLng(42,-84))
        .strokeWidth(1.00f));
	}
	
	private void Handle_SimulateStuff(){
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		mMap.addPolygon(new PolygonOptions()
        .add(new LatLng(41.5016, -86.19123),  // 41� 5.016', -86� 19.123'
        		new LatLng(41.5055,-86.19123),  //41� 5.055', -86� 16.805'
        		new LatLng(41.3296,-86.846), //41� 3.285', -86� 16.846'
        		new LatLng(41.3296,-86.18747)) //41� 3.296', -86� 18.747'

        .strokeWidth(3.00f)
        .strokeColor(Color.RED));
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.5016, -86.19123), mMap.getMaxZoomLevel() - 10.00f)); //set default camera zoom and location
		
	}
	
	private void Handle_SearchDevice(){
		
		final DeviceSelectDialog devListbox = new DeviceSelectDialog();
		
		//foundDevices will contain list of devices found given by the BTAgent
		ArrayList<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
		
		//Our bluetooth connector goodies stuff and etc
		BTAgent connector = new BTAgent(Map.this, null);
		
		//Populate with PairedDevices
		connector.populateWithPairedDevices(foundDevices);
		
		//Stringify the devices
		String[] foundDevices_itemDisplayText = new String[foundDevices.size()];
		for(int i=0;i< foundDevices.size();i++){
			BluetoothDevice fd = foundDevices.get(i);
			foundDevices_itemDisplayText[i] = fd.getName() + " - " + fd.getAddress();
		}
		

		if(connector.ConnectorStatus == BTAgent.STATUS_BT_ON){
			//Assign it to devListBox so that it can populate its list
			devListbox.items = foundDevices_itemDisplayText;
			devListbox.bindToRealDevices(foundDevices, connector);
			devListbox.mContext = Map.this;
			devListbox.setHandler(postman);
			
			//Android requires that all UI activities do not hang up the app's main thread
			//so it must be processed on the separate UI thread
			//TODO: this could get a bit messy overtime, there may be more elegant way to accomplish this.
			
			runOnUiThread(new Runnable() {
	            public void run() {
	            	devListbox.show(getFragmentManager(), "btdev"); 
	            }
	        });
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.map_actions, menu);
		myMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("ISOCOMM","selected actionbar item");
	    	switch(item.getItemId()){
	    		case R.id.action_search:
	    			Handle_SearchDevice();
	    		break;
	    		case R.id.action_sim:
	    			Handle_SimulateStuff();
	    		break;
	    	}
	    	return true;
	    }

}