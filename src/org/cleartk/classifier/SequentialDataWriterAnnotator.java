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
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

public class SequentialDataWriterAnnotator<OUTCOME_TYPE> extends SequentialInstanceConsumer_ImplBase<OUTCOME_TYPE> {

	/**
	 * The name of the directory where the training data will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "org.cleartk.classifier.SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY";
	
	public static final String PARAM_DATAWRITER_FACTORY_CLASS = "org.cleartk.classifier.SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS";

	private SequentialDataWriter<OUTCOME_TYPE> sequentialDataWriter;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		String outputDirectoryPath = (String) UIMAUtil.getRequiredConfigParameterValue(context, PARAM_OUTPUT_DIRECTORY);
		File outputDirectory = new File(outputDirectoryPath);

		// create the factory and instantiate the data writer
		SequentialDataWriterFactory<?> factory = UIMAUtil.create(
				context, PARAM_DATAWRITER_FACTORY_CLASS, SequentialDataWriterFactory.class);
		SequentialDataWriter<?> untypedDataWriter;
		try {
			untypedDataWriter = factory.createSequentialDataWriter(outputDirectory);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		// check the type of the DataWriter and assign the instance variable
		this.checkOutcomeType(SequentialDataWriter.class, "OUTCOME_TYPE", untypedDataWriter);
		this.sequentialDataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
		UIMAUtil.initialize(this.sequentialDataWriter, context);
	}

	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {
			sequentialDataWriter.finish();
		}
		catch (CleartkException ctke) {
			throw new AnalysisEngineProcessException(ctke);
		}
	}

	public boolean expectsOutcomes() {
		return true;
	}
	
	public List<OUTCOME_TYPE> consumeSequence(List<Instance<OUTCOME_TYPE>> instances)  throws CleartkException{
			sequentialDataWriter.writeSequence(instances);
			return null;
	}

}
