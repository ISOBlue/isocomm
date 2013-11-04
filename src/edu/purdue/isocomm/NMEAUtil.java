package edu.purdue.isocomm;

import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

//{reference diagram}
//See http://sourceforge.net/apps/mediawiki/openskipper/index.php?title=NMEA_2000_Fast_Packets
//for more detail about the msg parsing process

public class NMEAUtil {
	Context mContext;
	public NMEAUtil(Context context, Handler handler) {
		mContext = context;
	}
	
	public void processMessages(ISOBUSSocket sck){
		Message message = null;
		int total_data_byte = 1;
		boolean reading_data = false; 
		int remaining_byte_in_msg = 8;
		byte[] mydata;
		
	 	while(remaining_byte_in_msg > 0){
	 		   try {
	     		    message = sck.read();
	     		} catch(InterruptedException e) {
	     		    // Thrown if the thread calling read can is interrupted for some reason
	     			Log.e("ISOBLUE","Unable to read socket");
	     		}
	     	   
	     	   Log.i("ISOBLUE",message.toString());
	     	    	   
	     	   byte[] cur_msg = message.getData(); //read this message
	     	   
	     	   byte msg_head = cur_msg[0]; //read the first byte
	     	   remaining_byte_in_msg--;
	     	  
	     	   //process header
	     	   if(msg_head == 0x0 && !reading_data){ 
	     		   //look for the second byte, it contains the length of the 
	     		   //entire message we need to construct
	     		   //that is the messages in [yellow] of the reference diagram
	     		    total_data_byte = cur_msg[1];
	     		    Log.i("ISOBLUE","Total Data Byte = " + total_data_byte);
	     		    reading_data = true;
	     		    mydata = new byte[total_data_byte];
	     	   }
	     	   
	     	   //process data
	     	   if(reading_data){
	     		   Log.i("ISOBLUE","Reading yellow data block = " + total_data_byte);
	     		   while(remaining_byte_in_msg > 0){
	     			   //read the rest of the bytes in the current message
	     			   //and concat it 
	     		   }
	     	   }
	     	   
	 	}
	}
}
