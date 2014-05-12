/* Note: Not Intended for use in this feature-limited release (see other branch of this repository)
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
