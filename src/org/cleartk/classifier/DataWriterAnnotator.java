/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.classifier;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

public class DataWriterAnnotator<OUTCOME_TYPE> extends InstanceConsumer_ImplBase<OUTCOME_TYPE> {

	/**
	 * The name of the directory where the training data will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "org.cleartk.classifier.DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY";
	
	public static final String PARAM_DATAWRITER_FACTORY_CLASS = "org.cleartk.classifier.DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS";

	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		String outputDirectoryPath = (String) UIMAUtil.getRequiredConfigParameterValue(context, PARAM_OUTPUT_DIRECTORY);
		File outputDirectory = new File(outputDirectoryPath);

		// create the factory and instantiate the data writer
		DataWriterFactory<?> factory = UIMAUtil.create(
				context, PARAM_DATAWRITER_FACTORY_CLASS, DataWriterFactory.class);
		DataWriter<?> untypedDataWriter;
		try {
			untypedDataWriter = factory.createDataWriter(outputDirectory);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		// check the type of the DataWriter and assign the instance variable
		this.checkOutcomeType(DataWriter.class, "OUTCOME_TYPE", untypedDataWriter);
		this.dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
		UIMAUtil.initialize(this.dataWriter, context);
	}

	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {
			dataWriter.finish();
		}
		catch (CleartkException ctke) {
			throw new AnalysisEngineProcessException(ctke);
		}
	}

	
	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance)  throws CleartkException{
		dataWriter.write(instance);
		return null;
	}

	public boolean expectsOutcomes() {
		return true;
	}
	
	private DataWriter<OUTCOME_TYPE> dataWriter;

}
