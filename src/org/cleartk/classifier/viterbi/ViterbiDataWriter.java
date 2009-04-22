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
package org.cleartk.classifier.viterbi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.Initializable;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.ClassifierManifest;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

public class ViterbiDataWriter<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE, FEATURES_TYPE> implements
		SequentialDataWriter<INPUTOUTCOME_TYPE>, Initializable {

	public static final String OUTCOME_FEATURE_EXTRACTOR_FILE_NAME = "outcome-features-extractors.ser";

	/**
	 * "org.cleartk.classifier.ViterbiSequentialDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTOR"
	 * is an optional, multi-valued, string parameter that specifies which
	 * OutcomeFeatureExtractors should be used. Each value of this parameter
	 * should be the name of a class that implements
	 * {@link OutcomeFeatureExtractor}. One valid value that you might use is"org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor"
	 * .
	 */
	public static final String PARAM_OUTCOME_FEATURE_EXTRACTOR = "org.cleartk.classifier.ViterbiDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTOR";

	public static final String PARAM_DATAWRITER_FACTORY_CLASS = "org.cleartk.classifier.ViterbiDataWriter.PARAM_DATAWRITER_FACTORY_CLASS";

	public static final Attributes.Name DELEGATED_CLASSIFIER_BUILDER_ATTRIBUTE = new Attributes.Name("delegatedClassifierBuilderClass");

	
	protected DataWriter<INPUTOUTCOME_TYPE> dataWriter;

	private File outputDirectory;

	private OutcomeFeatureExtractor[] outcomeFeatureExtractors;

	public ViterbiDataWriter(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		DataWriterFactory dataWriterFactory = UIMAUtil.create(context, PARAM_DATAWRITER_FACTORY_CLASS,
				DataWriterFactory.class);
		try {
			this.dataWriter = ReflectionUtil.uncheckedCast(dataWriterFactory.createDataWriter(outputDirectory));
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		UIMAUtil.initialize(this.dataWriter, context);

		try {
			String[] outcomeFeatureExtractorNames = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context,
					PARAM_OUTCOME_FEATURE_EXTRACTOR, null);
			if (outcomeFeatureExtractorNames == null) {
				outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
			}
			else {
				outcomeFeatureExtractors = new OutcomeFeatureExtractor[outcomeFeatureExtractorNames.length];
				for (int i = 0; i < outcomeFeatureExtractorNames.length; i++) {
					Class<?> cls = Class.forName(outcomeFeatureExtractorNames[i]);
					Class<? extends OutcomeFeatureExtractor> outcomeFeatureExtractorClass = cls
							.asSubclass(OutcomeFeatureExtractor.class);
					outcomeFeatureExtractors[i] = outcomeFeatureExtractorClass.newInstance();
					outcomeFeatureExtractors[i].initialize(context);
				}
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	public void finish() throws IOException {
		this.dataWriter.finish();

		ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(outputDirectory, OUTCOME_FEATURE_EXTRACTOR_FILE_NAME)));
		os.writeObject(this.outcomeFeatureExtractors);
		os.close();

		try {
			ClassifierManifest classifierManifest = new ClassifierManifest(outputDirectory);
			ClassifierBuilder<?> delegatedClassifierBuilder  = classifierManifest.getClassifierBuilder();
			Attributes attributes = classifierManifest.getMainAttributes();
			attributes.put(DELEGATED_CLASSIFIER_BUILDER_ATTRIBUTE, delegatedClassifierBuilder.getClass().getName());

			Class<? extends ClassifierBuilder<? extends INPUTOUTCOME_TYPE>> classifierBuilderClass = this.getDefaultClassifierBuilderClass();
			classifierManifest.setClassifierBuilder(classifierBuilderClass.newInstance());
			
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public Class<? extends ClassifierBuilder<INPUTOUTCOME_TYPE>> getDefaultClassifierBuilderClass() {
		return (Class<? extends ClassifierBuilder<INPUTOUTCOME_TYPE>>) ViterbiClassifierBuilder.class;
	}

	public void writeSequence(List<Instance<INPUTOUTCOME_TYPE>> instances) throws IOException {
		List<Object> outcomes = new ArrayList<Object>();
		for (Instance<INPUTOUTCOME_TYPE> instance : instances) {
			List<Feature> instanceFeatures = instance.getFeatures();
			for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
				instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
			}
			outcomes.add(instance.getOutcome());
			dataWriter.write(instance);
		}

	}

}
