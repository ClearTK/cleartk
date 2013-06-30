/** 
 * Copyright 2011-2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
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
 * Copyright (c) 2011-2012, Technische Universität Darmstadt <br>
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
