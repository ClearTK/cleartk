package org.cleartk.util.collection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

public class IntStringBidiMap extends DualHashBidiMap<Integer, String> implements GenKeyBidiMap<Integer, String>, Writable {

	private static final long serialVersionUID = 3934773868021265596L;

	public IntStringBidiMap(int firstIndex) {
		this.nextIndex = firstIndex;
	}
	
	public IntStringBidiMap() {
		this(1);
	}
	
	public Integer getOrGenerateKey(String value) {
		if( this.containsValue(value) ) {
			return this.getKey(value);
		} else {
			this.put(nextIndex, value);
			return nextIndex++;
		}
	}
	
	public void write(File file) throws IOException {
		Writer writer = new FileWriter(file);
		write(writer);
		writer.close();
	}

	public void write(Writer writer) throws IOException {
		List<Integer> keys = new ArrayList<Integer>(this.keySet());
		Collections.sort(keys);
		for( int key : keys ) {
			writer.append(String.format("%d %s\n", key, this.get(key)));
		}
		
		writer.flush();
	}

	private int nextIndex;

}
