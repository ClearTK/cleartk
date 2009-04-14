package org.cleartk.classifier;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

public class DataWriterAnnotator<OUTCOME_TYPE> extends InstanceConsumer<OUTCOME_TYPE> {

	/**
	 * The name of the directory where the training data will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "OutputDirectory";
	
	public static final String PARAM_DATAWRITER_FACTORY_CLASS = "DataWriterFactoryClass";

	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		String outputDirectoryPath = (String) UIMAUtil.getRequiredConfigParameterValue(context, PARAM_OUTPUT_DIRECTORY);
		File outputDirectory = new File(outputDirectoryPath);

		// Instantiate the data writer
		DataWriterFactory dataWriterFactory = UIMAUtil.create(
				context, PARAM_DATAWRITER_FACTORY_CLASS, DataWriterFactory.class);
		this.dataWriter = ReflectionUtil.uncheckedCast(
				dataWriterFactory.getDataWriter(outputDirectory));
	}

	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {
			dataWriter.finish();
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	
	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance) {
		try {
			dataWriter.write(instance);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public List<?> consumeSequence(List<?> instances) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean expectsOutcomes() {
		return true;
	}
	
	private DataWriter<OUTCOME_TYPE> dataWriter;

}
