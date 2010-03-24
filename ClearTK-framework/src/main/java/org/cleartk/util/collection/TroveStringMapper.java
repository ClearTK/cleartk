 /** 
 * Copyright (c) 2010, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.util.collection;

import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <br>Copyright (c) 2010, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 */
public class TroveStringMapper implements StringMapper, Writable {

	private static final long serialVersionUID = -2082431386582502356L;
	
	public TroveStringMapper(int cutoff) {
		this.cutoff = cutoff;
	}

	public int getOrGenerateInteger(String s) {
		if( expandMap ) {
			if( countingMap.containsKey(s) ) {
				Entry e = countingMap.get(s);
				e.increment();
				return e.i;
			} else {
				Entry e = new Entry(nextValue++);
				countingMap.put(s, e);
				return e.i;
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public int getInteger(String s) throws UnknownKeyException {
		if( expandMap ) {
			throw new UnsupportedOperationException();
		} else {
			if( stringIntMap.containsKey(s) )
				return stringIntMap.get(s);
			else
				throw new UnknownKeyException(s);
		}
	}

	public void finalizeMap() {
		int total = 0;
		int kept = 0;
		
		stringIntMap = new TObjectIntHashMap<String>();
		for( String s : countingMap.keySet() ) {
			Entry e = countingMap.get(s);
			total += 1;
			
			if( e.count >= cutoff ) {
				stringIntMap.put(s, e.i);
				kept += 1;
			}
		}
		
		Logger.getLogger("org.cleartk.util.collection.TroveStringMapper").info(String.format("discarded %d features that occurred less than %d times; %d features remaining", total - kept, cutoff, kept));
		
		countingMap = null;
		expandMap = false;		
	}

	public void write(File file) throws IOException {
		Writer writer = new FileWriter(file);
		write(writer);
		writer.close();
	}

	public void write(Writer writer) throws IOException {
		if( expandMap )
			throw new UnsupportedOperationException();
		
		for( Object o : stringIntMap.keys() ) {
			String key = (String) o;
			writer.append(String.format("%d %s\n", stringIntMap.get(key), key));
		}

		writer.flush();
	}


	boolean expandMap = true;
	int nextValue = 1;
	int cutoff;
	Map<String,Entry> countingMap = new HashMap<String,Entry>();
//	Map<String,Entry> countingMap = new DiskMap();

	TObjectIntHashMap<String> stringIntMap = null;


	private static class Entry {
		public Entry(int i) {
			this.i = i;
			this.count = 1;
		}

		public void increment() {
			count += 1;
		}

		public int i;
		public int count;
	}
	
//	private static class LinkedEntry extends Entry {
//		public LinkedEntry(String s, int i, int count, DiskMap owningMap) {
//			super(i);
//			this.s = s;
//			this.count = count;
//			this.owningMap = owningMap;
//		}
//		
//		@Override
//		public void increment() {
//			count += 1;
//			
//			owningMap.update(this.s, this);
//		}
//		
//		private String s;
//		private DiskMap owningMap;
//	}
//
//	private static class DiskMap implements Map<String,org.cleartk.util.collection.TroveStringMapper.Entry> {
//		
//		public DiskMap() {
//			try {
//				filename = File.createTempFile("stringmap", ".db");
//				filename.deleteOnExit();
//				
//				Class.forName("org.sqlite.JDBC");
//				c = DriverManager.getConnection("jdbc:sqlite:" + filename);
//				System.err.println(".");
//				c.setAutoCommit(true);
//				
//				PreparedStatement stat = c.prepareStatement("create table entries "
//						+ "(s text unique, i integer unique, count integer)");
//				stat.execute();
//				stat.close();
//			} catch (ClassNotFoundException e) {
//				throw new RuntimeException(e);
//			} catch (SQLException e) {
//				throw new RuntimeException(e);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//		
//		public void clear() {
//			throw new UnsupportedOperationException();
//		}
//
//		public boolean containsKey(Object o) {
//			String s = (String) o;
//			
//			try {
//				PreparedStatement stat = c.prepareStatement("select * from entries "
//						+ "where s = ? limit 1");
//				stat.setString(1, s);
//
//				ResultSet rs = stat.executeQuery();
//				boolean contains = rs.next();
//				rs.close();
//				
//				stat.close();
//				
//				return contains;
//			} catch (SQLException e1) {
//				throw new RuntimeException(e1);
//			}
//		}
//
//		public boolean containsValue(Object value) {
//			throw new UnsupportedOperationException();
//		}
//
//		public Set<java.util.Map.Entry<String, org.cleartk.util.collection.TroveStringMapper.Entry>> entrySet() {
//			throw new UnsupportedOperationException();
//		}
//
//		public org.cleartk.util.collection.TroveStringMapper.Entry get(Object o) {
//			String s = (String) o;
//			
//			try {
//				PreparedStatement stat = c.prepareStatement("select i, count from entries "
//						+ "where s = ? limit 1");
//				stat.setString(1, s);
//
//				ResultSet rs = stat.executeQuery();
//				rs.next();
//				LinkedEntry e = new LinkedEntry(s, rs.getInt("i"), rs.getInt("count"), this);
//				rs.close();
//				
//				stat.close();
//				
//				return e;
//			} catch (SQLException e1) {
//				throw new RuntimeException(e1);
//			}
//		}
//
//		public boolean isEmpty() {
//			throw new UnsupportedOperationException();
//		}
//
//		public Set<String> keySet() {
//			throw new UnsupportedOperationException();
//		}
//
//		public org.cleartk.util.collection.TroveStringMapper.Entry put(String s, org.cleartk.util.collection.TroveStringMapper.Entry e) {
//			if( containsKey(s) )
//				return update(s, e);
//			
//			try {
//				PreparedStatement stat = c.prepareStatement("insert into entries "
//						+ "(s, i, count) "
//						+ "values (?, ?, ?)");
//				stat.setString(1, s);
//				stat.setInt(2, e.i);
//				stat.setInt(3, e.count);
//				stat.execute();
//				stat.close();
//				
//				return e;
//			} catch (SQLException e1) {
//				throw new RuntimeException(e1);
//			}
//		}
//		
//		public org.cleartk.util.collection.TroveStringMapper.Entry update(String s, org.cleartk.util.collection.TroveStringMapper.Entry e) {
//			try {
//				PreparedStatement stat = c.prepareStatement("update entries "
//						+ "set i = ?, count = ? where s = ?");
//				stat.setInt(1, e.i);
//				stat.setInt(2, e.count);
//				stat.setString(3, s);
//				stat.execute();
//				stat.close();
//				
//				return e;
//			} catch (SQLException e1) {
//				throw new RuntimeException(e1);
//			}
//		}
//
//		public void putAll(Map<? extends String, ? extends org.cleartk.util.collection.TroveStringMapper.Entry> m) {
//			throw new UnsupportedOperationException();
//		}
//
//		public org.cleartk.util.collection.TroveStringMapper.Entry remove(Object key) {
//			throw new UnsupportedOperationException();
//		}
//
//		public int size() {
//			throw new UnsupportedOperationException();
//		}
//
//		public Collection<org.cleartk.util.collection.TroveStringMapper.Entry> values() {
//			throw new UnsupportedOperationException();
//		}
//		
//		File filename;
//		Connection c;
//		
//	}
	
}
