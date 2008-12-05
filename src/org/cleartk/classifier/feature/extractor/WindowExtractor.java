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
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.AnnotationUtil;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * 
 */

public class WindowExtractor {

	Class<? extends Annotation> featureClass;

	Type featureType;

	SimpleFeatureExtractor featureExtractor;

	String windowOrientation;

	int windowStart;

	int windowEnd;

	protected String name;

	public WindowExtractor(Class<? extends Annotation> featureClass, SimpleFeatureExtractor featureExtractor,
			String windowOrientation, int windowStart, int windowEnd) {
		this.featureClass = featureClass;
		this.featureExtractor = featureExtractor;
		this.windowOrientation = windowOrientation;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
	}

	/**
	 * This feature extractor provides a mechanism for getting features such as:
	 * <ul>
	 * <li>the three words to the left or right of a token</li>
	 * <li>get the three words to the left and right of a constituent bounded
	 * by a sentence.</li>
	 * <li>get the words "inside" the constituent</li>
	 * <li>the three pos tags of the three words to the left of a token</li>
	 * <li>the concept ids of the three normalized named entities to the left
	 * of a consituent inside a paragraph/section annotation.</li>
	 * </ul>
	 * 
	 * An example call to this constructor might look something like this: <br>
	 * <code>
	 * <pre>
	 * 	 new WindowExtractor(Token.class, 
	 * 										 new SpannedTextExtractor(),
	 * 										 WindowExtractor.ORIENTATION_LEFT,
	 * 										 0,3)
	 * 
	 * &#064;param name the name of the extractor
	 * &#064;param featureClass the type of the annotations that we are going to extract a feature from
	 * &#064;param featureExtractor a feature extractor that will extract features from the annotations of type featureType
	 * &#064;param windowOrientation one of ORIENTATION_LEFT, ORIENTATION_RIGHT, ORIENTATION_MIDDLE, ORIENTATION_MIDDLE_REVERSE
	 * &#064;param windowStart the index (inclusive) of the first annotation of type featureType to examine
	 * &#064;param windowEnd the index (exclusive) of the last annotation to examine. 
	 * 
	 * &#064;see WindowFeature
	 * 
	 */
	public WindowExtractor(String name, Class<? extends Annotation> featureClass,
			SimpleFeatureExtractor featureExtractor, String windowOrientation, int windowStart, int windowEnd) {
		this(featureClass, featureExtractor, windowOrientation, windowStart, windowEnd);
		this.name = name;
	}

	public List<Feature> extract(JCas jCas, Annotation focusAnnotation, Class<? extends Annotation> cls) {
		Annotation windowAnnotation = AnnotationRetrieval.getContainingAnnotation(jCas, focusAnnotation, cls);
		return extract(jCas, focusAnnotation, windowAnnotation);
	}

	public List<Feature> extract(JCas jCas, Annotation focusAnnotation, Annotation windowAnnotation) {
		if (this.featureType == null) this.featureType = UIMAUtil.getCasType(jCas, this.featureClass);

		List<Feature> returnValues = new ArrayList<Feature>();

		Annotation startAnnotation = getStartAnnotation(jCas, focusAnnotation);

		FSIterator featureAnnotationIterator = jCas.getAnnotationIndex(featureType).iterator();

		if (startAnnotation != null) {
			featureAnnotationIterator.moveTo(startAnnotation);
		}

		int outOfBoundsDistance = 0;

		for (int i = 0; i < windowEnd; i++) {
			// if start annotation is null, then we will mistakenly create a
			// featureAnnotation for whatever
			// annotation the iterator starts at. Therefore, we check for null
			// here.
			if (outOfBoundsDistance == 0 && startAnnotation != null && featureAnnotationIterator.isValid()) {
				Annotation featureAnnotation = (Annotation) featureAnnotationIterator.get();
				if (isWithinBoundaries(featureAnnotation, focusAnnotation, windowAnnotation)) {
					if (i >= windowStart) {
						returnValues.addAll(extractWindowedFeatures(jCas, i, featureAnnotation));
					}
					moveIterator(featureAnnotationIterator);
					continue;
				}
			}
			outOfBoundsDistance++;
			if (i >= windowStart) {
				Feature feature = new WindowFeature(this.name, null, windowOrientation, i, outOfBoundsDistance);
				returnValues.add(feature);
			}

		}
		return returnValues;
	}

	private boolean isWithinBoundaries(Annotation featureAnnotation, Annotation focusAnnotation,
			Annotation windowAnnotation) {
		if (windowOrientation.equals(WindowFeature.ORIENTATION_LEFT)
				|| windowOrientation.equals(WindowFeature.ORIENTATION_RIGHT)) {
			return AnnotationUtil.contains(windowAnnotation, featureAnnotation);
		}
		else return AnnotationUtil.contains(windowAnnotation, featureAnnotation)
				&& AnnotationUtil.contains(focusAnnotation, featureAnnotation);
	}

	private void moveIterator(FSIterator windowIterator) {
		if (windowOrientation.equals(WindowFeature.ORIENTATION_LEFT)
				|| windowOrientation.equals(WindowFeature.ORIENTATION_MIDDLE_REVERSE)) windowIterator.moveToPrevious();
		else if (windowOrientation.equals(WindowFeature.ORIENTATION_RIGHT)
				|| windowOrientation.equals(WindowFeature.ORIENTATION_MIDDLE)) windowIterator.moveToNext();
	}

	private List<Feature> extractWindowedFeatures(JCas jCas, int i, Annotation annotation) {
		List<Feature> windowedFeatures = featureExtractor.extract(jCas, annotation);
		List<Feature> returnValues = new ArrayList<Feature>();
		if (windowedFeatures != null && windowedFeatures.size() > 0) {
			for (Feature windowedFeature : windowedFeatures) {
				Feature feature = new WindowFeature(this.name, windowedFeature.getValue(), windowOrientation, i,
						windowedFeature, 0);
				returnValues.add(feature);
			}
			return returnValues;
		}

		Feature feature = new WindowFeature(this.name, null, windowOrientation, i, 0);
		return Collections.singletonList(feature);
	}

	/**
	 * This method returns the annotation at index 0 w.r.t. the orientation of
	 * the extractor, the annotation we are extracting features for and the
	 * window the provides the boundaries for feature extraction.
	 * 
	 * @param annotation
	 *            the annotation of focus for which we are extracting features
	 *            for
	 * @return the annotation at index 0:
	 *         <ul>
	 *         <li>for ORIENTATION_LEFT return the annotation adjacent and to
	 *         the left of annotation
	 *         <li>for ORIENTATION_RIGHT return the annotation adjacent and to
	 *         the right of annotation
	 *         <li>for ORIENTATION_MIDDLE return the left most annotation
	 *         completely contained in annotation, null if does not exist
	 *         <li>for ORIENTATION_MIDDLE_REVERSE return the right most
	 *         annotation completely contained in annotation, null if does not
	 *         exist
	 *         </ul>
	 */
	public Annotation getStartAnnotation(JCas jCas, Annotation annotation) {
		if (windowOrientation.equals(WindowFeature.ORIENTATION_LEFT)) {
			return AnnotationRetrieval.getAdjacentAnnotation(jCas, annotation, featureClass, true);
		}
		else if (windowOrientation.equals(WindowFeature.ORIENTATION_RIGHT)) {
			return AnnotationRetrieval.getAdjacentAnnotation(jCas, annotation, featureClass, false);
		}
		else if (windowOrientation.equals(WindowFeature.ORIENTATION_MIDDLE)) {
			return AnnotationRetrieval.getFirstAnnotation(jCas, annotation, featureClass);
		}
		else if (windowOrientation.equals(WindowFeature.ORIENTATION_MIDDLE_REVERSE)) {
			return AnnotationRetrieval.getLastAnnotation(jCas, annotation, featureClass);
		}
		return null;
	}

	public Class<? extends Annotation> getFeatureClass() {
		return featureClass;
	}

	public SimpleFeatureExtractor getFeatureExtractor() {
		return featureExtractor;
	}

	public String getName() {
		return name;
	}

	public int getWindowEnd() {
		return windowEnd;
	}

	public String getWindowOrientation() {
		return windowOrientation;
	}

	public int getWindowStart() {
		return windowStart;
	}

}
