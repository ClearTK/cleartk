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
package org.cleartk.classifier.encoder.factory;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.encoder.EncoderFactory_ImplBase;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NOPNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public abstract class SVMEncoderFactory<OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> extends EncoderFactory_ImplBase<FeatureVector, OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> {
	
	public static final String PARAM_NORMALIZE_VECTORS = "org.cleartk.classifier.encoder.factory.SVMEncoderFactory.PARAM_NORMALIZE_VECTORS";

	@Override
	public FeaturesEncoder<FeatureVector> createFeaturesEncoder(UimaContext context) {
		FeaturesEncoder<FeatureVector> featuresEncoder = super.createFeaturesEncoder(context);
		if (featuresEncoder == null) {

			// create either a default or vector-normalizing features encoder
			FeaturesEncoder_ImplBase<FeatureVector, NameNumber> svmFeaturesEncoder;
			boolean normalizeVectors = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(
					context, SVMEncoderFactory.PARAM_NORMALIZE_VECTORS, false);
			NameNumberNormalizer normalizer;
			if (normalizeVectors) {
				normalizer = new EuclidianNormalizer();
			} else {
				normalizer = new NOPNormalizer();
			}
			svmFeaturesEncoder = new FeatureVectorFeaturesEncoder(normalizer);
	
			// add number, boolean and string encoder
			svmFeaturesEncoder.addEncoder(new NumberEncoder());
			svmFeaturesEncoder.addEncoder(new BooleanEncoder());
			svmFeaturesEncoder.addEncoder(new StringEncoder());
			
			// use the SVM features encoder
			featuresEncoder = svmFeaturesEncoder;
		}
		return featuresEncoder;
	}

	@Override
	public abstract OutcomeEncoder<OUTCOME_IN_TYPE, OUTCOME_OUT_TYPE> createOutcomeEncoder(UimaContext context);
}
