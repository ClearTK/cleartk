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
package org.cleartk.classifier.feature.extractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.util.AnnotationRetrieval;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Wetzler
 */
public class CountsExtractor implements SimpleFeatureExtractor {

	public CountsExtractor(
			String extractorName,
			Class<? extends Annotation> annotationClass,
			SimpleFeatureExtractor subExtractor) {
		this.annotationClass = annotationClass;
		this.subExtractor = subExtractor;
		String simpleAnnotationName = annotationClass.getSimpleName();
		this.extractorName = Feature.createName(extractorName, "Bagged" + simpleAnnotationName);
		this.frequenciesMap = new HashMap<Object,Integer>();
	}
	
	public CountsExtractor(
			Class<? extends Annotation> annotationClass,
			SimpleFeatureExtractor subExtractor) {
		this(null, annotationClass, subExtractor);
	}
	
	public List<Feature> extract(JCas jCas, Annotation windowAnnotation)
			throws UnsupportedOperationException {
		String featureName = null;

		for( Annotation annotation : AnnotationRetrieval.getAnnotations(jCas, windowAnnotation, annotationClass) ) {
			for( Feature feature : subExtractor.extract(jCas, annotation) ) {
				if( featureName == null )
					featureName = feature.getName();
				else if( ! featureName.equals(feature.getName()) )
					throw new UnsupportedOperationException("sub-extractor of FrequenciesExtractor must only extract features of one name");

				Object o = feature.getValue();
				if( frequenciesMap.containsKey(o) )
					frequenciesMap.put(o, frequenciesMap.get(o) + 1);
				else
					frequenciesMap.put(o, 1);
			}
		}
		Counts frequencies = new Counts(featureName, frequenciesMap);
		Feature feature = new Feature(extractorName, frequencies);
		
		return Collections.singletonList(feature);
	}

	private Class<? extends Annotation> annotationClass;
	private SimpleFeatureExtractor subExtractor;
	private String extractorName;
	
	private Map<Object,Integer> frequenciesMap;

}
