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
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philipp Wetzler
 * 
 */
public class CombinedExtractor implements SimpleFeatureExtractor {

	private SimpleFeatureExtractor[] extractors;

	private String name;

	public SimpleFeatureExtractor[] getExtractors() {
		return extractors;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to give to the top level context of all generated
	 *            features.
	 * @param extractors
	 *            the array of sub-extractors to be called by this one.
	 */
	public CombinedExtractor(String name, SimpleFeatureExtractor ... extractors) {
		this(extractors);
		this.name = name;
	}

	/**
	 * This constructor doesn't set the name. Consequently, the extract method
	 * will not change the context name of features generated by the
	 * sub-extractors.
	 * 
	 * @param extractors
	 *            the array of sub-extractors to be called by this one.
	 */
	public CombinedExtractor(SimpleFeatureExtractor ... extractors) {
		this.extractors = extractors;
	}

	/**
	 * Extract features from the <tt>Annotation</tt> using the sub-extractors.
	 * The parameters are passed on as they are.
	 * 
	 * @param jCas
	 * @param focusAnnotation
	 * 
	 * @return the combined list of features generated by the sub-extractors. If
	 *         <tt>name</tt> was set in the constructor, the top-level context
	 *         of all features will have that as their name.
	 */
	public List<Feature> extract(JCas jCas, Annotation focusAnnotation) throws UnsupportedOperationException {
		List<Feature> result = new ArrayList<Feature>();
		for (SimpleFeatureExtractor extractor : this.extractors) {
			result.addAll(extractor.extract(jCas, focusAnnotation));
		}

		if (name != null) {
			for (Feature feature : result) {
				feature.setName(this.name);
			}
		}
		return result;
	}

}
