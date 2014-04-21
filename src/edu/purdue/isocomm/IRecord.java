package edu.purdue.isocomm;

public class IRecord {
	public int PGN;
	public long timestamp;
	public byte[] data;
	public int id;
	
	public IRecord(int i, String pgn, long ts, byte[] d){
		PGN = Integer.parseInt(pgn.substring(4)); //nasty hack (temporary)
		timestamp = ts;
		data = d;
		id = i;
	}
	
	public org.isoblue.isobus.Message asMsg(){
		org.isoblue.isobus.Message m = new org.isoblue.isobus.Message((short) id, new org.isoblue.isobus.PGN(PGN), data);
		return m;
	}
}
