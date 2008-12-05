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
package org.cleartk.classifier.svmlight;

import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cleartk.classifier.Classifier_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class SVMlightClassifier extends Classifier_ImplBase<Boolean,Boolean,FeatureVector> {

	public static final String ATTRIBUTES_NAME = "SVMlight";
	public static final String SCALE_FEATURES_KEY = "scaleFeatures";
	public static final String SCALE_FEATURES_VALUE_NORMALIZEL2 = "normalizeL2";
	
	SVMlightModel model;
	
	public SVMlightClassifier(JarFile modelFile) throws IOException {
		super(modelFile);
		
		ZipEntry modelEntry = modelFile.getEntry("model.svmlight");
		this.model = SVMlightModel.fromInputStream(modelFile.getInputStream(modelEntry));		
	}

	@Override
	public Boolean classify(List<Feature> features) {
		FeatureVector featureVector = featuresEncoder.encodeAll(features);
		
		double prediction = model.evaluate(featureVector);
		boolean encodedResult = (prediction > 0);

		return outcomeEncoder.decode(encodedResult);
	}

	@Override
	public boolean isSequential() {
		return false;
	}
	
}
