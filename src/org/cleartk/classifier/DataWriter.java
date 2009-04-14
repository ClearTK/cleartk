package org.cleartk.classifier;

import java.io.IOException;

public interface DataWriter<OUTCOME_TYPE> {
	public void write(Instance<OUTCOME_TYPE> instance) throws IOException;
	
	public void finish() throws IOException;
	
	public Class<? extends ClassifierBuilder<OUTCOME_TYPE>> getDefaultClassifierBuilderClass();
	
}
