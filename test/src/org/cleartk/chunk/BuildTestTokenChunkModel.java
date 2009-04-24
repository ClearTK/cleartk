package org.cleartk.chunk;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.corpus.genia.GeniaPosGoldReader;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.token.util.Subtokenizer;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;

public class BuildTestTokenChunkModel {

	public static void main(String[] args) throws Exception {
		
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, 
				GeniaPosGoldReader.PARAM_GENIA_CORPUS, "test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml",
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, true);
		
		AnalysisEngine subtokenizer = AnalysisEngineFactory.createAnalysisEngine(TokenAnnotator.class, typeSystemDescription, 
				TokenAnnotator.PARAM_TOKEN_TYPE, Subtoken.class.getName(),
				TokenAnnotator.PARAM_TOKENIZER, Subtokenizer.class.getName());
		 
		AnalysisEngine chunkTokenizerDataWriter = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.token.chunk.ChunkTokenizerDataWriter",
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/token/chunk",
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletCRFDataWriterFactory.class.getName());

		JCasIterable jCases = new JCasIterable(reader, subtokenizer, chunkTokenizerDataWriter);
		
		for(@SuppressWarnings("unused") JCas jCas : jCases) { }
		
		org.cleartk.classifier.Train.main(new String[] {"test/data/token/chunk"});

	}
}
