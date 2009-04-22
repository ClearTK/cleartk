package org.cleartk.classifier.viterbi;

import java.io.File;
import java.util.jar.Attributes;

import org.cleartk.classifier.ClassifierBuilder;

public class  ViterbiClassifierBuilder<OUTCOME_TYPE> implements ClassifierBuilder<OUTCOME_TYPE>{


	private ClassifierBuilder<OUTCOME_TYPE> delegatedClassifierBuilder;
	
	public void buildJar(File dir, String[] args) throws Exception {
		delegatedClassifierBuilder.buildJar(dir, args);
		
	}

	public Class<?> getClassifierClass() {
		return ViterbiClassifier.class;
	}

	public void train(File dir, String[] args) throws Exception {
		delegatedClassifierBuilder.train(dir, args);
	}

}
