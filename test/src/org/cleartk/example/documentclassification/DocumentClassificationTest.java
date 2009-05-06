package org.cleartk.example.documentclassification;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.cleartk.classifier.DataWriterAnnotator;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;

public class DocumentClassificationTest {

	@Test
	public void testDataWriterDescriptor() throws UIMAException, IOException {
		AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.documentclassification.DataWriter", DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/documentclassification");
	}
	
	@Test
	public void testGoldAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.documentclassification.GoldAnnotator");
		
	}
	
	@Test
	public void testEvaluatorDescriptor() throws UIMAException, IOException {
		AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.documentclassification.Evaluator");
		
	}

}
