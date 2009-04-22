package org.cleartk.classifier.viterbi;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.SequentialDataWriterFactory;

public class ViterbiDataWriterFactory  implements SequentialDataWriterFactory {

	public SequentialDataWriter<?> createSequentialDataWriter(File outputDirectory) throws IOException {
		return new ViterbiDataWriter(outputDirectory);
	}


	public void initialize(UimaContext context) throws ResourceInitializationException {
		// TODO Auto-generated method stub
		
	}
}
