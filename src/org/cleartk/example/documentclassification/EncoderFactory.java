package org.cleartk.example.documentclassification;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.TFIDFEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.encoder.outcome.StringToIntegerOutcomeEncoder;

public class EncoderFactory implements org.cleartk.classifier.encoder.EncoderFactory {

	public FeaturesEncoder<?> createFeaturesEncoder(UimaContext context) {
		FeatureVectorFeaturesEncoder featuresEncoder = new FeatureVectorFeaturesEncoder();

		try {
			featuresEncoder.addEncoder(new TFIDFEncoder(new File("example/documentclassification/idfmap")));
		}
		catch (IOException e) {
			// FIXME: this is not how it's supposed to be done -- this method should be allowed to throw exceptions!
			throw new RuntimeException(e);
		}
		
		return featuresEncoder;
	}

	public OutcomeEncoder<?, ?> createOutcomeEncoder(UimaContext context) {
		return new StringToIntegerOutcomeEncoder();
	}

}
