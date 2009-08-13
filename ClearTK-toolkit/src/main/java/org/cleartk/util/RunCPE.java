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
package org.cleartk.util;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 *
 */

public class RunCPE {

	/**
	 * @param args
	 * @throws ResourceInitializationException 
	 * @throws IOException 
	 * @throws InvalidXMLException 
	 */
	public static void main(String[] args) throws ResourceInitializationException, InvalidXMLException, IOException {
		if( args.length != 1 )
			System.exit(1);
		
		CpeDescription cpeDesc = UIMAFramework.getXMLParser().parseCpeDescription(new XMLInputSource(args[0]));
		      
		final CollectionProcessingEngine mCPE = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
				
		mCPE.addStatusCallbackListener(new StatusCallbackListener() {
			
			private long totalDocuments = 0;
			private long processedDocuments = 0;
			private long interval = 1;
			
			public void entityProcessComplete(CAS arg0, EntityProcessStatus arg1) {
				totalDocuments = mCPE.getProgress()[0].getTotal();
				processedDocuments += 1;
				
				if( processedDocuments % interval == 0 )
					System.err.format("%d / %d\n", processedDocuments, totalDocuments);				

				List<?> exceptions = arg1.getExceptions();
				for (int i = 0; i < exceptions.size(); i++) {
					((Throwable) exceptions.get(i)).printStackTrace();
				}
			 
			}

			public void aborted() {
				System.err.println("Processing aborted");
			}

			public void batchProcessComplete() {
				System.err.println("Batch process complete");
			}

			public void collectionProcessComplete() {
				System.err.println("Collection process complete");
			}

			public void initializationComplete() {
				System.err.println("Initialization complete");
			}

			public void paused() {
				System.err.println("paused...");
			}

			public void resumed() {
				System.err.println("resumed");
			}});

		mCPE.process();
	}

}
