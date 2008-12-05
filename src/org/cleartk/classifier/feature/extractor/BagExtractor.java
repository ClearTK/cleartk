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
package org.cleartk.classifier.feature.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.util.AnnotationRetrieval;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * A class for extracting bag-of-words style features. It allows any type of
 * Annotation to serve as the "words", and allows any type of feature extractor
 * to be applied to each of these "words".
 * 
 * @author Steven Bethard
 */
public class BagExtractor implements SimpleFeatureExtractor {

	/**
	 * Extracts bag-of-words style features, running a feature extractor over all
	 * "word" Annotations within the focus Annotation.
	 * 
	 * @param extractorName    A name identifying this extractor, which will be prefixed
	 *                         to the context string of all features extracted.
	 * @param annotationClass  The class of Annotation representing "words".
	 * @param featureExtractor The feature extractor to be applied to each "word".
	 */
	public BagExtractor(
			String extractorName,
			Class<? extends Annotation> annotationClass,
			SimpleFeatureExtractor featureExtractor) {
		this.annotationClass = annotationClass;
		this.simpleAnnotationName = annotationClass.getSimpleName();
		this.featureExtractor = featureExtractor;
		this.extractorName = extractorName;
	}

	/**
	 * Extracts bag-of-words style features, running a feature extractor over all
	 * "word" Annotations within the focus Annotation.
	 * 
	 * @param annotationClass  The class of Annotation representing "words".
	 * @param featureExtractor The feature extractor to be applied to each "word".
	 */
	public BagExtractor(
			Class<? extends Annotation> annotationClass,
			SimpleFeatureExtractor featureExtractor) {
		this(null, annotationClass, featureExtractor);
	}

	public List<Feature> extract(JCas jCas, Annotation focusAnnotation) {
		
		// list for collecting features and set of values seen
		List<Feature> features = new ArrayList<Feature>();
		Set<String> seenValues = new HashSet<String>();

		// get the Type of the chosen Annotation
		for (Annotation ann: AnnotationRetrieval.getAnnotations(
				jCas, focusAnnotation, this.annotationClass)) {
			for (Feature feature: this.featureExtractor.extract(jCas, ann)) {
				String featureValue = feature.getValue().toString();
				if (!seenValues.contains(featureValue)) {
					String featureName = Feature.createName(
							"BagOf",
							this.simpleAnnotationName,
							feature.getName(),
							this.extractorName);
					features.add(new Feature(featureName, featureValue));
					seenValues.add(featureValue);
				}
			}
		}

		// return the collected features
		return features;
	}
	private Class<? extends Annotation> annotationClass;
	private String simpleAnnotationName;
	private SimpleFeatureExtractor featureExtractor;
	private String extractorName;

}
