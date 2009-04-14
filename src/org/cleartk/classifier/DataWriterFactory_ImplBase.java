package org.cleartk.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.UIMAUtil;

public abstract class DataWriterFactory_ImplBase implements DataWriterFactory {

	public static final String PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM = "org.cleartk.classifier.DataWriterFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM";

	public void initialize(UimaContext context) {
		boolean loadEncoders = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(
				context, DataWriterFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, false);
		if (loadEncoders) {
			try {
				String outputDirectory = (String)UIMAUtil.getRequiredConfigParameterValue(
						context, DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
				File encoderFile = new File(
						outputDirectory, FeaturesEncoder_ImplBase.ENCODER_FILE_NAME);
				
				if (!encoderFile.exists()) {
					throw new RuntimeException(String.format(
							"No encoder found in directory %s", outputDirectory));
				}
				
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(encoderFile));
				this.featuresEncoder = (FeaturesEncoder<?>) is.readObject();
				this.featuresEncoder.allowNewFeatures(false);
				this.outcomeEncoder = (OutcomeEncoder<?,?>) is.readObject();
				is.close();
			} catch (Exception e) {
				// FIXME: improve exception handling
				throw new RuntimeException(e);
			}
		} else {
			this.featuresEncoder = null;
			this.outcomeEncoder = null;
		}
		this.context = context;
	}

	public abstract DataWriter<?> createDataWriter(File outputDirectory) throws IOException;
	
	protected FeaturesEncoder<?> getFeaturesEncoder() {
		return this.featuresEncoder;		
	}

	protected OutcomeEncoder<?, ?> getOutcomeEncoder() {
		return this.outcomeEncoder;
	}
	
	protected UimaContext context = null;
	protected FeaturesEncoder<?> featuresEncoder = null;
	protected OutcomeEncoder<?,?> outcomeEncoder = null;
}
