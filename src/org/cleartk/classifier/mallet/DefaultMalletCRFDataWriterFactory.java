package org.cleartk.classifier.mallet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.cleartk.Initializable;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.SequentialDataWriterFactory_ImplBase;
import org.cleartk.classifier.encoder.factory.NameNumberEncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

public class DefaultMalletCRFDataWriterFactory extends SequentialDataWriterFactory_ImplBase implements Initializable{

	@Override
	public void initialize(UimaContext context) {
		NameNumberEncoderFactory nnef = new NameNumberEncoderFactory();
		featuresEncoder = nnef.createFeaturesEncoder(context);
		outcomeEncoder = nnef.createOutcomeEncoder(context);
	}
	
	@Override
	public SequentialDataWriter<?> createSequentialDataWriter(File outputDirectory) throws IOException {
		MalletCRFDataWriter mdw = new MalletCRFDataWriter(outputDirectory);
		mdw.setFeaturesEncoder((FeaturesEncoder<List<NameNumber>>)getFeaturesEncoder());
		mdw.setOutcomeEncoder((OutcomeEncoder<String, String>)getOutcomeEncoder());
		return mdw;
	}
}
