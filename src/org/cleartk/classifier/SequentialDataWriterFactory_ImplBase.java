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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.UIMAUtil;

public abstract class SequentialDataWriterFactory_ImplBase implements SequentialDataWriterFactory {

	public static final String PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM = "org.cleartk.classifier.SequentialDataWriterFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM";

	public void initialize(UimaContext context)  throws ResourceInitializationException{
		boolean loadEncoders = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(
				context, SequentialDataWriterFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, false);
		if (loadEncoders) {
			try {
				String outputDirectory = (String)UIMAUtil.getRequiredConfigParameterValue(
						context, SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
				File encoderFile = new File(
						outputDirectory, FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME);
				
				if (!encoderFile.exists()) {
					throw new RuntimeException(String.format(
							"No encoder found in directory %s", outputDirectory));
				}
				
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(encoderFile));
				this.featuresEncoder = (FeaturesEncoder<?>) is.readObject();
				this.featuresEncoder.allowNewFeatures(false);
				this.outcomeEncoder = (OutcomeEncoder<?,?>) is.readObject();
				is.close();
			} catch (Exception e) {
				throw new ResourceInitializationException(e);
			}
		} else {
			this.featuresEncoder = null;
			this.outcomeEncoder = null;
		}
		this.context = context;
	}

	public abstract SequentialDataWriter<?> createSequentialDataWriter(File outputDirectory) throws IOException;
	
	protected FeaturesEncoder<?> getFeaturesEncoder() {
		return this.featuresEncoder;		
	}

	protected OutcomeEncoder<?, ?> getOutcomeEncoder() {
		return this.outcomeEncoder;
	}
	
	protected UimaContext context = null;
	protected FeaturesEncoder<?> featuresEncoder = null;
	protected OutcomeEncoder<?,?> outcomeEncoder = null;
}
