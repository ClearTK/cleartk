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
package org.cleartk.srl.conll2005;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.DocumentUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class Conll2005GoldReader extends CollectionReader_ImplBase {

	public static final String PARAM_CONLL_2005_DATA_FILE = "CoNLL2005DataFile";
										  
	BufferedReader reader;
	
	boolean finished = false;
	
	int documentNumber;
	
	int totalDocuments;
	
	@Override
	public void initialize() throws ResourceInitializationException
	{
		try
		{
			File dataFile = new File((String) getConfigParameterValue(PARAM_CONLL_2005_DATA_FILE));

			InputStream in;
			if( dataFile.getName().endsWith(".gz") )
				in = new GZIPInputStream(new FileInputStream(dataFile));
			else
				in = new FileInputStream(dataFile);
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			totalDocuments = 0;
			do {
				line = reader.readLine();
				while( line != null && line.trim().length() == 0 )
					line = reader.readLine();
				
				if( line == null )
					break;
				
				totalDocuments += 1;
				while( line != null && line.trim().length() > 0 )
					line = reader.readLine();
			} while( line != null );			
			reader.close();
			
			if( dataFile.getName().endsWith(".gz") )
				in = new GZIPInputStream(new FileInputStream(dataFile));
			else
				in = new FileInputStream(dataFile);
			reader = new BufferedReader(new InputStreamReader(in));
			documentNumber = 0;
			
			super.initialize();
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public void getNext(CAS cas) throws IOException, CollectionException {
		try {
			JCas conllView = cas.createView("CoNLL2005View").getJCas();
			
			String lineBuffer;
			StringBuffer docBuffer = new StringBuffer();
			
			lineBuffer = reader.readLine();
			while( lineBuffer != null && lineBuffer.trim().length() == 0 )
				lineBuffer = reader.readLine();
			
			if( lineBuffer == null )
				throw new CollectionException("unexpected end of input", null);
			
			while( lineBuffer != null && lineBuffer.trim().length() != 0 ) {
				docBuffer.append(lineBuffer.trim());
				docBuffer.append("\n");
				lineBuffer = reader.readLine();
			}
			
			documentNumber += 1;
			
			if( documentNumber == totalDocuments )
				finished = true;
			
			conllView.setSofaDataString(docBuffer.toString(), "text/plain");
			DocumentUtil.createDocument(conllView, String.valueOf(documentNumber), String.valueOf(documentNumber) + ".conll05");
		} catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	public void close() throws IOException {
		reader.close();
	}

	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(documentNumber, totalDocuments, Progress.ENTITIES) };
	}

	public boolean hasNext() throws IOException, CollectionException {
		return !finished;
	}

}
