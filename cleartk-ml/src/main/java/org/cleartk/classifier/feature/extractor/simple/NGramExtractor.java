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
package org.cleartk.classifier.feature.extractor.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.util.AnnotationRetrieval;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */
public class NGramExtractor implements SimpleFeatureExtractor {
	
	public NGramExtractor(
			int n,
			Class<? extends Annotation> annotationClass, 
			SimpleFeatureExtractor subExtractor) {
		this.n = n;
		this.subExtractor = subExtractor;
		this.annotationClass = annotationClass;
	}

	public List<Feature> extract(JCas view, Annotation focusAnnotation)	throws CleartkException {
		List<? extends Annotation> annotations;
		
		List<Feature> ngramFeatures = new ArrayList<Feature>();

		annotations = AnnotationRetrieval.getAnnotations(view, focusAnnotation, annotationClass);

		Map<Annotation, List<Feature>> annotationFeatures = new HashMap<Annotation, List<Feature>>();
		for( Annotation annotation : annotations ) {
			List<Feature> features = this.subExtractor.extract(view, annotation);
			annotationFeatures.put(annotation, features);
		}
		
		for( int i = 0; i < annotations.size() - n + 1; i++ ) {
			List<Annotation> slice = new ArrayList<Annotation>(n);
			for( int j = 0; j < n; j++ ) {
				slice.add(annotations.get(i+j));
			}
			
			
			List<String> values = new ArrayList<String>(n);
			List<String> names = new ArrayList<String>(n + 1);

			for( Annotation annotation : slice ) {
				for( Feature feature : annotationFeatures.get(annotation) ) {
					values.add(feature.getValue().toString());
					names.add(feature.getName());
				}
			}
			
			// create name
			StringBuffer nameBuffer = new StringBuffer();
			for( String name : names ) {
				if( nameBuffer.toString().length() > 0 )
					nameBuffer.append(",");
				nameBuffer.append(name);
			}
			
			String name = String.format("Ngram(%s,%s)", this.annotationClass.getSimpleName(), nameBuffer.toString());

			// create value
			StringBuffer valueBuffer = new StringBuffer();
			for( String subValue : values ) {
				valueBuffer.append(subValue);
				valueBuffer.append(valueSeparator);
			}
			if( valueBuffer.length() > 0 )
				valueBuffer.deleteCharAt(valueBuffer.length()-1);
			String value = valueBuffer.toString();
			
			// add feature
			ngramFeatures.add(new Feature(name, value));
		}
		
		return ngramFeatures;
	}
	
	private SimpleFeatureExtractor subExtractor;
	private int n;
	Class<? extends Annotation> annotationClass;

	private String valueSeparator = "|";

	public String getValueSeparator() {
		return valueSeparator;
	}

	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}
	
}
