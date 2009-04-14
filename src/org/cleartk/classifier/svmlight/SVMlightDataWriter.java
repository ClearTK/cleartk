package org.cleartk.classifier.svmlight;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public class SVMlightDataWriter extends DataWriter_ImplBase<Boolean,Boolean,FeatureVector> {

	public SVMlightDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);
		this.outputWriter = getPrintWriter("training-data.svmlight");
	}

	@Override
	public void writeEncoded(FeatureVector features, Boolean outcome) {
		StringBuffer output = new StringBuffer();
		
		if( outcome == null )
			return;
			
		if( outcome.booleanValue() ) {
			output.append("+1");
		} else {
			output.append("-1");
		}

		for( FeatureVector.Entry entry : features ) {
			if( entry.value == 0.0 )
				continue;

			output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
		}

		outputWriter.println(output);
	}

	public Class<? extends ClassifierBuilder<Boolean>> getDefaultClassifierBuilderClass() {
		return SVMlightClassifierBuilder.class;
	}

	private PrintWriter outputWriter;

}
