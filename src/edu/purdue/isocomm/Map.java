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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.R.menu;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import edu.purdue.isocomm.DataGrabber;


public class Map extends Activity {
	private GoogleMap mMap;
	public HashMap<String, LatLng> places;
	public static final int CONNECTION_SUCCESS = 1;
	public static final int BEGIN_COMMUNICATE = 2;
	public static final int SHOW_PROGRESSBOX = 3;
	public static final int SHOW_TOAST = 4;
	public Menu myMenu;
	public Polyline linePath; 
	public ArrayList<LatLng> gplist;  //gplist contains all the GPS used to draw path
	public ArrayList<Marker> yieldMarkerList;
	private SQLController dbcon;
	private ProgressDialog activeDialog;

	public static float YIELD_HIGH = 0.26f;
	public static float YIELD_MEDIUM = 0.24f;
	public static float YIELD_LOW = 0.20f;
	
	public ArrayList<org.isoblue.isobus.Message> gpsbuffer; //gpsbuffer contains all the GPS messages to be decoded
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#99000000")));

		setContentView(R.layout.activity_map);
		

		places = new HashMap<String, LatLng>();
		
		//bunch of places we can use as reference
		places.put("birck",new LatLng(40.42262549999998, -86.92454150000002));
		places.put("ee",new LatLng(40.428903899999995, -86.91123760000002));
		
		initMap();
		dbcon = new SQLController(Map.this);
		//buffer the GPS coordinates for manipulation
		gpsbuffer = new ArrayList<org.isoblue.isobus.Message>();

		
	}
	

	//Postman deliver messages from DeviceSelectDialog to Map activity
	@SuppressLint("HandlerLeak")
	private final Handler postman = new Handler() {
		@Override
		public void handleMessage(Message msg) {
						
			switch (msg.what) {
				case SHOW_TOAST:
					
					if(activeDialog != null)
					{
						activeDialog.cancel();
						activeDialog = null;
					}
					
					Toast toast = Toast.makeText(Map.this, msg.obj.toString(),Toast.LENGTH_SHORT);
			 	    toast.show();
					break;
					
				case SHOW_PROGRESSBOX:
	
					
					final ProgressDialog syncingbox = ProgressDialog.show(Map.this, "",msg.obj.toString(), true, true);
					activeDialog = syncingbox;
					
					runOnUiThread(new Runnable() {
			            public void run() {
			        		syncingbox.show();
			            }
					});
					
					break;
					
				case BEGIN_COMMUNICATE:
					myMenu.getItem(2).setTitle("Live");
					myMenu.getItem(2).setIcon(R.drawable.gdot2);
					myMenu.getItem(2).setEnabled(false);		
					
					@SuppressWarnings("unchecked")
					ArrayList<ISOBUSSocket> socks = (ArrayList<ISOBUSSocket>)msg.obj;
					final ISOBUSSocket imsock = socks.get(0);
					final DataGrabber dgrab = new DataGrabber();
		
					Thread ATAX = new Thread(){
							public void run(){
								org.isoblue.isobus.Message message = null;

								while(true){ 
									try {
										message = imsock.read();
										dbcon.saveMessage(message);
										
										if (message.getPgn().toString().equals("PGN:129029")){
											//@@ TODO: Check that the gpsbuffer starts from 0~6
											// it can cause parsing error if we just catch 7 messages arbitarily
											
											if(gpsbuffer.size() == 0){
												byte[] mdata = message.getData();
//												if(mdata[0] == 00){
//													gpsbuffer.add(message);
													//remove below
//												}
												gpsbuffer.add(message);
											}else if(gpsbuffer.size() > 0){
												gpsbuffer.add(message);
											}
											
											
										}else if (message.getPgn().toString().equals("PGN:65488")){
											final double result = dgrab.yieldData(message);
											Log.i("postman","Yield data " + result);
											
											runOnUiThread(new Runnable() {
									            public void run() {
									            	
									            	if(gplist.size() == 0){
									            		//if there no decoded GPS coordinate ready
									            		//do nothing
									            		return;
									            	}
									            	
									            	//plot yield data at latest coordinate
									            	LatLng previous_coord = gplist.get(gplist.size() - 1);
									            	markPlace(previous_coord, result + "");
									            	
									            	//Color.rgb((int)(255*Math.pow(result*10,2)), 255 - (int)Math.pow(result*50,2), 0)
									            	int TRESHC = Color.MAGENTA;						            	
									            	if(result >= Map.YIELD_HIGH){
									            		TRESHC = Color.GREEN;
									            	}else if(result >= Map.YIELD_MEDIUM){
									            		TRESHC = Color.YELLOW;
									            	}else if(result >= Map.YIELD_LOW){
									            		TRESHC = Color.RED;
									            	}else{
									            		TRESHC = Color.MAGENTA;
									            	}
									            	
									            	//create new path, don't care about old one 
									            	linePath = mMap.addPolyline(new PolylineOptions()
										       	     .width(5)
										       	     .color(TRESHC));
										       		 							            	
									            	//Clear gplist but retain latest coordinate for curve smoothness
									            	LatLng LatestCoord = gplist.get(gplist.size() - 1);
										       		gplist.clear();
										       		gplist.add(LatestCoord);
										       		
									            	
									            }
									        });
											
										}
										
										//If FastPackets are ready to be process
										//send them to DataGrabber
									
										
										if(gpsbuffer.size() == 7){
											org.isoblue.isobus.Message[] bar = gpsbuffer.toArray(new org.isoblue.isobus.Message[7]);
											final LatLng coord = dgrab.GNSSData(bar);
											Log.i("postman","GPS data " + coord.latitude + ", " + coord.longitude);
											gpsbuffer.clear();										
											
											//Once GPS coordinate is ready, update it on map  
											runOnUiThread(new Runnable() {
									            public void run() {
									            	gplist.add(coord);
									            	if(gplist.size() == 1){
									            		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 17.00f)); 
									            	}
													linePath.setPoints(gplist);	
									            }
									        });
											
										}
																														
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
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		
		 linePath = mMap.addPolyline(new PolylineOptions()
	     .width(5)
	     .color(Color.RED));
		 
		 
		 gplist = new ArrayList<LatLng>();
		 yieldMarkerList = new ArrayList<Marker>();
		 
		 linePath.setPoints(gplist);
	}
	
	//Handles incoming GPS coordinates from BBB sent over bluetooth
	public void markPlace(LatLng point, String lbl){

		Marker newmk = mMap.addMarker(new MarkerOptions()
        .position(point)
        .title("Yield Data")
        .snippet(lbl));

		
		if(yieldMarkerList.size() > 0){
			if(!yieldMarkerList.get(0).isVisible()){
				newmk.setVisible(false);
			}
		}
		
		yieldMarkerList.add(newmk);
		
	}
	
	
	private void Handle_SimulateStuff(){
		
		if(yieldMarkerList.size() == 0){
			return;
		}
		
		boolean setVis = true;
		if(yieldMarkerList.get(0).isVisible()){
			setVis = false;
		}
		
		for(int j = 0; j< yieldMarkerList.size(); j++){
			Marker ptr = (Marker)yieldMarkerList.get(j);
			ptr.setVisible(setVis);
		}
		
	}
	
	private void Handle_SearchDevice(){
		
		final DeviceSelectDialog devListbox = new DeviceSelectDialog();
		
		//foundDevices will contain list of devices found given by the BTAgent
		ArrayList<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
		
		//Our bluetooth connector goodies stuff and etc
		BTAgent connector = new BTAgent(Map.this, postman);
		
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
	    	switch(item.getItemId()){
	    		case R.id.action_search:
	    			Handle_SearchDevice();
	    			
	    		break;
	    		case R.id.action_sim:
	    			Handle_SimulateStuff();
	    			postman.obtainMessage(Map.SHOW_TOAST,
							-1, -1, "Toggle Numeric Yield").sendToTarget();
	    		break;
	    	}
	    	return true;
	    }

}