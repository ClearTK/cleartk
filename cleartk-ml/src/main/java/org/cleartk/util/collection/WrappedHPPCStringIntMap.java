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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.ObjectIntMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class WrappedHPPCStringIntMap implements Map<String, Integer>, Serializable {
	
	private static final long serialVersionUID = -4367165336749739971L;


	public WrappedHPPCStringIntMap() {
		this.map = new ObjectIntOpenHashMap<String>();
	}

	public int size() {
		return this.map.size();
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey((String) key);
	}

	public boolean containsValue(Object value) {
		int i = (Integer) value;
		for( ObjectIntCursor<String> cursor : this.map ) {
			if( cursor.value == i )
				return true;
		}

		return false;
	}

	public Integer get(Object key) {
		return this.map.get((String) key);
	}

	public Integer put(String key, Integer value) {
		Integer rv = null;
		if( this.map.containsKey(key) )
			rv = this.map.get(key);
		
		this.map.put(key, value);
		return rv;
	}

	public Integer remove(Object key) {
		Integer rv = null;
		if( this.map.containsKey((String) key) )
			rv = this.map.get((String) key);

		this.map.remove((String) key);
		return rv;
	}

	public void putAll(Map<? extends String, ? extends Integer> m) {
		for( String key : m.keySet() ) {
			this.map.put(key, m.get(key));
		}
	}

	public void clear() {
		this.map.clear();
	}

	public Set<String> keySet() {
		Set<String> keySet = new HashSet<String>();		
		for( ObjectIntCursor<String> cursor : this.map ) {
			keySet.add(cursor.key);
		}

		return keySet;
	}

	public Collection<Integer> values() {
		Set<Integer> valueSet = new HashSet<Integer>();
		for( ObjectIntCursor<String> cursor : this.map ) {
			valueSet.add(cursor.value);
		}

		return valueSet;		
	}

	public Set<java.util.Map.Entry<String, Integer>> entrySet() {
		Set<java.util.Map.Entry<String, Integer>> entrySet = new HashSet<java.util.Map.Entry<String, Integer>>();
		for( ObjectIntCursor<String> cursor : this.map ) {
			Entry entry = new Entry(cursor.key, cursor.value);
			entrySet.add(entry);
		}

		return entrySet;
	}
	
	private transient ObjectIntMap<String> map;
	
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(this.map.size());
		for( ObjectIntCursor<String> cursor : this.map ) {
			out.writeObject(cursor.key);
			out.writeInt(cursor.value);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.map = new ObjectIntOpenHashMap<String>();
		int size = in.readInt();
		for( int i=0; i<size; i++ ) {
			String key = (String) in.readObject();
			int value = in.readInt();
			this.map.put(key, value);
		}
	}
	
	private class Entry implements java.util.Map.Entry<String, Integer> {
		
		public Entry(String key, int value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return this.key;
		}

		public Integer getValue() {
			return this.value;
		}

		public Integer setValue(Integer value) {
			throw new UnsupportedOperationException();
		}
		
		private String key;
		private int value;
		
	}
	
}
