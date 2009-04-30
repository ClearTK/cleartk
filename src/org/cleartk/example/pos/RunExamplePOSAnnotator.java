package org.cleartk.example.pos;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;

public class RunExamplePOSAnnotator {

	public static void main(String[] args)  throws Exception {
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();
		
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class, typeSystemDescription, 
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, "example/data/2008_Sichuan_earthquake.txt");

		AnalysisEngine sentenceSegmenter = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.sentence.SentenceSegmenter");
		AnalysisEngine tokenizer = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.TokenAnnotator");
		AnalysisEngine stemmer = AnalysisEngineFactory.createAnalysisEngine(SnowballStemmer.class, typeSystemDescription,
				SnowballStemmer.PARAM_STEMMER_NAME, "English");
		AnalysisEngine posTagger = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.pos.ExamplePOSAnnotator", 
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, "example/model/model.jar");
		AnalysisEngine posWriter= AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.pos.ExamplePOSPlainTextWriter"); 
				
		JCasIterable jCases = new JCasIterable(reader, sentenceSegmenter, tokenizer, stemmer, posTagger, posWriter);
		
		for(@SuppressWarnings("unused") JCas jCas : jCases) { }
		
		posWriter.collectionProcessComplete();
	}
}
