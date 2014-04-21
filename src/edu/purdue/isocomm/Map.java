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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;


import android.R.menu;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import edu.purdue.isocomm.DataGrabber;


public class Map extends Activity {
	private GoogleMap mMap;
	public HashMap<String, LatLng> places;
	public static final int CONNECTION_SUCCESS = 1;
	public static final int BEGIN_COMMUNICATE = 2;
	public static final int SHOW_PROGRESSBOX = 3;
	public static final int MAP_DRAW_OVERLAY = 11;
	public static final int SHOW_TOAST = 4;
	public static final int TRANSLATE_POINT = 12;
	public Menu myMenu;
	public Polyline linePath; 
	public ArrayList<LatLng> gplist;  //gplist contains all the GPS used to draw path
	public ArrayList<Marker> yieldMarkerList;
	private SQLController dbcon;
	private ProgressDialog activeDialog;

	public static float YIELD_HIGH = 0.26f;
	public static float YIELD_MEDIUM = 0.24f;
	public static float YIELD_LOW = 0.20f;
	public GroundOverlay globaloverlay;
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
		//places.put("birck",new LatLng(40.42262549999998, -86.92454150000002));
		//places.put("ee",new LatLng(40.428903899999995, -86.91123760000002));
		
		initMap();
		//dbcon = new SQLController(Map.this);
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
					
					
					Toast toast = Toast.makeText(Map.this, msg.obj.toString(),Toast.LENGTH_SHORT);
			 	    toast.show();
			 	    
			 	    if(activeDialog != null)
					{
						activeDialog.dismiss();
						activeDialog = null;
					}
			 	   
					break;
				case MAP_DRAW_OVERLAY:
					Bitmap image = (Bitmap)msg.obj;
					BitmapDescriptor overlay_img = BitmapDescriptorFactory.fromBitmap(image);
					 
					 if(globaloverlay != null){
						 globaloverlay.remove();
					 }
					 
					 LatLng center_anchor = new LatLng(39.0487286, -86.8766694);
					 //need to find LatLng to Pixel translation that is consistent
					 LatLng east_anchor = new LatLng(39.0487286, -86.8766694 + 0.00347);
					 LatLng west_anchor = new LatLng(39.0487286, -86.8766694 - 0.00347);
					 LatLng north_anchor = new LatLng(39.0487286 + 0.00266, -86.8766694);
					 LatLng south_anchor = new LatLng(39.0487286 - 0.00266, -86.8766694);
					 
					 
					/* mMap.addMarker(new MarkerOptions()
				        .position(east_anchor)
				        .title("East Mark"));
					
					 mMap.addMarker(new MarkerOptions()
				        .position(west_anchor)
				        .title("West Mark"));
					 
					 mMap.addMarker(new MarkerOptions()
				        .position(north_anchor)
				        .title("North Mark"));

					 mMap.addMarker(new MarkerOptions()
				        .position(south_anchor)
				        .title("South Mark"));*/

					 
					 //Test Projection
					
					 LatLng house_anchor = new LatLng(39.047214, -86.873819);

					 globaloverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
			         .image(overlay_img)
			         .position(center_anchor, 600f, 600f));
					 
					 Point markerScreenPosition = mMap.getProjection().toScreenLocation(house_anchor);
					 Log.i("isoblue","CONVLOC (translated from input) " + markerScreenPosition.x + "," + markerScreenPosition.y);
					 Point refPosition = mMap.getProjection().toScreenLocation(center_anchor);

					 Log.i("isoblue","CONVLOC (reference center of overlay) " + refPosition.x + "," + refPosition.y);
					 
					 Point pixel_center = new Point(refPosition.x,refPosition.y);
					 Point pixel_target = new Point(markerScreenPosition.x,markerScreenPosition.y);
					 Point pixel_diff = new Point(pixel_target.x - pixel_center.x,pixel_target.y - pixel_center.y);

					 Log.i("isoblue","CONVLOC (dx,dy) " + pixel_diff.x + "," + pixel_diff.y);
					 
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
											// it can cause parsing error if we just catch 7 messages arbitarily!!!
											
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
//									            	markPlace(previous_coord, result + "");
									            	
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
											
											//compare coord with previous coord 
											//filter out if they are closer than 1 meter

											if(gplist.size() > 1){
												LatLng pcoord = gplist.get(gplist.size() - 1);
												//TODO: Turning point detection 
												// http://stackoverflow.com/questions/17422314/polylines-appearing-on-map-where-they-shouldnt

												if((Math.abs(coord.latitude - pcoord.latitude) > 0.0000000000001) && (Math.abs(coord.longitude - pcoord.longitude) > 0.0000000000001)){
													gplist.add(coord);
												}else{
													Log.i("isoblue","Coordinates too close");
												}
											}else{
												gplist.add(coord);
											}
											
											//Once GPS coordinate is ready, update it on map  
											runOnUiThread(new Runnable() {
									            public void run() {
									            	
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
	
	
	GoogleMap.OnCameraChangeListener GetOnCameraChangeListener(){
		return new GoogleMap.OnCameraChangeListener(){

			@Override
			public void onCameraChange(CameraPosition position) {
				// TODO Auto-generated method stub
				VisibleRegion rVisible = mMap.getProjection().getVisibleRegion();
				LatLngBounds rVisible_latlng = rVisible.latLngBounds;

				Toast toast = Toast.makeText(Map.this, rVisible_latlng.toString(),Toast.LENGTH_SHORT);
		 	    toast.show();
		 	    
			}
			
		};
	}
	
	private void demoDraw(){
		//Test Ground Overlay
		 
		 Thread FastDraw = new BitMapGenerateThread();
		 FastDraw.start();
		
	}
	
	private void initMap(){
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); //init map
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		 linePath = mMap.addPolyline(new PolylineOptions()
	     .width(5)
	     .color(Color.RED));
		 
		 gplist = new ArrayList<LatLng>();
		 yieldMarkerList = new ArrayList<Marker>();
		 
		 //Listeners for map
		 
		 mMap.setOnCameraChangeListener(GetOnCameraChangeListener());

		// demoDraw();
		 
		 
		 /*for(int i = 0; i< 800; i++){
			 mMap.addGroundOverlay(new GroundOverlayOptions()
	         .image(overlay_img)
	         .position(new LatLng(39.0487236 + (i*8.5*.000001), -86.8766684 + (Math.sin(i)*.00001)), 1.5f, 1.5f));
			 gplist.add(new LatLng(39.0487236 + (i*8.5*.000001), -86.8766684 + (Math.sin(i)*.00001)));
			 
		 }*/
		 
		 
		 linePath.setPoints(gplist);
		  
		 
		 mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.0487236, -86.8766694), 17.00f)); 
		 
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
	    			/*Handle_SimulateStuff();
	    			postman.obtainMessage(Map.SHOW_TOAST,
							-1, -1, "Toggle Numeric Yield").sendToTarget();*/
//	    			demoDraw();
	    			final LegendDialog devListbox = new LegendDialog();
	    			devListbox.mContext = Map.this;
	    			devListbox.setHandler(postman);
	    			
	    			runOnUiThread(new Runnable() {
	    	            public void run() {
	    	            	devListbox.show(getFragmentManager(), "btdev"); 
	    	            }
	    	        });
	    			
	    		break;
	    		
	    		case R.id.action_history:
	    			demoDraw();
	    	}
	    	return true;
	    }
	
	
	
	private class BitMapGenerateThread extends Thread{
		float[] transposeToOV(GoogleMap maph, 
							  LatLng coord, 
							  LatLng refCenter,
							  int DW, 
							  int DH){
			
			float lin0 = (float) coord.latitude;
			float lcen0 = (float) refCenter.latitude;
			float relative_center0 = DH/2;
			
			float lin1 = (float) coord.longitude;
			float lcen1 = (float) refCenter.longitude;
			float relative_center1 = DW/2;
					
			float[] dbx = new float[]{(lin0-lcen0) + relative_center0,
									  (lin1-lcen1) + relative_center1};
			return dbx;
		}
		
		public void run(){
			 //Generate
			 int DIM_WIDTH = 600;
			 int DIM_HEIGHT = 600;
			 Bitmap image = Bitmap.createBitmap(DIM_WIDTH,DIM_HEIGHT,Bitmap.Config.ARGB_8888);
			 
			 Canvas canvas = new Canvas(image);
			 Paint redfp  = new Paint();
			 Paint boundfp = new Paint();

			 redfp.setColor(Color.RED);
			 redfp.setStrokeWidth(2);
			 
			 boundfp.setColor(Color.YELLOW);
			 boundfp.setAlpha(10);
			 boundfp.setStrokeWidth(6);

			 //Center Point
			 LatLng center_anchor = new LatLng(39.0487286, -86.8766694);
			 LatLng house_anchor = new LatLng(39.047214, -86.873819);

			 //Draw Square Boundary for debugging
			 for(int i = 0; i< DIM_WIDTH; i++){
				 canvas.drawPoint(0, i, boundfp);
				 canvas.drawPoint(i, 0, boundfp);
				 canvas.drawPoint(DIM_WIDTH - 1, i, boundfp);
				 canvas.drawPoint(i, DIM_HEIGHT, boundfp);
			 }
	
			 int k;
			 for(k=0;k<DIM_HEIGHT;k++){
				 
				 LatLng newlat = new LatLng(center_anchor.latitude, 
						 					center_anchor.longitude + k);
				 float[] samplingPt = transposeToOV(mMap, newlat, 
						  center_anchor,
						  DIM_WIDTH, 
						  DIM_HEIGHT);

				 canvas.drawPoint(samplingPt[0], samplingPt[1], redfp);
			 }
			 
			 for(k=0;k<DIM_WIDTH*Math.random();k++){
				 
				 LatLng newlat = new LatLng(center_anchor.latitude + k, 
						 					center_anchor.longitude + Math.random()*10 +k);
				 float[] samplingPt = transposeToOV(mMap, newlat, 
						  center_anchor,
						  DIM_WIDTH, 
						  DIM_HEIGHT);

				 canvas.drawPoint(samplingPt[0], samplingPt[1], redfp);
			 }
			 
			 float[] ieref = transposeToOV(mMap, house_anchor, 
					  center_anchor,
					  DIM_WIDTH, 
					  DIM_HEIGHT);
			 canvas.drawPoint(ieref[0], ieref[1], redfp);
			 
			 
			
//
			 canvas.drawPoint(569, 390,redfp);
			 
			 //TODO: Pass in set of width and height
			 postman.obtainMessage(Map.MAP_DRAW_OVERLAY,
						-1, -1, image).sendToTarget();
		}
		
	}
}