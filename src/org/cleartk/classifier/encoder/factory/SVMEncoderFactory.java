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
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.features.FeatureEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.features.featurevector.DefaultBooleanEncoder;
import org.cleartk.classifier.encoder.features.featurevector.DefaultFeaturesEncoder;
import org.cleartk.classifier.encoder.features.featurevector.DefaultNumberEncoder;
import org.cleartk.classifier.encoder.features.featurevector.DefaultStringEncoder;
import org.cleartk.classifier.encoder.features.featurevector.FeatureVectorElement;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public abstract class SVMEncoderFactory implements EncoderFactory {

	public FeaturesEncoder<?> createFeaturesEncoder(UimaContext context) {
		FeatureEncoder<FeatureVectorElement> fe;
		FeaturesEncoder_ImplBase<FeatureVector,FeatureVectorElement> a = new DefaultFeaturesEncoder();

		fe = new DefaultNumberEncoder();
		a.addEncoder(fe);

		fe = new DefaultBooleanEncoder();
		a.addEncoder(fe);
		
		fe = new DefaultStringEncoder();
		a.addEncoder(fe);
		
		return a;
	}

	public abstract OutcomeEncoder<?, ?> createOutcomeEncoder(UimaContext context);
}
