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
import org.apache.uima.resource.ResourceCreationSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.ClassifierAnnotator;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.InstanceConsumer_ImplBase;
import org.cleartk.classifier.SequentialAnnotationHandler;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialDataWriterFactory;
import org.cleartk.classifier.SequentialInstanceConsumer_ImplBase;
import org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.viterbi.ViterbiDataWriter;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLWriter;
import org.cleartk.corpus.timeml.TreebankAligningAnnotator;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.srl.conll2005.Conll2005GoldAnnotator;
import org.cleartk.srl.conll2005.Conll2005GoldReader;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.util.FilesCollectionReader;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.ConfigurationParameterFactory;
import org.uutuc.factory.ResourceCreationSpecifierFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.factory.ConfigurationParameterFactory.ConfigurationData;

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

	public static CollectionReader createFilesCollectionReader(String fileOrDir) throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, FilesCollectionReader.PARAM_ROOT_FILE, fileOrDir);
	}

	public static CollectionReader createFilesCollectionReaderWithPatterns(String dir, String viewName,
			String... patterns) throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class, TYPE_SYSTEM_DESCRIPTION,
				FilesCollectionReader.PARAM_ROOT_FILE, dir, FilesCollectionReader.PARAM_VIEW_NAME, viewName,
				FilesCollectionReader.PARAM_PATTERNS, patterns);
	}

	public static AnalysisEngineDescription createOpenNLPPOSTagger() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(OpenNLPPOSTagger.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE, getParameterValue(
						OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE, "resources/models/OpenNLP.TagDict.txt"),
				OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE, getParameterValue(OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE,
						"resources/models/OpenNLP.POSTags.English.bin.gz"));
	}

	public static AnalysisEngineDescription createOpenNLPTreebankParser() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(OpenNLPTreebankParser.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE, getParameterValue(
						OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE,
						"resources/models/OpenNLP.Parser.English.Build.bin.gz"),
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE, getParameterValue(
						OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE,
						"resources/models/OpenNLP.Parser.English.Check.bin.gz"),
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE,
				getParameterValue(OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE,
						"resources/models/OpenNLP.Chunker.English.bin.gz"),
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE, getParameterValue(
						OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE, "resources/models/OpenNLP.HeadRules.txt"));

	}

		public static <OUTCOME_TYPE> AnalysisEngineDescription createViterbiDataWriterAnnotator(
			Class<? extends SequentialAnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> delegatedDataWriterFactoryClass, String outputDir,
			Object... configurationParameters) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(SequentialDataWriterAnnotator.class,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, combineParams(configurationParameters,
						SequentialInstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER_NAME, annotationHandlerClass.getName(),
						SequentialDataWriterAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, ViterbiDataWriterFactory.class
								.getName(), SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDir,
						ViterbiDataWriter.PARAM_DELEGATED_DATAWRITER_FACTORY_CLASS, delegatedDataWriterFactoryClass
								.getName(), ViterbiDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTORS,
						new String[] { DefaultOutcomeFeatureExtractor.class.getName() }));
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createClassifierAnnotator(
			Class<? extends AnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass, String classifierJar)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(ClassifierAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER_NAME, annotationHandlerClass.getName(),
				ClassifierAnnotator.PARAM_CLASSIFIER_JAR_PATH, classifierJar);
	}

	public static <OUTCOME_TYPE> AnalysisEngineDescription createDataWriterAnnotator(
			Class<? extends AnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {
		AnalysisEngineDescription aed =  AnalysisEngineFactory.createPrimitiveDescription(DataWriterAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES);
		
		if (dynamicallyLoadedClasses != null) {
			addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}
		if(annotationHandlerClass != null) {
			addConfigurationParameter(aed, InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER_NAME, annotationHandlerClass.getName());
		}
		if(dataWriterFactoryClass != null) {	
			addConfigurationParameter(aed, DataWriterAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, dataWriterFactoryClass.getName());
		}
		if(outputDir != null) {
			addConfigurationParameter(aed, DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDir);
		}
		if (configurationData != null) {
			addConfigurationParameters(aed, configurationData);
		}
		return aed;
	}


	public static <OUTCOME_TYPE> AnalysisEngineDescription createSequentialClassifierAnnotator(
			Class<? extends SequentialAnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass, String classifierJar,
			List<Class<?>> dynamicallyLoadedClasses, Object... configurationData)
			throws ResourceInitializationException {

		AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(SequentialClassifierAnnotator.class, TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES);
		
		if (dynamicallyLoadedClasses != null) {
			addConfigurationParameters(aed, dynamicallyLoadedClasses);
		}
		
		if(annotationHandlerClass != null) {
			addConfigurationParameter(aed, SequentialInstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER_NAME, annotationHandlerClass.getName());
		}
		if(classifierJar != null) {	
			addConfigurationParameter(aed, SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR_PATH, classifierJar);
		}
		if (configurationData != null) {
			addConfigurationParameters(aed, configurationData);
		}
		return aed;
	}

	public static void addConfigurationParameters(ResourceCreationSpecifier specifier, Object... configurationData) {
		ConfigurationData cdata = ConfigurationParameterFactory.createConfigurationData(configurationData);
		ResourceCreationSpecifierFactory.setConfigurationParameters(specifier, cdata.configurationParameters,
				cdata.configurationValues);
	}

	private static void addConfigurationParameters(ResourceCreationSpecifier specifier, List<Class<?>> dynamicallyLoadedClasses) {
		for (Class<?> dynamicallyLoadedClass : dynamicallyLoadedClasses) {
			ConfigurationData reflectedConfigurationData = ConfigurationParameterFactory
					.createConfigurationData(dynamicallyLoadedClass);
			ResourceCreationSpecifierFactory.setConfigurationParameters(specifier,
					reflectedConfigurationData.configurationParameters,
					reflectedConfigurationData.configurationValues);
		}

	}

	private static void addConfigurationParameter(ResourceCreationSpecifier specifier, String name, Object value) {
		ConfigurationData cdata = ConfigurationParameterFactory.createConfigurationData(name, value);
		ResourceCreationSpecifierFactory.setConfigurationParameters(specifier, cdata.configurationParameters,
				cdata.configurationValues);
		
	}
	
	public static <OUTCOME_TYPE> AnalysisEngineDescription createSequentialDataWriterAnnotator(
			Class<? extends SequentialAnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass,
			Class<? extends SequentialDataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass, String outputDir)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(SequentialDataWriterAnnotator.class,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, SequentialInstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER_NAME,
				annotationHandlerClass.getName(), SequentialDataWriterAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				dataWriterFactoryClass.getName(), SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDir);
	}


	public static AnalysisEngineDescription createTimeMLGoldAnnotator(boolean loadTLinks)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(TimeMLGoldAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, loadTLinks);
	}

	public static AnalysisEngineDescription createPlainTextTLINKGoldAnnotator() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(PlainTextTLINKGoldAnnotator.class,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
				getParameterValue(PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
						"http://www.stanford.edu/~bethard/data/timebank-verb-clause.txt"));
	}

	public static AnalysisEngineDescription createTreebankAligningAnnotator(String treeBankDir)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(TreebankAligningAnnotator.class,
				TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, TreebankAligningAnnotator.PARAM_TREEBANK_DIRECTORY_NAME,
				treeBankDir);

	}

	public static AnalysisEngineDescription createTimeMLWriter(String outputDir) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(TimeMLWriter.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES, TimeMLWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDir);
	}

	public static CollectionReader createConll2005GoldReader(String conll2005DataFile)
			throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(Conll2005GoldReader.class, TYPE_SYSTEM_DESCRIPTION,
				Conll2005GoldReader.PARAM_CONLL2005_DATA_FILE, conll2005DataFile);
	}

	public static AnalysisEngineDescription createConll2005GoldAnnotator() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(Conll2005GoldAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TYPE_PRIORITIES);
	}

	public static AnalysisEngineDescription createSentencesAndTokens() throws ResourceInitializationException {
		AnalysisEngineDescription sentences = createPrimitiveDescription(OpenNLPSentenceSegmenter.class);
		AnalysisEngineDescription tokenizer = CleartkComponents.createPrimitiveDescription(TokenAnnotator.class,
				TokenAnnotator.PARAM_WINDOW_TYPE_NAME, org.cleartk.type.Sentence.class.getName());
		return AnalysisEngineFactory.createAggregateDescription(Arrays.asList(sentences, tokenizer), Arrays.asList(
				"SentenceSegmenter", "TokenAnnotator"), TYPE_SYSTEM_DESCRIPTION, TYPE_PRIORITIES, null);
	}

	private static String getParameterValue(String paramName, String defaultValue) {
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
