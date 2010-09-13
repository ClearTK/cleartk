/** 
 * Copyright (c) 2009-2010, Regents of the University of Colorado 
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
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.impl.UimaContext_ImplBase;
import org.apache.uima.resource.ConfigurationManager;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.SequentialDataWriterFactory;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2009-2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren, Philipp Wetzler
 * 
 */

public class ViterbiDataWriterFactory<OUTCOME_TYPE> implements SequentialDataWriterFactory<OUTCOME_TYPE>, Initializable {

	public static final String PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES = 
		ConfigurationParameterFactory.createConfigurationParameterName(
				ViterbiDataWriterFactory.class, 
				"outcomeFeatureExtractorNames");
	@ConfigurationParameter(
			mandatory = false, 
			description = "An optional, multi-valued, string parameter that " +
			"specifies which OutcomeFeatureExtractors should be used. " +
			"Each value of this parameter should be the name of a " +
			"class that implements OutcomeFeatureExtractor. One valid " +
			"value that you might use is " +
	"org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor")
	protected String outcomeFeatureExtractorNames[];


	public static final String PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS = 
		ConfigurationParameterFactory.createConfigurationParameterName(
				ViterbiDataWriterFactory.class, 
				"delegatedDataWriterFactoryClass");
	@ConfigurationParameter(
			mandatory = true,
			description = "A single, required, string parameter that provides " +
			"the full name of the DataWriterFactory class that will be " +
	"wrapped.")
	protected String delegatedDataWriterFactoryClass;


	public static final String PARAM_OUTPUT_DIRECTORY = 
		ConfigurationParameterFactory.createConfigurationParameterName(
				ViterbiDataWriterFactory.class, 
				"outputDirectory");
	@ConfigurationParameter(
			mandatory = true, 
			description = "provides the name of the directory where the " +
	"training data will be written.")
	protected File outputDirectory;


	public void initialize(UimaContext context) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, context);

		try {
			OutcomeFeatureExtractor outcomeFeatureExtractors[];
			if (outcomeFeatureExtractorNames == null) {
				outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
			} else {
				outcomeFeatureExtractors = new OutcomeFeatureExtractor[outcomeFeatureExtractorNames.length];
				for (int i = 0; i < outcomeFeatureExtractorNames.length; i++) {
					outcomeFeatureExtractors[i] = InitializableFactory.create(context, outcomeFeatureExtractorNames[i], OutcomeFeatureExtractor.class);
				}
			}

			dataWriter = 
				new ViterbiDataWriter<OUTCOME_TYPE>(
						outputDirectory,
						outcomeFeatureExtractors);

			UimaContext_ImplBase contextImpl = (UimaContext_ImplBase) context;
			ConfigurationManager c = contextImpl.getConfigurationManager();
			c.setConfigParameterValue(contextImpl.getQualifiedContextName() + JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, dataWriter.getDelegatedModelDirectory().toString());
			
			JarDataWriterFactory<?, OUTCOME_TYPE, ?> delegatedDataWriterFactory = 
				createDelegatedDataWriterFactory(delegatedDataWriterFactoryClass, context);
			
			DataWriter<OUTCOME_TYPE> delegatedDataWriter = 
				delegatedDataWriterFactory.createDataWriter();
			dataWriter.setDelegatedDataWriter(delegatedDataWriter);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		} catch (CleartkException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public SequentialDataWriter<OUTCOME_TYPE> createSequentialDataWriter() throws IOException {
		return dataWriter;
	}
	
	@SuppressWarnings("unchecked")
	private JarDataWriterFactory<?, OUTCOME_TYPE, ?> createDelegatedDataWriterFactory(
			String delegatedDataWriterFactoryClass,
			UimaContext context) throws ResourceInitializationException {
		return InitializableFactory.create(context,
				delegatedDataWriterFactoryClass, 
				JarDataWriterFactory.class);
	}

	private ViterbiDataWriter<OUTCOME_TYPE> dataWriter;
}
