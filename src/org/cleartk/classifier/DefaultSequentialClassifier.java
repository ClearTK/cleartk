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
package org.cleartk.classifier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.util.ReflectionUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public abstract class DefaultSequentialClassifier<OUTCOME_TYPE> implements SequentialClassifier<OUTCOME_TYPE> {

	protected Classifier<OUTCOME_TYPE> classifier;
	protected OutcomeFeatureExtractor[] outcomeFeatureExtractors;

	public DefaultSequentialClassifier(JarFile modelFile) throws IOException {
		
		classifier = ReflectionUtil.uncheckedCast(ClassifierFactory.createClassifierFromJar(modelFile.getName()));
		
		ZipEntry zipEntry = modelFile.getEntry(DataWriter_ImplBase.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
		if(zipEntry == null) {
			outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
		} else {
			ObjectInputStream is = new ObjectInputStream(modelFile.getInputStream(zipEntry));
		
			try {
				outcomeFeatureExtractors = (OutcomeFeatureExtractor[]) is.readObject();
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features) {
		List<Object> outcomes = new ArrayList<Object>();
		List<OUTCOME_TYPE> returnValues = new ArrayList<OUTCOME_TYPE>();
		for (List<Feature> instanceFeatures : features) {
			for(OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
				instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
			}
			OUTCOME_TYPE outcome = classifier.classify(instanceFeatures);
			outcomes.add(outcome);
			returnValues.add(outcome);
		}
		return returnValues;
	}

	}
