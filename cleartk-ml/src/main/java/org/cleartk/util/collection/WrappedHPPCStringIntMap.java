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
