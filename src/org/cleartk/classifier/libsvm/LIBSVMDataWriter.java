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
package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.jar.Attributes;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.util.featurevector.FeatureVector;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public abstract class LIBSVMDataWriter<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> extends
		DataWriter_ImplBase<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE,FeatureVector> {

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			// set up files
			File trainingDataFile = new File(outputDir, "training-data.libsvm");
			trainingDataFile.delete();

			// set up writer
			trainingDataWriter = new PrintWriter(trainingDataFile);
			
			// set manifest attributes for classifier
			Map<String, Attributes> entries = classifierManifest.getEntries();
			if (!entries.containsKey(LIBSVMClassifier.ATTRIBUTES_NAME)) {
				entries.put(LIBSVMClassifier.ATTRIBUTES_NAME, new Attributes());
			}
			Attributes attributes = entries.get(LIBSVMClassifier.ATTRIBUTES_NAME);
			attributes.putValue(
					LIBSVMClassifier.SCALE_FEATURES_KEY,
					LIBSVMClassifier.SCALE_FEATURES_VALUE_NORMALIZEL2);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public INPUTOUTCOME_TYPE consume(Instance<INPUTOUTCOME_TYPE> instance) {
		String classString = encode(instance.getOutcome());

		FeatureVector featureVector = this.featuresEncoder.encodeAll(instance.getFeatures());
				
		StringBuffer output = new StringBuffer();
		
		output.append(classString);		
		
		for( FeatureVector.Entry entry : featureVector ) {
			output.append(" ");
			output.append(entry.index);
			output.append(":");
			output.append(entry.value);
		}
		
		trainingDataWriter.println(output);

		// training data writer, so no labels to return
		return null;
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		// flush and close writer
		trainingDataWriter.flush();
		trainingDataWriter.close();
	}
	
	protected abstract String encode(INPUTOUTCOME_TYPE outcome);

	private PrintWriter trainingDataWriter;
}
