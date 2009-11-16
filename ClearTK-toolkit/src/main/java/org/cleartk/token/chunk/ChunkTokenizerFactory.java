package org.cleartk.token.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.chunk.ChunkerHandler;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;

public class ChunkTokenizerFactory {

	public static AnalysisEngineDescription createChunkTokenizerDescription() throws ResourceInitializationException {
		List<Class<?>> dynamicallyLoadedClasses = new ArrayList<Class<?>>();
		dynamicallyLoadedClasses.add(ChunkerHandler.class);
		dynamicallyLoadedClasses.add(ChunkTokenizerLabeler.class);
		
		return CleartkComponents.createSequentialClassifierAnnotator(ChunkerHandler.class, "CHANGEME", 
				dynamicallyLoadedClasses,
			ChunkerHandler.PARAM_LABELED_ANNOTATION_CLASS_NAME, Subtoken.class.getName(),
			ChunkerHandler.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
			ChunkerHandler.PARAM_CHUNK_LABELER_CLASS_NAME, ChunkTokenizerLabeler.class.getName(),
			ChunkerHandler.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, ChunkTokenizerFeatureExtractor.class.getName(),
			ChunkTokenizerLabeler.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Token.class.getName()
		);
	}
}
