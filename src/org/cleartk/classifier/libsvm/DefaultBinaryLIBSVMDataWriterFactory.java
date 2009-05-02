package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory_ImplBase;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.encoder.outcome.BooleanToBooleanOutcomeEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public class DefaultBinaryLIBSVMDataWriterFactory extends DataWriterFactory_ImplBase<FeatureVector, Boolean, Boolean> {

	public DataWriter<Boolean> createDataWriter(File outputDirectory) throws IOException {
		BinaryLIBSVMDataWriter dataWriter = new BinaryLIBSVMDataWriter(outputDirectory);

		if( this.featuresEncoder != null && this.outcomeEncoder != null ) {
			/* The superclass has been able to load a features encoder
			 * and an outcome encoder from disk. Use those instead of
			 * creating new ones.
			 */
			dataWriter.setFeaturesEncoder(this.featuresEncoder);
			dataWriter.setOutcomeEncoder(this.outcomeEncoder);
		} else {
			/* Create features encoder and outcome encoder from scratch.
			 */
			NameNumberNormalizer normalizer = new EuclidianNormalizer();
			FeatureVectorFeaturesEncoder featuresEncoder = new FeatureVectorFeaturesEncoder(normalizer);
			featuresEncoder.addEncoder(new NumberEncoder());
			featuresEncoder.addEncoder(new BooleanEncoder());
			featuresEncoder.addEncoder(new StringEncoder());
			dataWriter.setFeaturesEncoder(featuresEncoder);

			dataWriter.setOutcomeEncoder(new BooleanToBooleanOutcomeEncoder());
		}

		return dataWriter;
	}

}
