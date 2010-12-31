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

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkComponents {


	public static <OUTCOME_TYPE> AnalysisEngineDescription createViterbiAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> annotatorClass,
			TypeSystemDescription typeSystemDescription,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> delegatedDataWriterFactoryClass, String outputDir,
			Object... configurationParameters) throws ResourceInitializationException {

		return AnalysisEngineFactory.createPrimitiveDescription(annotatorClass, typeSystemDescription, 
				combineParams(configurationParameters,
						CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME, ViterbiDataWriterFactory.class
								.getName(), ViterbiDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir,
						ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS, delegatedDataWriterFactoryClass
								.getName(), ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
						new String[] { DefaultOutcomeFeatureExtractor.class.getName() }));
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator (
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass, 
					TypeSystemDescription typeSystemDescription, String classifierJar, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkAnnotator(cleartkAnnotatorClass, typeSystemDescription, classifierJar, null, configurationData);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass, 
			TypeSystemDescription typeSystemDescription,
			String classifierJar,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				cleartkAnnotatorClass, typeSystemDescription);

		if (dynamicallyLoadedClasses != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}

		if (classifierJar != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, classifierJar);
		}
		if (configurationData != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, configurationData);
		}
		return aed;

	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(cleartkAnnotatorClass, typeSystemDescription);

		if (dynamicallyLoadedClasses != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}
		if (dataWriterFactoryClass != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
					dataWriterFactoryClass.getName());
		}
		if (outputDir != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir);
		}
		if (configurationData != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, configurationData);
		}
		return aed;
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			String classifierJar, List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				sequentialClassifierAnnotatorClass, typeSystemDescription);

		if (dynamicallyLoadedClasses != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}

		if (classifierJar != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, classifierJar);
		}
		if (configurationData != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, configurationData);
		}
		return aed;
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				sequentialClassifierAnnotatorClass, typeSystemDescription);

		if (dynamicallyLoadedClasses != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}

		if (dataWriterFactoryClass != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME,
					dataWriterFactoryClass.getName());
		}
		if (outputDir != null) {
			ConfigurationParameterFactory.addConfigurationParameter(aed, JarSequentialDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir);
			ConfigurationParameterFactory.addConfigurationParameter(aed, ViterbiDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir);
		}
		if (configurationData != null) {
			ConfigurationParameterFactory.addConfigurationParameters(aed, configurationData);
		}
		return aed;

	}

	private static Object[] combineParams(Object[] oldParams, Object... newParams) {
		Object[] combined = new Object[oldParams.length + newParams.length];
		System.arraycopy(oldParams, 0, combined, 0, oldParams.length);
		System.arraycopy(newParams, 0, combined, oldParams.length, newParams.length);
		return combined;
	}

}
