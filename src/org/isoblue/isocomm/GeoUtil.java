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

package org.isoblue.isocomm;

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
		    double R = 6378.137;
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
