/*
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

import java.io.IOException;
import java.util.ArrayList;

import org.isoblue.isobus.ISOBUSSocket;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
	public static final int CONNECTION_SUCCESS = 1;
	public static final int BEGIN_COMMUNICATE = 2;
	public static final int SHOW_PROGRESSBOX = 3;
	public static final int SHOW_TOAST = 4;
	public static final int BEGIN_LOCAL_PLOT = 5;
	public Menu myMenu;
	public Polyline polyPath_normal, polyPath_buffer; 
	public ArrayList<LatLng> coords_normal, coords_buffer;  //coords_normal contains all the GPS used to draw path
	public ArrayList<Marker> yieldMarkerList;
	private ProgressDialog activeDialog;
	public ISOBUSSocket imsock, engsock, imsock_b, engsock_b;
	
	private static double YIELD_MAX = 0.19;
	
	private static int PGN_GNSS = 129029;
	private static int PGN_YIELD = 65488;


	private ColorMapper<Double> cmapper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#99000000")));

		setContentView(R.layout.activity_map);
		
		initMap();


		//loadFromSQLite();
		cmapper = new ColorMapper<Double>((double) 0.15,YIELD_MAX);
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
						
						//UI Stuff
						myMenu.getItem(2).setTitle("Live");
						myMenu.getItem(2).setIcon(R.drawable.gdot2);
						myMenu.getItem(2).setEnabled(false);	
						
						ArrayList<ISOBUSSocket> socks = (ArrayList<ISOBUSSocket>)msg.obj;
						
						imsock = socks.get(0);
						engsock = socks.get(1);
						imsock_b = socks.get(2);
						engsock_b = socks.get(3);
						
						 Thread n1 = new Thread(new Normal_Stream_Thread_EN());
						 Thread n2 = new Thread(new GPSStream_Thread(imsock,coords_normal));
						 Thread n3 = new Thread(new Buffer_Stream_Thread_EN());
						 Thread n4 = new Thread(new GPSStream_Thread(imsock_b,coords_buffer));

						 n1.start();
						 n2.start();

						 n3.start();
						 n4.start();

				break;

			}
		}
	};
	
	public class GPSStream_Thread implements Runnable{
		private ISOBUSSocket sock;
		private ArrayList<org.isoblue.isobus.Message> gpsbuffer;
		private int gpsbuf_badstartcount,gpsbuf_start;
		private ArrayList<LatLng> coords_normal;
		
		public GPSStream_Thread(ISOBUSSocket s, ArrayList<LatLng> ugp){
			this.sock = s;
			this.gpsbuffer = new ArrayList<org.isoblue.isobus.Message>();
			this.gpsbuf_badstartcount = 0;
			this.gpsbuf_start = 0;
			this.coords_normal = ugp;
		}
		
		@Override
		public void run() {
			// Stream GPS from Implement Bus
			org.isoblue.isobus.Message message = null;
			final DataGrabber dgrab = new DataGrabber();

			while(true){ 
				try {
					//Read form given socket
					message = this.sock.read();
					
					if (message.getPgn().asInt() == PGN_GNSS){

						byte[] xdata = message.getData();
						int StartByte = xdata[0] & 0x0F; //Unsigned int

						if(0 == this.gpsbuf_start && (StartByte % 10 == 0)){
							Log.i("fastpacket","Start byte found" + StartByte);
							this.gpsbuffer.add(message);
							gpsbuf_start = StartByte + 1;
						}else if(this.gpsbuf_start == StartByte && this.gpsbuf_start != 0){
							//If frame buffering has already began
							this.gpsbuffer.add(message);
							gpsbuf_start++;
						}else if(this.gpsbuf_start != StartByte && this.gpsbuf_start != 0 && StartByte == 0){
							//Begin collecting new frame , clear previous frame buffer
							this.gpsbuffer.clear();
							this.gpsbuffer.add(message);
							this.gpsbuf_start = 1; //look for 0x01 as starting byte for next frame
					    }else if(this.gpsbuf_start != StartByte && this.gpsbuf_start != 0){
							this.gpsbuf_badstartcount++;
							
							if(this.gpsbuf_badstartcount >= 5){
								//Try 5 more frames
								this.gpsbuffer.clear();
								this.gpsbuf_start = 0;
								this.gpsbuf_badstartcount = 0;
							}
						}
					}
					
					
					//If FastPackets are ready to be process send them to DataGrabber
					if(gpsbuffer.size() == 7){
						
						org.isoblue.isobus.Message[] bar = gpsbuffer.toArray(new org.isoblue.isobus.Message[7]);
						
						final LatLng coord = dgrab.GNSSData(bar);

						Log.i("postman","GPS data " + coord.latitude + ", " + coord.longitude);
						
						this.gpsbuffer.clear();	
						this.gpsbuf_start = 0;
						
						//compare coord with previous coord 
						//filter out if they are closer than 100 meter

						if(coords_normal.size() > 1){
							LatLng pcoord = coords_normal.get(coords_normal.size() - 1);

							double ddist = GeoUtil.distanceInMeter(pcoord, coord);
							if(ddist <= 100 && ddist > 0.5){
								coords_normal.add(coord);
							}
						}else{
							coords_normal.add(coord);
						}

						//Map UI Task
						//Once GPS coordinate is ready, update it on map  
						runOnUiThread(new Runnable() {
				            public void run() {
								polyPath_normal.setPoints(coords_normal);	
				            }
				        });
						
					}
																									
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	}

	public class Normal_Stream_Thread_EN implements Runnable{
		public void run(){
			org.isoblue.isobus.Message message = null;
			
			final DataGrabber dgrab = new DataGrabber();

			while(true){
				try{
				message = engsock.read();
				if (message.getPgn().asInt() == PGN_YIELD){
					final double result = dgrab.yieldData(message);
					runOnUiThread(new Runnable() {
			            public void run() {
			            	if(coords_normal.size() == 0){
			            		//if there no decoded GPS coordinate ready
			            		//do nothing
			            		return;
			            	}
			            	int TRESHC = cmapper.map(result);
			            	//create new path, don't care about old one 
			            	if(coords_normal.size() >= 5){
			            		LatLng LatestCoord = coords_normal.get(coords_normal.size() - 1);
			            		polyPath_normal = mMap.addPolyline(new PolylineOptions()
					       	     .width(20)
					       	     .color(TRESHC));	
			            		//Clear coords_normal but retain latest coordinate for curve smoothness
					       	coords_normal.clear();
					       	coords_normal.add(LatestCoord);
			            	}
			            				            	
			            }
			        });
				
					
				}
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
				 
			
		}
	}
	
	public class Buffer_Stream_Thread_EN implements Runnable{
		public void run(){
			org.isoblue.isobus.Message message = null;
			
			final DataGrabber dgrab = new DataGrabber();

			while(true){
				try{
				message = engsock_b.read();
				if (message.getPgn().asInt() == PGN_YIELD){
					final double result = dgrab.yieldData(message);
					Log.i("postman","Yield data " + result);
					
					runOnUiThread(new Runnable() {
			            public void run() {
			            	
			            	if(coords_buffer.size() == 0){
			            		//if there no decoded GPS coordinate ready
			            		return;
			            	}

			            	int TRESHC = cmapper.map(result); //Color determined by cmapper
			            	
			            	if(coords_buffer.size() >= 5){
			            		//create new path, don't care about old one 
				            	polyPath_buffer = mMap.addPolyline(new PolylineOptions()
					       	     .width(20)
					       	     .color(TRESHC));
				            	//Clear coords_normal but retain latest coordinate for curve smoothness
				            	LatLng LatestCoord = coords_buffer.get(coords_buffer.size() - 1);
					       		coords_buffer.clear();
					       		coords_buffer.add(LatestCoord);
			            	}
			            }
			        });
				
					
				}
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
				 
			
		}
	}

	private void initMap(){
		
		 mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); //init map
		 mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		 
		 //Initialize both buffer and normal polyline path to be gray
		 polyPath_normal = mMap.addPolyline(new PolylineOptions()
	     .width(20)
	     .color(Color.GRAY));
		 polyPath_buffer = mMap.addPolyline(new PolylineOptions()
	     .width(20)
	     .color(Color.GRAY));
		 
		 coords_normal = new ArrayList<LatLng>();
		 coords_buffer = new ArrayList<LatLng>();
		 
		 yieldMarkerList = new ArrayList<Marker>();
		 
		 polyPath_normal.setPoints(coords_normal);
		 polyPath_buffer.setPoints(coords_buffer);

		 mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.988075256347656, -86.1761245727539), 17.00f));
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
			
			//TODO: Move to Handler
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
	    			//No action assigned
	    		break;
	    		case R.id.action_search:
	    			Handle_SearchDevice();
	    		break;
	    		case R.id.action_sim:
	    			//No action assigned
	    		break;
	    	}
	    	return true;
	    }

}