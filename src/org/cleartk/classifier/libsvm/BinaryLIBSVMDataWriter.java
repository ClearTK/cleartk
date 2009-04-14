package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.ClassifierBuilder;

public class BinaryLIBSVMDataWriter extends LIBSVMDataWriter<Boolean,Boolean> {

	public BinaryLIBSVMDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);
	}

	@Override
	public Class<? extends ClassifierBuilder<Boolean>> getDefaultClassifierBuilderClass() {
		return BinaryLIBSVMClassifierBuilder.class;
	}

	@Override
	protected String encode(Boolean outcome) {
		Boolean encodedOutcome = this.outcomeEncoder.encode(outcome);
		return encodedOutcome.booleanValue() ? "+1" : "-1";
	}

}
