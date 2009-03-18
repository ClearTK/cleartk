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
package org.cleartk.classifier.svmlight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.factory.MulticlassSVMEncoderFactory;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class OVASVMlightDataWriter extends
		DataWriter_ImplBase<String,Integer,FeatureVector> {

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		// prepare output files
		allFalseFile = new File(outputDir, "training-data-allfalse.svmlight");
		allFalseFile.delete();		

		// create the output writers
		try {
			allFalseWriter = new PrintWriter(allFalseFile);
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
		trainingDataWriters = new TreeMap<Integer,PrintWriter>();
		
		// create the indices
//		classIndex = new StringIndex(1);
	}

	
	public String consume(Instance<String> instance) {
		String outcome = instance.getOutcome();
		
		int encodedOutcome = this.outcomeEncoder.encode(outcome);
		if( ! trainingDataWriters.containsKey(encodedOutcome) ) {
			addClass(encodedOutcome);
		}
		
		FeatureVector featureVector = this.featuresEncoder.encodeAll(instance.getFeatures());
		
		StringBuffer featureString = new StringBuffer();
		for( FeatureVector.Entry entry : featureVector ) {
			featureString.append(String.format(" %d:%.7f", entry.index, entry.value));
		}
		
		StringBuffer output = new StringBuffer();
		output.append("-1");
		output.append(featureString);
		allFalseWriter.println(output);
		
		for( int i : trainingDataWriters.keySet()) {
			output = new StringBuffer();
			if( encodedOutcome == i )
				output.append("+1");
			else
				output.append("-1");

			output.append(featureString);
			
			trainingDataWriters.get(i).println(output);
		}
		
		// training data writer, so no labels to return
		return null;
	}
	
	@Override
	public void collectionProcessComplete()
	throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		// close and remove all-false file
		allFalseWriter.close();
		allFalseFile.delete();

		// flush and close all training data writers
		for( PrintWriter pw : trainingDataWriters.values() ) {
			pw.flush();
			pw.close();
		}
	}
	
	
	private void addClass(int label) {
		File newTDFile = new File(outputDir, String.format("training-data-%d.svmlight", label));
		newTDFile.delete();

		allFalseWriter.flush();
		try {
			copyFile(allFalseFile, newTDFile);
			trainingDataWriters.put(label, new PrintWriter(new BufferedWriter(new FileWriter(newTDFile, true))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private void copyFile(File source, File target) throws IOException {
	     // Create channel on the source
        FileChannel srcChannel = new FileInputStream(source).getChannel();
    
        // Create channel on the destination
        FileChannel dstChannel = new FileOutputStream(target).getChannel();
    
        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    
        // Close the channels
        srcChannel.close();
        dstChannel.close();
    }
	

	private File allFalseFile;
	private PrintWriter allFalseWriter;
	private Map<Integer, PrintWriter> trainingDataWriters;
	
	@Override
	protected Class<? extends ClassifierBuilder<? extends String>> getDefaultClassifierBuilderClass() {
		return OVASVMlightClassifierBuilder.class;
	}

	@Override
	protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
		return MulticlassSVMEncoderFactory.class;
	}

}
