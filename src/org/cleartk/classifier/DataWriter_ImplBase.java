package org.cleartk.classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

public abstract class DataWriter_ImplBase<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE, FEATURES_TYPE> implements DataWriter<INPUTOUTCOME_TYPE> {

	public DataWriter_ImplBase(File outputDirectory) {
		// Initialize the output directory and list of output writers
		this.outputDirectory = outputDirectory;
		this.writers = new ArrayList<PrintWriter>();

		// Initialize the Manifest
		this.classifierManifest = new ClassifierManifest();
	}

	public void write(Instance<INPUTOUTCOME_TYPE> instance) throws IOException {
		FEATURES_TYPE features = featuresEncoder.encodeAll(instance.getFeatures());
		OUTPUTOUTCOME_TYPE outcome = outcomeEncoder.encode(instance.getOutcome());
		writeEncoded(features, outcome);
	}

	public abstract void writeEncoded(FEATURES_TYPE features, OUTPUTOUTCOME_TYPE outcome);

	public void finish() throws IOException {
		// close out the file writers
		for (PrintWriter writer : this.writers) {
			writer.flush();
			writer.close();
		}

		// serialize the features encoder
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
				getFile(FeaturesEncoder_ImplBase.ENCODER_FILE_NAME)));
		os.writeObject(this.featuresEncoder);
		os.writeObject(this.outcomeEncoder);
		os.close();

		// set manifest values
		try {
			Class<? extends ClassifierBuilder<? extends INPUTOUTCOME_TYPE>> classifierBuilderClass = this
					.getDefaultClassifierBuilderClass();
			this.classifierManifest.setClassifierBuilder(classifierBuilderClass.newInstance());
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		// write the manifest file
		classifierManifest.write(this.outputDirectory);
	}

	public void setFeaturesEncoder(FeaturesEncoder<FEATURES_TYPE> featuresEncoder) {
		this.featuresEncoder = featuresEncoder;
	}

	public void setOutcomeEncoder(OutcomeEncoder<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE> outcomeEncoder) {
		this.outcomeEncoder = outcomeEncoder;
	}

	protected File getFile(String fileName) {
		return new File(this.outputDirectory, fileName);
	}

	protected PrintWriter getPrintWriter(String fileName) throws IOException {
		File file = this.getFile(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		this.writers.add(writer);
		return writer;
	}

	private File outputDirectory;
	private List<PrintWriter> writers;
	protected ClassifierManifest classifierManifest;
	protected FeaturesEncoder<FEATURES_TYPE> featuresEncoder;
	protected OutcomeEncoder<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE> outcomeEncoder;
}
