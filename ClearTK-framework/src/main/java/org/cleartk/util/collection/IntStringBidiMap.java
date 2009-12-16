/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.bidimap.DualHashBidiMap;

/**
 * <br>Copyright (c) 2007-2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 */
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
