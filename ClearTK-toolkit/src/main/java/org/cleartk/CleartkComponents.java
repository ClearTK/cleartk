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
package org.cleartk;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.SequentialDataWriterFactory;
import org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.classifier.viterbi.ViterbiDataWriter;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.token.TokenAnnotator;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.ConfigurationParameterFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkComponents {

	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
			.createTypeSystemDescription("org.cleartk.TypeSystem");

	public static TypePriorities TYPE_PRIORITIES = null;

	// TypePrioritiesFactory.createTypePriorities(new String[] {
	// "org.cleartk.corpus.ace2005.type.Document",
	// "org.cleartk.type.SimpleAnnotation",
	// "org.cleartk.type.ContiguousAnnotation",
	// "org.cleartk.type.SplitAnnotation",
	// "org.cleartk.type.Sentence",
	// "org.cleartk.type.Chunk",
	// "org.cleartk.syntax.treebank.type.TopTreebankNode",
	// "org.cleartk.syntax.treebank.type.TreebankNode",
	// "org.cleartk.ne.type.GazetteerNamedEntityMention",
	// "org.cleartk.ne.type.NamedEntityMention",
	// "org.cleartk.ne.type.NamedEntity",
	// "org.cleartk.ne.type.NamedEntityClass",
	// "org.cleartk.srl.type.Predicate",
	// "org.cleartk.srl.type.Argument",
	// "org.cleartk.srl.type.SemanticArgument",
	// "org.cleartk.corpus.timeml.type.Event",
	// "org.cleartk.corpus.timeml.type.Time",
	// "org.cleartk.corpus.timeml.type.TemporalLink",
	// "org.cleartk.corpus.timeml.type.Text",
	// "org.cleartk.type.Token",
	// "org.cleartk.token.chunk.type.Subtoken"
	// });

	public static <OUTCOME_TYPE> AnalysisEngineDescription createViterbiAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> annotatorClass,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> delegatedDataWriterFactoryClass, String outputDir,
			Object... configurationParameters) throws ResourceInitializationException {

		return AnalysisEngineFactory.createPrimitiveDescription(annotatorClass, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, combineParams(configurationParameters,
						CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME, ViterbiDataWriterFactory.class
								.getName(), ViterbiDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDir,
						ViterbiDataWriter.PARAM_DELEGATED_DATAWRITER_FACTORY_CLASS, delegatedDataWriterFactoryClass
								.getName(), ViterbiDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTORS,
						new String[] { DefaultOutcomeFeatureExtractor.class.getName() }));
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass, String classifierJar, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkAnnotator(cleartkAnnotatorClass, classifierJar, null, configurationData);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass, String classifierJar,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkAnnotator(cleartkAnnotatorClass, TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, classifierJar, dynamicallyLoadedClasses, configurationData);

	}
	
	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass, 
			TypeSystemDescription typeSystemDescription,
			TypePriorities typePriorities,
			String classifierJar,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				cleartkAnnotatorClass, typeSystemDescription, typePriorities);

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
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			Object... configurationData) throws ResourceInitializationException {
		return createCleartkAnnotator(cleartkAnnotatorClass, dataWriterFactoryClass, outputDir, null, configurationData);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkAnnotator(cleartkAnnotatorClass, TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, dataWriterFactoryClass, outputDir, dynamicallyLoadedClasses, configurationData);
	}
	
	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkAnnotator(
			Class<? extends CleartkAnnotator<OUTCOME_TYPE>> cleartkAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			TypePriorities typePriorities,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(cleartkAnnotatorClass,
				typeSystemDescription, typePriorities);

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
			String classifierJar, Object... configurationData) throws ResourceInitializationException {
		return createCleartkSequentialAnnotator(sequentialClassifierAnnotatorClass, classifierJar, null,
				configurationData);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			String classifierJar, List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkSequentialAnnotator(sequentialClassifierAnnotatorClass, TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, classifierJar, dynamicallyLoadedClasses,
				configurationData);
	}
	
	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			TypePriorities typePriorities,
			String classifierJar, List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				sequentialClassifierAnnotatorClass, typeSystemDescription, typePriorities);

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
			Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			Object... configurationData) throws ResourceInitializationException {
		return createCleartkSequentialAnnotator(sequentialClassifierAnnotatorClass, dataWriterFactoryClass, outputDir,
				null, configurationData);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		return createCleartkSequentialAnnotator(sequentialClassifierAnnotatorClass, TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, dataWriterFactoryClass, outputDir,
				dynamicallyLoadedClasses, configurationData);

	}
	
	public static <OUTCOME_TYPE> AnalysisEngineDescription createCleartkSequentialAnnotator(
			Class<? extends CleartkSequentialAnnotator<OUTCOME_TYPE>> sequentialClassifierAnnotatorClass,
			TypeSystemDescription typeSystemDescription,
			TypePriorities typePriorities,
			Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
				sequentialClassifierAnnotatorClass, typeSystemDescription, typePriorities);

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


	public static AnalysisEngineDescription createSentencesAndTokens() throws ResourceInitializationException {
		AnalysisEngineDescription sentences = createPrimitiveDescription(OpenNLPSentenceSegmenter.class);
		AnalysisEngineDescription tokenizer = CleartkComponents.createPrimitiveDescription(TokenAnnotator.class,
				TokenAnnotator.PARAM_WINDOW_TYPE_NAME, org.cleartk.type.Sentence.class.getName());
		return AnalysisEngineFactory.createAggregateDescription(Arrays.asList(sentences, tokenizer), Arrays.asList(
				"SentenceSegmenter", "TokenAnnotator"), TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, null);
	}

	public static String getParameterValue(String paramName, String defaultValue) {
		String value = System.getProperty(paramName);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	private static Object[] combineParams(Object[] oldParams, Object... newParams) {
		Object[] combined = new Object[oldParams.length + newParams.length];
		System.arraycopy(oldParams, 0, combined, 0, oldParams.length);
		System.arraycopy(newParams, 0, combined, oldParams.length, newParams.length);
		return combined;
	}

	public static AnalysisEngineDescription createPrimitiveDescription(
			Class<? extends AnalysisComponent> componentClass, Object... configurationData)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(componentClass,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, configurationData);
		aed.getMetaData().setName(componentClass.getSimpleName());
		return aed;
	}

	public static AnalysisEngine createPrimitive(Class<? extends AnalysisComponent> componentClass,
			Object... configurationData) throws ResourceInitializationException {
		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(componentClass,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, configurationData);
		aed.getMetaData().setName(componentClass.getSimpleName());
		return AnalysisEngineFactory.createPrimitive(aed);
	}

	public static CollectionReaderDescription createCollectionReaderDescription(
			Class<? extends CollectionReader> readerClass, Object... configurationData)
			throws ResourceInitializationException {
		CollectionReaderDescription crd = CollectionReaderFactory.createDescription(readerClass,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, configurationData);
		crd.getMetaData().setName(readerClass.getSimpleName());
		return crd;
	}

	public static CollectionReader createCollectionReader(Class<? extends CollectionReader> readerClass,
			Object... configurationData) throws ResourceInitializationException {
		CollectionReaderDescription crd = CollectionReaderFactory.createDescription(readerClass,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, configurationData);
		crd.getMetaData().setName(readerClass.getSimpleName());
		return CollectionReaderFactory.createCollectionReader(crd);
	}

}
