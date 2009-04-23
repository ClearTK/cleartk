package org.cleartk.classifier.opennlp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory_ImplBase;
import org.cleartk.classifier.encoder.factory.NameNumberEncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

public class DefaultMaxentDataWriterFactory extends DataWriterFactory_ImplBase {

	@Override
	public void initialize(UimaContext context) {
		NameNumberEncoderFactory nnef = new NameNumberEncoderFactory();
		featuresEncoder = nnef.createFeaturesEncoder(context);
		featuresEncoder.allowNewFeatures(true);
		outcomeEncoder = nnef.createOutcomeEncoder(context);
	}
	
	@Override
	public DataWriter<?> createDataWriter(File outputDirectory) throws IOException {
		MaxentDataWriter mdw = new MaxentDataWriter(outputDirectory);
		mdw.setFeaturesEncoder((FeaturesEncoder<List<NameNumber>>)getFeaturesEncoder());
		mdw.setOutcomeEncoder((OutcomeEncoder<String, String>)getOutcomeEncoder());
		return mdw;
	}

}
