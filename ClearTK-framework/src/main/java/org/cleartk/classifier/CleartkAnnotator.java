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
package org.cleartk.classifier;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.Initializable;
import org.cleartk.test.util.ConfigurationParameterNameFactory;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.util.InitializeUtil;

public abstract class CleartkAnnotator<OUTCOME_TYPE> extends JCasAnnotator_ImplBase implements Initializable{

	public static final String PARAM_OUTPUT_DIRECTORY = ConfigurationParameterNameFactory
			.createConfigurationParameterName(CleartkAnnotator.class, "outputDirectory");

	@ConfigurationParameter(mandatory = false, description = "provides the name of the directory where the training data will be written.")
	private File outputDirectory;

	public static final String PARAM_DATA_WRITER_FACTORY_CLASS_NAME = ConfigurationParameterNameFactory
			.createConfigurationParameterName(CleartkAnnotator.class, "dataWriterFactoryClassName");

	@ConfigurationParameter(mandatory = false, description = "provides the full name of the DataWriterFactory class to be used.")
	private String dataWriterFactoryClassName;

	public static final String PARAM_CLASSIFIER_JAR_PATH = ConfigurationParameterNameFactory
			.createConfigurationParameterName(CleartkAnnotator.class, "classifierJarPath");

	@ConfigurationParameter(mandatory = false, description = "provides the path to the jar file that should be used to instantiate the classifier.")
	private String classifierJarPath;

	protected Classifier<OUTCOME_TYPE> classifier;

	protected DataWriter<OUTCOME_TYPE> dataWriter;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		InitializeUtil.initialize(this, context);

		if (dataWriterFactoryClassName != null) {
			if(outputDirectory == null) {
				throw new ResourceInitializationException(ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {PARAM_OUTPUT_DIRECTORY});
			}
			// create the factory and instantiate the data writer
			DataWriterFactory<?> factory = UIMAUtil
					.create(dataWriterFactoryClassName, DataWriterFactory.class, context);
			DataWriter<?> untypedDataWriter;
			try {
				untypedDataWriter = factory.createDataWriter(outputDirectory);
			}
			catch (IOException e) {
				throw new ResourceInitializationException(e);
			}
			catch (CleartkException e) {
				throw new ResourceInitializationException(e);
			}
			
			UIMAUtil.initialize(untypedDataWriter, context);
			this.dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
		}
		else {

			if(classifierJarPath == null) {
				throw new ResourceInitializationException(ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {PARAM_CLASSIFIER_JAR_PATH});
			}
			Classifier<?> untypedClassifier;
			try {
				untypedClassifier = ClassifierFactory.createClassifierFromJar(classifierJarPath);
			}
			catch (IOException e) {
				throw new ResourceInitializationException(e);
			}

			// check that the Classifier matches the Annotator type
			this.classifier = ReflectionUtil.uncheckedCast(untypedClassifier);
			UIMAUtil.checkTypeParameterIsAssignable(
					CleartkAnnotator.class, "OUTCOME_TYPE", this,
					Classifier.class, "OUTCOME_TYPE", this.classifier);
			UIMAUtil.initialize(this.classifier, context);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		if (isTraining()) {
			try {
				dataWriter.finish();
			}
			catch (CleartkException ctke) {
				throw new AnalysisEngineProcessException(ctke);
			}
		}
	}

	protected boolean isTraining() {
		return dataWriter != null;
	}

}
