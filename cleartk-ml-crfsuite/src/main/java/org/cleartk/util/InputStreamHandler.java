/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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

package org.cleartk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */


public abstract class InputStreamHandler<T> extends Thread {
	/**
	 * Stream that is read
	 */
	private BufferedReader reader;
	private T buffer=null;

	InputStreamHandler(T buffer ,InputStream stream) {
		this.reader =new BufferedReader(new InputStreamReader(stream));
		this.buffer = buffer;
		start();
	}

	InputStreamHandler(T buffer, BufferedReader reader) {
		this.reader = reader;
		this.buffer = buffer;
		start();
	}
	public T getBuffer(){
		return buffer;
	}

	/**
	 * read data into buffer
	 */
	public void run() {
		try {
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				addToBuffer(nextLine);
			}
		} catch (IOException ioe) {
			UIMAFramework.getLogger(InputStreamHandler.class).log(Level.WARNING,ioe.getMessage());
		}
	}
	
	public abstract void addToBuffer(String line);
	
	public static InputStreamHandler<StringBuffer> getInputStreamAsBufferedString(InputStream stream){
		
		return new InputStreamHandler<StringBuffer>(new StringBuffer(), stream) {
			@Override
			public synchronized  void  addToBuffer(String line) {
				getBuffer().append(line.trim());
			}
		};
	}
	public static InputStreamHandler<List<String>> getInputStreamAsList(InputStream stream){
		
	
		return new InputStreamHandler<List<String>>(new ArrayList<String>(), stream) {
			@Override
			public synchronized void addToBuffer(String line) {
				getBuffer().add(line);
			}
		};
	}
}
