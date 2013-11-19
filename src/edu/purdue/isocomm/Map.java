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
	
	public class PGNPosData {
		
		public byte SID; //SID
		public int date; //days since january 1, 1970
		public int time; //second since midnight
		public long latitude; //latitude
		public long longitude; //longitude
		public long altitude; //altitude
		public byte GNSStypeAndMethod; //GNSS type and GNSS Method
		public byte integrityAndReserved; //Integreity and Reserved Variables
		public byte numOfSVs; //number of satellites used
		public int HDOP; //horizontal dilution of precision
		public int PDOP; //probable dilution of precision
		public int GeoidalSep; //Geoidal Seperation
		public int ref; //Reference Stations number, type, and ID
		public int age; //age of DGNSS Corrections
		
		/* See Document for alex for more on these values above */		
	}
	
	
	
	
	//Postman deliver messages from DeviceSelectDialog to Map activity
	@SuppressLint("HandlerLeak")
	private final Handler postman = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				
				case BEGIN_COMMUNICATE:
					myMenu.getItem(1).setTitle("Live");
					myMenu.getItem(1).setIcon(R.drawable.gdot2);
					myMenu.getItem(1).setEnabled(false);		
					
					Log.i("ISOBLUE","postman: I will now do stuff");
					
					final ISOBUSSocket imsock = (ISOBUSSocket)msg.obj;
						
					Thread ATAX = new Thread(){
							public void run(){
								org.isoblue.isobus.Message message = null;
								int msg_count = 0;
								
								while(msg_count < 1000){
									try {
										message = imsock.read();

										//BBB <--- https://dl.dropboxusercontent.com/u/41564792/data.zip
										
										msg_count++;
										
										final int coord_adder = msg_count;
										final org.isoblue.isobus.Message bfr = message;
										
										runOnUiThread(new Runnable() {
								            public void run() {
								            	markPlace(new LatLng(Math.random() + 40.12262549999998, Math.random() + -89.92454150000002),
								            			bfr.getData().toString());
								            }
								        });
										
										//slow it down, demo only
										Thread.sleep(500);
									} catch (InterruptedException e) {
										//Interuption not thrown!!
										Log.i("postman","Unable to read from BT");
										e.printStackTrace();
									}
									
									Log.i("postman", message.toString());
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
	
	@Override
	//You need to pass it all 7 messages from the BBB
	//The PGN for these values must be 129029, and NOT 129025 as it is not yet supported
	//As of right now only latitude and longitude are included. 
	// not sure how we want save teh final latitude and longitude
	public boolean GNSSData(Message[] msgs, PGNPosData )
	{
		long latitude = 0, longitude = 0; //variables to hold the values from the data block
		
		//rebuilds the latitude from data block from pgn 129029
		latitude = msgs[1].data[2];
		latitude = latitude | ((int)msgs[1].data[3] << 8);
		latitude = latitude | ((int)msgs[1].data[4] << 16);
		latitude = latitude | ((int)msgs[1].data[5] << 24);
		latitude = latitude | ((int)msgs[1].data[6] << 32);
		latitude = latitude | ((int)msgs[1].data[7] << 40);
		latitude = latitude | ((int)msgs[2].data[1] << 48);
		latitude = latitude | ((int)msgs[2].data[2] << 56);
		
		//rebuilds the longitude from data block from pgn 129029
		longitude = msgs[2].data[3];
		longitude = longitude | ((int)msgs[2].data[4] << 8);
		longitude = longitude | ((int)msgs[2].data[5] << 16);
		longitude = longitude | ((int)msgs[2].data[6] << 24);
		longitude = longitude | ((int)msgs[2].data[7] << 32);
		longitude = longitude | ((int)msgs[3].data[2] << 40);
		longitude = longitude | ((int)msgs[3].data[3] << 48);
		longitude = longitude | ((int)msgs[3].data[4] << 56);
		
		//Convert latitude and longitude to double form
		double final_longitude = longitude * 0.0000000000000001;
		double final_latitude = latitude * 0.0000000000000001;
		
		
		//Should save the longitude and latitude in a file, along with a time stamp to help with replaying.
		
		//Also should pass the values somehow to be used with google maps. 
		
		return true;
	}

	//Given PGN 65488 message block (it is only one message)
	//Will return the yield data stored in units bushels/sec
	public double yieldData(Message msg)
	{
		int yield = 0;
		yield = msg.data[0];
		yield = yield | msg.data[1] << 8;

		return (double) (yield * .0000189545096358038);

		//YIELD PGN 65488
		//FIRST TWO BYTES IN DATA FIELD ARE YIELD DATA
		//multiply by 1.89545096358038e-5	bushels / sec

	}
	
	//new function to save pgns

	//if PGN == 129029
	//collect or store all 7 messages of the packet or pass them to the next function
	//grab latitude and longitude from the data packets from the messages
	//pass the time stamp as well to the data
	//store the latitude, longitude, and timestamp to a file of some sort
	//plot the data from this file using markers or tiles. 

	//store lat, long, and timestamp

	//plot the data

}
