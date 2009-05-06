package org.cleartk.example.documentclassification;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.tfidf.IDFMapWriter;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;

public class BuildIDFMap {

	public static void main(String[] args) throws UIMAException, IOException {
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();

		CollectionReader reader = CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				typeSystemDescription, FilesCollectionReader.PARAM_FILE_OR_DIRECTORY,
				"../ClearTK Data/data/20newsgroups/20news-bydate-train");

		AnalysisEngine sentenceSegmenter = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.sentence.SentenceSegmenter", typeSystemDescription);

		AnalysisEngine tokenizer = AnalysisEngineFactory.createAnalysisEngine(TokenAnnotator.class,
				typeSystemDescription);

		AnalysisEngine stemmer = AnalysisEngineFactory.createAnalysisEngine(SnowballStemmer.class,
				typeSystemDescription);

		AnalysisEngine idfMapWriter = AnalysisEngineFactory.createAnalysisEngine(IDFMapWriter.class,
				typeSystemDescription, args, IDFMapWriter.PARAM_IDFMAP_FILE, "example/documentclassification/idfmap",
				IDFMapWriter.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class);

		JCasIterable jCases = new JCasIterable(reader, sentenceSegmenter, tokenizer, stemmer, idfMapWriter);

		for (@SuppressWarnings("unused")	JCas jCas : jCases) { }

		sentenceSegmenter.collectionProcessComplete();
		tokenizer.collectionProcessComplete();
		stemmer.collectionProcessComplete();
		idfMapWriter.collectionProcessComplete();

	}
}
