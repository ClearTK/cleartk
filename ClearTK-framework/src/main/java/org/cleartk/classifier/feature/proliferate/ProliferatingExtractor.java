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
package org.cleartk.classifier.feature.proliferate;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class ProliferatingExtractor implements SimpleFeatureExtractor {
	
	/**
	 * A wrapper for feature extractors and their associated feature proliferators
	 * which invokes the feature proliferators automatically at extraction time.
	 *  
	 * @param extractor     The feature extractor.
	 * @param proliferators Feature proliferators that use the output of the feature
	 *                      extractor to generate their features.
	 */
	public ProliferatingExtractor(
			SimpleFeatureExtractor extractor,
			FeatureProliferator ... proliferators) {
		this.extractor = extractor;
		this.proliferators = proliferators;
	}

	public List<Feature> extract(JCas jCas, Annotation focusAnnotation)
			throws UnsupportedOperationException {
		List<Feature> features = new ArrayList<Feature>();
		List<Feature> baseFeatures = this.extractor.extract(jCas, focusAnnotation);
		features.addAll(baseFeatures);
		for (FeatureProliferator proliferator: this.proliferators) {
			features.addAll(proliferator.proliferate(baseFeatures));
		}
		return features;
	}

	private SimpleFeatureExtractor extractor;
	private FeatureProliferator[] proliferators;
}
