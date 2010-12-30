package org.cleartk.examples;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.syntax.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.token.type.Sentence;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

public class ExampleComponents {

	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
	.createTypeSystemDescription("org.cleartk.examples.TypeSystem");
	
	public static AnalysisEngineDescription getSentenceSegmenter() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(OpenNLPSentenceSegmenter.class, TYPE_SYSTEM_DESCRIPTION,
				OpenNLPSentenceSegmenter.PARAM_SENTENCE_TYPE_NAME, Sentence.class.getName(),
				OpenNLPSentenceSegmenter.PARAM_SENTENCE_MODEL_FILE_NAME, "src/main/resources/model/sentence/OpenNLP.Sentence.English.bin.gz" );
	}

}
