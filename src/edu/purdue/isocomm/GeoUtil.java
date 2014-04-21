package edu.purdue.isocomm;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

public class GeoUtil {
	 private static GeoUtil instance = null;
	   protected GeoUtil() {
	      // Exists only to defeat instantiation.
	   }
	   public static GeoUtil getInstance() {
	      if(instance == null) {
	         instance = new GeoUtil();
	      }
	      return instance;
	   }
	   
	   public static double distanceInMeter(LatLng x, LatLng y){
		   double lat1 = x.latitude;
		   double lat2 = y.latitude;
		   double lon1 = x.longitude;
		   double lon2 = y.longitude;
		   double R = 6378.137; // Radius of earth in KM
		    double dLat = (lat2 - lat1) * Math.PI / 180;
		    double dLon = (lon2 - lon1) * Math.PI / 180;
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
		    Math.sin(dLon/2) * Math.sin(dLon/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double d = R * c;
		  
		    return d * 1000; 
	   }
	   
		public static boolean SamePoint(Point a, Point b){
			return (a.x == b.x) && ( a.y == b.y);
		}
		
		public static boolean SameLatLng(LatLng x, LatLng y){
			return (x.latitude == y.latitude) && (x.longitude == y.longitude);
		}
		
}
