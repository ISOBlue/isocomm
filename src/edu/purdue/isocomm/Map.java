package edu.purdue.isocomm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.isoblue.isobus.ISOBUSSocket;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
import edu.purdue.isocomm.GeoUtil;

public class Map extends Activity {
	private GoogleMap mMap;
//	public HashMap<String, LatLng> places;
	public static final int CONNECTION_SUCCESS = 1;
	public static final int BEGIN_COMMUNICATE = 2;
	public static final int SHOW_PROGRESSBOX = 3;
	public static final int SHOW_TOAST = 4;
	public static final int BEGIN_LOCAL_PLOT = 5;
	public Menu myMenu;
	public Polyline linePath, linePath2; 
	public ArrayList<LatLng> gplist, gplist2;  //gplist contains all the GPS used to draw path
	public ArrayList<Marker> yieldMarkerList;
	private SQLController dbcon;
	private ProgressDialog activeDialog;

	
	private boolean done_with_sqlstream;
	private ArrayList<IRecord> sqlstream_records;
	public ISOBUSSocket imsock, bf_imsock, engsock;
	
	private static float YIELD_HIGH = 0.26f;
	private static float YIELD_MEDIUM = 0.24f;
	private static float YIELD_LOW = 0.20f;
	
	private static double YIELD_MAX = 0.19;
	
	private static int PGN_GNSS = 129029;
	private static int PGN_YIELD = 65488;

	private ArrayList<org.isoblue.isobus.Message> gpsbuffer; //gpsbuffer contains all the GPS messages to be decoded
	//### private ArrayList<org.isoblue.isobus.Message> gpsbuffer2; //gpsbuffer contains all the GPS messages to be decoded
	private int gpsbuf_start;
	private int gpsbuf_badstartcount = 0;
	
	private ColorMapper<Double> cmapper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#99000000")));

		setContentView(R.layout.activity_map);
		
		initMap();
		dbcon = new SQLController(Map.this);
		//buffer the GPS coordinates for manipulation
		gpsbuffer = new ArrayList<org.isoblue.isobus.Message>();
		//### gpsbuffer2 = new ArrayList<org.isoblue.isobus.Message>();
		
		gpsbuf_start = 0;
		gpsbuf_badstartcount = 0;
		done_with_sqlstream = false;
		
		//loadFromSQLite();
		cmapper = new ColorMapper<Double>((double) 0.15,YIELD_MAX);
	}
	
	private void loadFromSQLite(){
		sqlstream_records = dbcon.allMessages();
		if(sqlstream_records.size() == 0){
			done_with_sqlstream = true;
		}
	}

	//Postman deliver messages from DeviceSelectDialog to Map activity
	@SuppressLint("HandlerLeak")
	private final Handler postman = new Handler() {
		@Override
		public void handleMessage(Message msg) {
						
			switch (msg.what) {
				case SHOW_TOAST:
					
					
					Toast toast = Toast.makeText(Map.this, msg.obj.toString(),Toast.LENGTH_SHORT);
			 	    toast.show();
			 	    
			 	    if(activeDialog != null)
					{
						activeDialog.dismiss();
						activeDialog = null;
					}
			 	   
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
						ArrayList<ISOBUSSocket> socks = (ArrayList<ISOBUSSocket>)msg.obj;
						imsock = socks.get(0);
						engsock = socks.get(1);
					
					 //Thread t = new Thread(new Buffer_Stream_Thread());
					 //t.start();
					 Thread t2 = new Thread(new Normal_Stream_Thread());
					 t2.start();
					 
					 Thread t_imp = new Thread(new Engine_Read());
					 t_imp.start();
					 
				break;

			}
		}
	};
	
	public class Engine_Read implements Runnable{
		public void run(){
			org.isoblue.isobus.Message message = null;
			
			final DataGrabber dgrab = new DataGrabber();

			while(true){
				try{
				message = engsock.read();
				if (message.getPgn().asInt() == PGN_YIELD){
					final double result = dgrab.yieldData(message);
					Log.i("postman","Yield data " + result);
					
					if(done_with_sqlstream)
						dbcon.saveMessage(message); //Save single Yield message
					
					runOnUiThread(new Runnable() {
			            public void run() {
			            	
			            	if(gplist.size() == 0){
			            		//if there no decoded GPS coordinate ready
			            		//do nothing
			            		return;
			            	}
			            	
			            	//plot yield data at latest coordinate
			            	LatLng previous_coord = gplist.get(gplist.size() - 1);
//			            	markPlace(previous_coord, result + "");

			            	int TRESHC = cmapper.map(result);
			            	
			            	//create new path, don't care about old one 
			            	linePath = mMap.addPolyline(new PolylineOptions()
				       	     .width(20)
				       	     .color(TRESHC));
				       		 							            	
			            	//Clear gplist but retain latest coordinate for curve smoothness
			            	LatLng LatestCoord = gplist.get(gplist.size() - 1);
				       		gplist.clear();
				       		gplist.add(LatestCoord);
				       		
			            	
			            }
			        });
				
					
				}
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
			
				 
			
		}
	}
	
	public class Normal_Stream_Thread implements Runnable{
		public void run(){
			
			org.isoblue.isobus.Message message = null;
			final DataGrabber dgrab = new DataGrabber();

			while(true){ 
				try {
//					if(done_with_sqlstream){
//						//If Done with SQL data use live socket data
//						message = imsock.read();
//					}else{
//						//Use SQL old data
//						
//						if(sqlstream_records.size() == 0){
//							//can pop no maor
//							done_with_sqlstream = true;
//							Log.i("sqlstream", "done with sqltream");
//							postman.obtainMessage(Map.SHOW_TOAST,
//									-1, -1, "Using Live Data").sendToTarget();
//							continue;
//						}else{
//							Log.i("ThreadNST","popping");
//							message = sqlstream_records.remove(0).asMsg();
//						}
//					}
					done_with_sqlstream = false;
					Log.i("Reading","reading frm sock..");
					message = imsock.read();
					
					if (message.getPgn().asInt() == PGN_GNSS){
						if(done_with_sqlstream)
							dbcon.saveMessage(message);

						byte[] xdata = message.getData();
						int StartByte = xdata[0] & 0x0F; //Unsigned int
						Log.i("xdata",StartByte + " = 0x" + String.format("%02X ", StartByte));

						if(0 == gpsbuf_start && (StartByte % 10 == 0)){
							Log.i("xdata","Start byte found" + StartByte);
							gpsbuffer.add(message);
							gpsbuf_start = StartByte + 1;
							Log.i("xdata",StartByte + " = 0x" + String.format("%02X ", StartByte));
						}else if(gpsbuf_start == StartByte && gpsbuf_start != 0){
							//If frame buffering has already began
							gpsbuffer.add(message);
							gpsbuf_start++;
						}else if(gpsbuf_start != StartByte && gpsbuf_start != 0 && StartByte == 0){
							//Begin collecting new frame , clear previous frame buffer
							gpsbuffer.clear();
							gpsbuffer.add(message);
							gpsbuf_start = 1; //look for 0x01 as starting byte for next frame
					    }else if(gpsbuf_start != StartByte && gpsbuf_start != 0){
							Log.i("xdata","Bad startbyte, starting over mismatch " + StartByte + " with count " + gpsbuf_start + " Tries: " + gpsbuf_badstartcount);
							gpsbuf_badstartcount++;
							
							if(gpsbuf_badstartcount >= 5){
								//Try 5 more frames
								gpsbuffer.clear();
								gpsbuf_start = 0;
								gpsbuf_badstartcount = 0;
							}
						}
						
						
					}
					
					
					//If FastPackets are ready to be process
					//send them to DataGrabber
				
					
					if(gpsbuffer.size() == 7){
						
						org.isoblue.isobus.Message[] bar = gpsbuffer.toArray(new org.isoblue.isobus.Message[7]);
						final LatLng coord = dgrab.GNSSData(bar);

						Log.i("postman","GPS data " + coord.latitude + ", " + coord.longitude);
						gpsbuffer.clear();	
						gpsbuf_start = 0;
						
						//compare coord with previous coord 
						//filter out if they are closer than 1 meter

						if(gplist.size() > 1){
							LatLng pcoord = gplist.get(gplist.size() - 1);
							//TODO: Turning point detection 
							// http://stackoverflow.com/questions/17422314/polylines-appearing-on-map-where-they-shouldnt

							double ddist = GeoUtil.distanceInMeter(pcoord, coord);
							Log.i("distdiff","Dist : " + ddist);
							if(ddist <= 500 && ddist > 0.1){
								gplist.add(coord);
							}
						}else{
							gplist.add(coord);
						}

						TimeUnit.MILLISECONDS.sleep(25);

						//Once GPS coordinate is ready, update it on map  
						runOnUiThread(new Runnable() {
				            public void run() {
				            	
				            	/*if(gplist.size() == 1){
				            		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 17.00f));
				            		 
				            	}*/
								linePath.setPoints(gplist);	
				            }
				        });
						
					}
																									
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
	}
	
//	
//	public class Buffer_Stream_Thread implements Runnable{
//		public void run(){
//			
//			org.isoblue.isobus.Message message = null;
//			final DataGrabber dgrab = new DataGrabber();
//
//			while(true){ 
//				try {
//
//					message = bf_imsock.read();
//					
//					if (message.getPgn().asInt() == PGN_GNSS){
//						if(done_with_sqlstream)
//							dbcon.saveMessage(message);
//
//						byte[] xdata = message.getData();
//						int StartByte = xdata[0] & 0x0F; //Unsigned int
//						Log.i("xdata",StartByte + " = 0x" + String.format("%02X ", StartByte));
//
//						if(0 == gpsbuf_start && (StartByte % 10 == 0)){
//							Log.i("xdata","Start byte found" + StartByte);
//							gpsbuffer.add(message);
//							gpsbuf_start = StartByte + 1;
//							Log.i("xdata",StartByte + " = 0x" + String.format("%02X ", StartByte));
//						}else if(gpsbuf_start == StartByte && gpsbuf_start != 0){
//							//If frame buffering has already began
//							gpsbuffer.add(message);
//							gpsbuf_start++;
//						}else if(gpsbuf_start != StartByte && gpsbuf_start != 0 && StartByte == 0){
//							//Begin collecting new frame , clear previous frame buffer
//							gpsbuffer.clear();
//							gpsbuffer.add(message);
//							gpsbuf_start = 1; //look for 0x01 as starting byte for next frame
//					    }else if(gpsbuf_start != StartByte && gpsbuf_start != 0){
//							Log.i("xdata","Bad startbyte, starting over mismatch " + StartByte + " with count " + gpsbuf_start + " Tries: " + gpsbuf_badstartcount);
//							gpsbuf_badstartcount++;
//							
//							if(gpsbuf_badstartcount >= 5){
//								//Try 5 more frames
//								gpsbuffer.clear();
//								gpsbuf_start = 0;
//								gpsbuf_badstartcount = 0;
//							}
//						}
//						
//						
//					}else if (message.getPgn().asInt() == PGN_YIELD){
//						final double result = dgrab.yieldData(message);
//						Log.i("postman","Yield data " + result);
//						
//						if(done_with_sqlstream)
//							dbcon.saveMessage(message); //Save single Yield message
//						
//						runOnUiThread(new Runnable() {
//				            public void run() {
//				            	
//				            	if(gplist.size() == 0){
//				            		//if there no decoded GPS coordinate ready
//				            		//do nothing
//				            		return;
//				            	}
//				            	
//				            	//plot yield data at latest coordinate
//				            	LatLng previous_coord = gplist.get(gplist.size() - 1);
////				            	markPlace(previous_coord, result + "");
//				            	
//				            	//Color.rgb((int)(255*Math.pow(result*10,2)), 255 - (int)Math.pow(result*50,2), 0)
//				            	int TRESHC = getResources().getColor(R.color.darkgreen);						            	
//				            	if(result >= Map.YIELD_HIGH){
//				            		TRESHC = getResources().getColor(R.color.darkgreen);
//				            	}else if(result >= Map.YIELD_MEDIUM){
//				            		TRESHC = getResources().getColor(R.color.lightgreen);	
//				            	}else if(result >= Map.YIELD_LOW){
//				            		TRESHC = getResources().getColor(R.color.yellow);	
//				            	}else{
//				            		TRESHC = getResources().getColor(R.color.orange);
//				            	}
//				            	
//				            	//create new path, don't care about old one 
//				            	linePath = mMap.addPolyline(new PolylineOptions()
//					       	     .width(10)
//					       	     .color(TRESHC));
//					       		 							            	
//				            	//Clear gplist but retain latest coordinate for curve smoothness
//				            	LatLng LatestCoord = gplist.get(gplist.size() - 1);
//					       		gplist.clear();
//					       		gplist.add(LatestCoord);
//					       		
//				            	
//				            }
//				        });
//						
//					}
//					
//					//If FastPackets are ready to be process
//					//send them to DataGrabber
//				
//					
//					if(gpsbuffer.size() == 7){
//						
//						org.isoblue.isobus.Message[] bar = gpsbuffer.toArray(new org.isoblue.isobus.Message[7]);
//						final LatLng coord = dgrab.GNSSData(bar);
//
//						Log.i("postman","GPS data " + coord.latitude + ", " + coord.longitude);
//						gpsbuffer.clear();	
//						gpsbuf_start = 0;
//						
//						//compare coord with previous coord 
//						//filter out if they are closer than 1 meter
//
//						if(gplist.size() > 1){
//							LatLng pcoord = gplist.get(gplist.size() - 1);
//							//TODO: Turning point detection 
//							// http://stackoverflow.com/questions/17422314/polylines-appearing-on-map-where-they-shouldnt
//
//							double ddist = GeoUtil.distanceInMeter(pcoord, coord);
//							Log.i("distdiff","Dist : " + ddist);
//							if(ddist <= 500 && ddist > 0.1){
//								gplist.add(coord);
//							}
//						}else{
//							gplist.add(coord);
//						}
//
//						TimeUnit.MILLISECONDS.sleep(25);
//
//						//Once GPS coordinate is ready, update it on map  
//						runOnUiThread(new Runnable() {
//				            public void run() {
//				            	
//				            	if(gplist.size() == 1){
//				            		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 17.00f)); 
//				            	}
//				            	linePath2.setPoints(gplist);	
//				            }
//				        });
//						
//					}
//																									
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				
//			}
//		}
//	}
//	
	
	private void initMap(){
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); //init map
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		 linePath = mMap.addPolyline(new PolylineOptions()
	     .width(20)
	     .color(Color.TRANSPARENT));
		 
		 linePath2 = mMap.addPolyline(new PolylineOptions()
	     .width(20)
	     .color(Color.WHITE));
		 
		 gplist = new ArrayList<LatLng>();
		 gplist2 = new ArrayList<LatLng>();
		 yieldMarkerList = new ArrayList<Marker>();
		 
		 linePath.setPoints(gplist);
		 
		 mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.988075256347656, -86.1761245727539), 17.00f));
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
	    		case R.id.action_history:

	    			postman.obtainMessage(Map.BEGIN_COMMUNICATE,
							-1, -1, null).sendToTarget();
	    		break;
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