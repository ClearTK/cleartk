package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.jar.Attributes;

import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public abstract class LIBSVMDataWriter<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> extends DataWriter_ImplBase<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE,FeatureVector> {

	public LIBSVMDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);

		// set up files
		File trainingDataFile = getFile("training-data.libsvm");
		trainingDataFile.delete();

		// set up writer
		trainingDataWriter = new PrintWriter(trainingDataFile);

		// set manifest attributes for classifier
		Map<String, Attributes> entries = classifierManifest.getEntries();
		if (!entries.containsKey(LIBSVMClassifier.ATTRIBUTES_NAME)) {
			entries.put(LIBSVMClassifier.ATTRIBUTES_NAME, new Attributes());
		}
		Attributes attributes = entries.get(LIBSVMClassifier.ATTRIBUTES_NAME);
		attributes.putValue(
				LIBSVMClassifier.SCALE_FEATURES_KEY,
				LIBSVMClassifier.SCALE_FEATURES_VALUE_NORMALIZEL2);
	}

	@Override
	public void writeEncoded(FeatureVector features, OUTPUTOUTCOME_TYPE outcome) {
		String classString = encode(outcome);
				
		StringBuffer output = new StringBuffer();
		
		output.append(classString);		
		
		for( FeatureVector.Entry entry : features ) {
			output.append(" ");
			output.append(entry.index);
			output.append(":");
			output.append(entry.value);
		}
		
		trainingDataWriter.println(output);
	}

	@Override
	public void finish() throws IOException {
		super.finish();

		// flush and close writer
		trainingDataWriter.flush();
		trainingDataWriter.close();
	}

	public abstract Class<? extends ClassifierBuilder<INPUTOUTCOME_TYPE>> getDefaultClassifierBuilderClass();

	protected abstract String encode(OUTPUTOUTCOME_TYPE outcome);

	private PrintWriter trainingDataWriter;
	
}
