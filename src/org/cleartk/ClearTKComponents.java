package org.cleartk;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.ClassifierAnnotator;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLWriter;
import org.cleartk.corpus.timeml.TreebankAligningAnnotator;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

public class ClearTKComponents {
	
	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION =
		TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem");
	
	public static CollectionReader createFilesCollectionReader(String fileOrDir)
	throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class,
				ClearTKComponents.TYPE_SYSTEM_DESCRIPTION,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, fileOrDir);
	}
	
	public static CollectionReader createFilesCollectionReaderWithPatterns(
			String dir, String viewName, String ... patterns)
	throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, TYPE_SYSTEM_DESCRIPTION,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, dir,
				FilesCollectionReader.PARAM_VIEW_NAME, viewName,
				FilesCollectionReader.PARAM_PATTERNS, patterns);
	}
	
	public static AnalysisEngine createOpenNLPSentenceSegmenter()
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				OpenNLPSentenceSegmenter.class, TYPE_SYSTEM_DESCRIPTION,
				OpenNLPSentenceSegmenter.PARAM_SENTENCE_MODEL_FILE,
				getParameterValue(
						OpenNLPSentenceSegmenter.PARAM_SENTENCE_MODEL_FILE,
						"resources/models/OpenNLP.Sentence.English.bin.gz"));
	}
	
	public static AnalysisEngine createTokenAnnotator()
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				TokenAnnotator.class, TYPE_SYSTEM_DESCRIPTION);

	}
	
	public static AnalysisEngine createSnowballStemmer(String stemmerName)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				SnowballStemmer.class, TYPE_SYSTEM_DESCRIPTION,
				SnowballStemmer.PARAM_STEMMER_NAME, stemmerName);
	}
	
	public static AnalysisEngine createOpenNLPPOSTagger()
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				OpenNLPPOSTagger.class, TYPE_SYSTEM_DESCRIPTION,
				OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE,
				getParameterValue(
						OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE,
						"resources/models/OpenNLP.TagDict.txt"),
				OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE,
				getParameterValue(
						OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE,
						"resources/models/OpenNLP.POSTags.English.bin.gz"));
	}
	
	public static AnalysisEngine createOpenNLPTreebankParser()
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				OpenNLPTreebankParser.class, TYPE_SYSTEM_DESCRIPTION,
				OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE,
				getParameterValue(
						OpenNLPTreebankParser.PARAM_BUILD_MODEL_FILE,
						"resources/models/OpenNLP.Parser.English.Build.bin.gz"),
				OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE,
				getParameterValue(
						OpenNLPTreebankParser.PARAM_CHECK_MODEL_FILE,
						"resources/models/OpenNLP.Parser.English.Check.bin.gz"),
				OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE,
				getParameterValue(
						OpenNLPTreebankParser.PARAM_CHUNK_MODEL_FILE,
						"resources/models/OpenNLP.Chunker.English.bin.gz"),
				OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE,
				getParameterValue(
						OpenNLPTreebankParser.PARAM_HEAD_RULES_FILE,
						"resources/models/OpenNLP.HeadRules.txt"));

	}
	
	public static <OUTCOME_TYPE> AnalysisEngine createDataWriterAnnotator(
			Class<? extends AnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass,
			Class<? extends DataWriterFactory<OUTCOME_TYPE>> dataWriterFactoryClass,
			String outputDir) throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				DataWriterAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				annotationHandlerClass.getName(),
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				dataWriterFactoryClass.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDir);
	}
	
	public static <OUTCOME_TYPE> AnalysisEngine createClassifierAnnotator(
			Class<? extends AnnotationHandler<OUTCOME_TYPE>> annotationHandlerClass,
			String classifierJar) throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				ClassifierAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				annotationHandlerClass.getName(),
				ClassifierAnnotator.PARAM_CLASSIFIER_JAR, classifierJar);
	}
	
	public static AnalysisEngine createTimeMLGoldAnnotator(boolean loadTLinks)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				TimeMLGoldAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, loadTLinks);
	}
	
	public static AnalysisEngine createPlainTextTLINKGoldAnnotator()
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				PlainTextTLINKGoldAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
				getParameterValue(
						PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
						"http://www.stanford.edu/~bethard/data/timebank-verb-clause.txt"));
	}
	
	public static AnalysisEngine createTreebankAligningAnnotator(String treeBankDir)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				TreebankAligningAnnotator.class, TYPE_SYSTEM_DESCRIPTION,
				TreebankAligningAnnotator.PARAM_TREEBANK_DIRECTORY, treeBankDir);

	}
	
	public static AnalysisEngine createTimeMLWriter(String outputDir)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createAnalysisEngine(
				TimeMLWriter.class, TYPE_SYSTEM_DESCRIPTION,
				TimeMLWriter.PARAM_OUTPUT_DIRECTORY, outputDir);

	}

	private static String getParameterValue(String paramName, String defaultValue) {
		String value = System.getProperty(paramName);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

}
