package org.cleartk.example.parser;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.CleartkComponents;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.XWriter;
import org.uutuc.util.SimplePipeline;

public class ParserExample {

	public static void main(String[] args) throws Exception {

		String filesDirectory = args[0];
		String outputDirectory = args[1];

		CollectionReader reader = CleartkComponents.createCollectionReader(FilesCollectionReader.class,
				FilesCollectionReader.PARAM_ROOT_FILE, filesDirectory);

		AnalysisEngineDescription sentenceAndTokensDescription = CleartkComponents.createSentencesAndTokens();
		AnalysisEngineDescription posTaggerDescription = OpenNLPPOSTagger.getDescription();
		AnalysisEngineDescription parserDescription = OpenNLPTreebankParser.getDescription();
		AnalysisEngineDescription xWriterDescription = CleartkComponents.createPrimitiveDescription(XWriter.class,
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDirectory);

		SimplePipeline.runPipeline(reader, sentenceAndTokensDescription, posTaggerDescription, parserDescription, xWriterDescription);
	}
}
