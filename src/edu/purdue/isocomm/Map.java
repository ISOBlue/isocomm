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

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;

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
	
	private void initMap(){
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		labelStuff();
	}
	
	public void markPlace(LatLng point, String lbl){
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.map_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
