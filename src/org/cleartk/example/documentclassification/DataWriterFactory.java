package org.cleartk.example.documentclassification;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.features.TFIDFEncoder;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.encoder.outcome.StringToIntegerOutcomeEncoder;
import org.cleartk.classifier.svmlight.OVASVMlightDataWriter;

public class DataWriterFactory implements org.cleartk.classifier.DataWriterFactory<String> {

	public DataWriter<String> createDataWriter(File outputDirectory) throws IOException {
		OVASVMlightDataWriter dataWriter = new OVASVMlightDataWriter(outputDirectory);

			NameNumberNormalizer normalizer = new EuclidianNormalizer();
			FeatureVectorFeaturesEncoder featuresEncoder = new FeatureVectorFeaturesEncoder(normalizer);
			featuresEncoder.addEncoder(new TFIDFEncoder(new File("example/documentclassification/idfmap")));
			featuresEncoder.addEncoder(new NumberEncoder());
			featuresEncoder.addEncoder(new BooleanEncoder());
			featuresEncoder.addEncoder(new StringEncoder());
			dataWriter.setFeaturesEncoder(featuresEncoder);

			dataWriter.setOutcomeEncoder(new StringToIntegerOutcomeEncoder());

		return dataWriter;
	}

}
