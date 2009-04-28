 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * Loads FeaturesEncoder and OutcomeEncoder objects from disk if the
 * PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM parameter is set to true.
 * Otherwise, the returned encoders will be null, a signal to subclasses that
 * the encoders were not loaded and they should be created by the subclasses.
 *
 * @author Steven Bethard, Philipp Wetzler
 */
public abstract class EncoderFactory_ImplBase<FEATURES_OUT_TYPE, OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> implements EncoderFactory {
	
	public static final String PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM = "org.cleartk.classifier.encoder.EncoderFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM";

	public FeaturesEncoder<FEATURES_OUT_TYPE> createFeaturesEncoder(UimaContext context) {
		if (this.context != context) {
			this.initialize(context);
		}
		return this.featuresEncoder;
	}

	public OutcomeEncoder<OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> createOutcomeEncoder(UimaContext context) {
		if (this.context != context) {
			this.initialize(context);
		}
		return this.outcomeEncoder;
	}
	
	protected void initialize(UimaContext context) {
		boolean loadEncoders = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(
				context, EncoderFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, false);
		if (loadEncoders) {
			try {
				String outputDirectory = (String)UIMAUtil.getRequiredConfigParameterValue(
						context, DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
				File encoderFile = new File(
						outputDirectory, FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME);
				
				if (!encoderFile.exists()) {
					throw new RuntimeException(String.format(
							"No encoder found in directory %s", outputDirectory));
				}
				
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(encoderFile));
				this.featuresEncoder = (FeaturesEncoder<FEATURES_OUT_TYPE>) is.readObject();
				this.featuresEncoder.allowNewFeatures(false);
				this.outcomeEncoder = (OutcomeEncoder<OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE>) is.readObject();
				is.close();
			} catch (Exception e) {
				// TODO: improve exception handling
				throw new RuntimeException(e);
			}
		} else {
			this.featuresEncoder = null;
			this.outcomeEncoder = null;
		}
		this.context = context;
	}
	
	protected UimaContext context = null;
	protected FeaturesEncoder<FEATURES_OUT_TYPE> featuresEncoder = null;
	protected OutcomeEncoder<OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> outcomeEncoder = null;
}
