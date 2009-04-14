package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.ClassifierBuilder;

public class MultiClassLIBSVMDataWriter extends LIBSVMDataWriter<String,Integer> {

	public MultiClassLIBSVMDataWriter(File outputDirectory) throws IOException {
		super(outputDirectory);
	}

	@Override
	public Class<? extends ClassifierBuilder<String>> getDefaultClassifierBuilderClass() {
		return MultiClassLIBSVMClassifierBuilder.class;
	}

	@Override
	protected String encode(Integer outcome) {
		return outcome.toString();
	}

}
