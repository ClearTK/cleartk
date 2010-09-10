 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.mallet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;

import cc.mallet.pipe.Csv2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */

public class InstanceListCreator {

	public static void main(String[] args) throws IOException {
		String inputDataFileName = args[0];
		String outputDataFileName = args[1];
		
		InstanceListCreator instanceListCreator = new InstanceListCreator();
		InstanceList instanceList = instanceListCreator.createInstanceList(new File(inputDataFileName));

		instanceList.save(new File(outputDataFileName));
	}

	
	public InstanceList createInstanceList(File dataFile) throws IOException {

		InstanceList instanceList = new InstanceList (new SerialPipes (new Pipe[] {
				new Target2Label (), new Csv2FeatureVector()}));

		Reader fileReader = new FileReader (dataFile);
		instanceList.addThruPipe (new DataIterator (fileReader));
		fileReader.close();

		return instanceList;
		
	}
	/**
	 * This DataIterator was cut-n-paste from cc.mallet.pipe.iterator.CsvIterator and modified
	 */
	public class DataIterator implements Iterator<Instance> {

		LineNumberReader reader;
		String currentLine;

		public DataIterator (Reader input) 
		{
			this.reader = new LineNumberReader (input);
			try {
				this.currentLine = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException ();
			}
		}

		public Instance next ()
		{
			String data = null;
			String target = null;
			
			int split = currentLine.lastIndexOf(" ");
			data = currentLine.substring(0, split);
			target = currentLine.substring(split+1);
			Instance carrier = new Instance (data, target, null, null);
			
			try {
				this.currentLine = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException ();
			}
			return carrier;
		}

		public boolean hasNext ()	{	return currentLine != null;	}
		
		public void remove () {
			throw new IllegalStateException ("This Iterator<Instance> does not support remove().");
		}


	}

}
