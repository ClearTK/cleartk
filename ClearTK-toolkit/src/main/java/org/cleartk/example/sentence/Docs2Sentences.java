package org.cleartk.example.sentence;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.CleartkComponents;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.type.Sentence;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.linewriter.LineWriter;
import org.uutuc.util.SimplePipeline;

public class Docs2Sentences {

	public static void main(String[] args) throws UIMAException, IOException {
		String inputDirectoryName = args[0];
		String outputFileName = args[1];
		
		CollectionReader filesReader = CleartkComponents.createCollectionReader(FilesCollectionReader.class, FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, inputDirectoryName);
		AnalysisEngine sentences = CleartkComponents.createPrimitive(OpenNLPSentenceSegmenter.class);
		AnalysisEngine lineWriter = CleartkComponents.createPrimitive(LineWriter.class, LineWriter.PARAM_OUTPUT_FILE_NAME, outputFileName, LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, Sentence.class.getName());

		SimplePipeline.runPipeline(filesReader, sentences, lineWriter);
	
		
	}
}
