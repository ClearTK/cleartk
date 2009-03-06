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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.util.ReflectionUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public abstract class Classifier_ImplBase<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE,FEATURES_TYPE> implements Classifier<INPUTOUTCOME_TYPE> {
	
//	protected ClassifierManifest classifierManifest;
	protected FeaturesEncoder<FEATURES_TYPE> featuresEncoder;
	protected OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> outcomeEncoder;
	private OutcomeFeatureExtractor[] outcomeFeatureExtractors;

	
	public Classifier_ImplBase(JarFile modelFile) throws IOException {
		
		// read the manifest
//		classifierManifest = new ClassifierManifest(modelFile);
		
		// de-serialize the encoders
		ZipEntry zipEntry = modelFile.getEntry(FeaturesEncoder_ImplBase.ENCODER_FILE_NAME);
		ObjectInputStream is = new ObjectInputStream(modelFile.getInputStream(zipEntry));
		FeaturesEncoder<?> genericFeaturesEncoder;
		OutcomeEncoder<?,?> genericOutcomeEncoder;
		try {
			genericFeaturesEncoder = (FeaturesEncoder<?>) is.readObject();
			genericOutcomeEncoder = (OutcomeEncoder<?,?>) is.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			is.close();
		}
		this.featuresEncoder = featuresEncoderCast(genericFeaturesEncoder);
		this.featuresEncoder.allowNewFeatures(false);
		this.outcomeEncoder = outcomeEncoderCast(genericOutcomeEncoder);
		
		zipEntry = modelFile.getEntry(DataWriter_ImplBase.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
		if(zipEntry == null) {
			outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
		} else {
			is = new ObjectInputStream(modelFile.getInputStream(zipEntry));
		
			try {
				outcomeFeatureExtractors = (OutcomeFeatureExtractor[]) is.readObject();
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public abstract INPUTOUTCOME_TYPE classify(List<Feature> features);

	public List<INPUTOUTCOME_TYPE> classifySequence(List<List<Feature>> features) {
		List<Object> outcomes = new ArrayList<Object>();
		List<INPUTOUTCOME_TYPE> returnValues = new ArrayList<INPUTOUTCOME_TYPE>();
		for (List<Feature> instanceFeatures : features) {
			for(OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
				instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
			}
			INPUTOUTCOME_TYPE outcome = classify(instanceFeatures);
			outcomes.add(outcome);
			returnValues.add(outcome);
		}
		return returnValues;
	}

	public ScoredValue<INPUTOUTCOME_TYPE> score(List<Feature> features) {
		throw new UnsupportedOperationException("there is no default implementation of the score method.");
	}

	public List<ScoredValue<INPUTOUTCOME_TYPE>> score(List<Feature> features, int maxResults) {
		throw new UnsupportedOperationException("there is no default implementation of the score method.");
	}

	public abstract boolean isSequential();
	
	protected Class<?> getMyTypeArgument(String parameterName) {
		return getTypeArgument(Classifier_ImplBase.class, parameterName, this);
	}
	
	protected  Class<?> getTypeArgument(Class<?> cls, String parameterName, Object instance) {
		Map<String,Type> typeArguments = ReflectionUtil.getTypeArguments(cls, instance);
		Type t = typeArguments.get(parameterName);
		if( t instanceof Class )
			return (Class<?>)t;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	protected FeaturesEncoder<FEATURES_TYPE> featuresEncoderCast(FeaturesEncoder<?> encoder) {
		Class<?> myFEATURESTYPE = getMyTypeArgument("FEATURES_TYPE");
		Class<?> feFEATURESTYPE = getTypeArgument(FeaturesEncoder.class, "FEATURES_TYPE", encoder);
		
		if( myFEATURESTYPE != feFEATURESTYPE )
			throw new ClassCastException();

		return (FeaturesEncoder<FEATURES_TYPE>) encoder;
	}
	
	@SuppressWarnings("unchecked")
	protected OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> outcomeEncoderCast(OutcomeEncoder<?,?> encoder) {
		Class<?> myINPUTOUTCOME_TYPE = getMyTypeArgument("INPUTOUTCOME_TYPE");
		Class<?> oeINPUTOUTCOME_TYPE = getTypeArgument(OutcomeEncoder.class, "INPUTOUTCOME_TYPE", encoder);
		
		if( myINPUTOUTCOME_TYPE != oeINPUTOUTCOME_TYPE )
			throw new ClassCastException();
		
		Class<?> myOUTPUTOUTCOME_TYPE = getMyTypeArgument("OUTPUTOUTCOME_TYPE");
		Class<?> oeOUTPUTOUTCOME_TYPE = getTypeArgument(OutcomeEncoder.class, "OUTPUTOUTCOME_TYPE", encoder);
		
		if( myOUTPUTOUTCOME_TYPE != oeOUTPUTOUTCOME_TYPE )
			throw new ClassCastException();
		
		return (OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE>) encoder;
	}
}
