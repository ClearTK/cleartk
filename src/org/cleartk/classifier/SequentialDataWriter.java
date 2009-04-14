package org.cleartk.classifier;

import java.io.IOException;
import java.util.List;

public interface SequentialDataWriter<OUTCOME_TYPE> {
	
	public void writeSequence(List<Instance<OUTCOME_TYPE>> instance) throws IOException;
	
	public void finish() throws IOException;
	
	public Class<? extends ClassifierBuilder<OUTCOME_TYPE>> getDefaultClassifierBuilderClass();
	
}
