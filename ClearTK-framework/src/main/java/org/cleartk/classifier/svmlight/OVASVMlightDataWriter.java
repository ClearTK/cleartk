/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.cleartk.CleartkException;
import org.cleartk.classifier.jar.ClassifierBuilder;
import org.cleartk.classifier.jar.JarDataWriter;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public class OVASVMlightDataWriter extends JarDataWriter<String,Integer,FeatureVector> {

	public OVASVMlightDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);

		// prepare output files
		String allFalseName = "training-data-allfalse.svmlight";
		allFalseFile = getFile(allFalseName);
		allFalseFile.delete();		

		// create the output writers
		allFalseWriter = this.getPrintWriter(allFalseName);
		trainingDataWriters = new TreeMap<Integer,PrintWriter>();
	}

	@Override
	public void writeEncoded(FeatureVector features, Integer outcome) {
		if( ! trainingDataWriters.containsKey(outcome) ) {
			addClass(outcome);
		}
		
		StringBuffer featureString = new StringBuffer();
		for( FeatureVector.Entry entry : features ) {
			featureString.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
		}
		
		StringBuffer output = new StringBuffer();
		output.append("-1");
		output.append(featureString);
		allFalseWriter.println(output);
		
		for( int i : trainingDataWriters.keySet()) {
			output = new StringBuffer();
			if( outcome == i )
				output.append("+1");
			else
				output.append("-1");

			output.append(featureString);
			
			trainingDataWriters.get(i).println(output);
		}
	}
	

	@Override
	public void finish() throws CleartkException{
		super.finish();

		// close and remove all-false file
		allFalseWriter.close();
		allFalseFile.delete();

		// flush and close all training data writers
		for( PrintWriter pw : trainingDataWriters.values() ) {
			pw.flush();
			pw.close();
		}
	}

	public Class<? extends ClassifierBuilder<String>> getDefaultClassifierBuilderClass() {
		return OVASVMlightClassifierBuilder.class;
	}
	

	private void addClass(int label) {
		File newTDFile = getFile(String.format("training-data-%d.svmlight", label));
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
}
