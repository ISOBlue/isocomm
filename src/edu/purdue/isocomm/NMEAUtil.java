package edu.purdue.isocomm;

import java.io.ByteArrayOutputStream;

import org.isoblue.isobus.ISOBUSSocket;
import org.isoblue.isobus.Message;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

//{reference diagram}
//See http://sourceforge.net/apps/mediawiki/openskipper/index.php?title=NMEA_2000_Fast_Packets
//for more detail about the msg parsing process

public class NMEAUtil {
	Context mContext;
	private int BYTESIZE = 8;

	public NMEAUtil(Context context, Handler handler) {
		mContext = context;
	}
	
	public void processMessages(ISOBUSSocket sck) {
		Message message = null;
		int total_data_byte = 6;
		boolean reading_data = false;
		int remaining_byte_in_msg = 8;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] mydata = null;
		byte[] cur_msg = null;
	
		
		while (true) { // keep reading

			if (remaining_byte_in_msg == 8) { // if we havent read anything in
												// this frame yet
				try {
					message = sck.read(); // read it
				} catch (InterruptedException e) {
					Log.e("ISOBLUE", "Unable to read socket for some reason");
				}

				Log.i("ISOBLUE", "NMEAUtil " + message.toString());
				
				cur_msg = message.getData(); // fit it in a byte array
				
         	   

//				byte msg_head = cur_msg[0]; // read the #< FRAME COUNT BYTE >
//				--remaining_byte_in_msg; // number subsequent bytes we need to
//											// read
//
//				// process header, if this is the first frame
//				if (!reading_data && msg_head == 0x0) {
//
//					// look for the second byte, it contains the length of the
//					// entire message we need to construct
//					// that is the messages in [yellow] of the reference diagram
//
//					total_data_byte = cur_msg[1];
//					Log.i("ISOBLUE", "Total Data Byte = " + total_data_byte);
//					reading_data = true;
//					mydata = new byte[total_data_byte];
//
//					--remaining_byte_in_msg;
//					--total_data_byte;
//				}
				
			} //end of while roupe

//			// process data in this message
//			if (reading_data) {
//				Log.i("ISOBLUE", "Reading yellow data block = "
//						+ total_data_byte);
//				while (remaining_byte_in_msg > 0) {
//					// read the rest of the bytes in the current message
//					// and concat it
//					byte buffer = cur_msg[BYTESIZE - remaining_byte_in_msg];
//					outputStream.write(buffer);
//
//					--remaining_byte_in_msg;
//					--total_data_byte;
//				}
//			}
//
//			// flag for "read one more message"
//			// if we are out of stuff to process
//			if (remaining_byte_in_msg == 0) {
//				remaining_byte_in_msg = 8;
//			}
//
//			// if we have read all the data byte
//			// as specified in Data Byte Count
//			if (total_data_byte == 0) {
//				mydata = outputStream.toByteArray();
//				break;
//			}

		}
	}
}
