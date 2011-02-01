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

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.WindowNGramFeature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.AnnotationUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 *         This class may change significantly per issue 19 as described here:
 *         http://code.google.com/p/cleartk/issues/detail?id=19
 *         <p>
 *         This class is very similar to the WindowExtractor in the ClearTK project, except that
 *         instead of getting individual features for each index in the window (as WindowExtractor
 *         does) it creates a single feature that represents an n-gram for all the indexes in the
 *         window. As an example, a window extractor that obtains the three words to the left of a
 *         focus word would retrieve three annotations following sentence for the word "in": <br>
 *         The blue chair in the corner <br>
 *         The three features would be "The (left 3)", "blue (left 2)", "chair (left 1)". The ngram
 *         extractor would retrieve a single feature "The blue chair" - which is the 3-gram
 *         preceding "in".
 *         <p>
 *         This extractor has a vastly different purpose than the CharacterNGramProliferator which
 *         is used to take string features and create prefix/suffix features.
 */

public class WindowNGramExtractor {

  Class<? extends Annotation> featureClass;

  Type featureType;

  SimpleFeatureExtractor featureExtractor;

  String orientation;

  String direction;

  String separator;

  int start;

  int end;

  protected String name;

  public WindowNGramExtractor(
      Class<? extends Annotation> featureClass,
      SimpleFeatureExtractor featureExtractor,
      String ngramOrientation,
      String ngramDirection,
      String ngramSeparator,
      int ngramStart,
      int ngramEnd) {
    this.featureClass = featureClass;
    this.featureExtractor = featureExtractor;
    this.orientation = ngramOrientation;
    this.direction = ngramDirection;
    this.separator = ngramSeparator;
    this.start = ngramStart;
    this.end = ngramEnd;
  }

  /**
   * This feature extractor provides a mechanism for getting features such as:
   * <ul>
   * <li>the word trigram to the left or right of a token.</li>
   * <li>get word trigram to the left and right of a constituent bounded by a sentence.</li>
   * <li>get the n-grams "inside" a constituent</li>
   * <li>the pos tag trigram of the three words to the left of a token</li>
   * </ul>
   * 
   * @param name
   *          the name of the extractor
   * @param featureClass
   *          the type of the annotations that we are going to extract a feature from
   * @param featureExtractor
   *          a feature extractor that will extract features from the annotations of type
   *          featureType
   * @param ngramOrientation
   *          one of ORIENTATION_LEFT, ORIENTATION_RIGHT, ORIENTATION_MIDDLE,
   *          ORIENTATION_MIDDLE_REVERSE
   * @param ngramStart
   *          the index (inclusive) of the first annotation of type featureType to examine
   * @param ngramEnd
   *          the index (exclusive) of the last annotation to examine.
   * 
   * @see WindowNGramFeature
   * 
   */
  public WindowNGramExtractor(
      String name,
      Class<? extends Annotation> featureClass,
      SimpleFeatureExtractor featureExtractor,
      String ngramOrientation,
      String ngramDirection,
      String ngramSeparator,
      int ngramStart,
      int ngramEnd) {
    this(
        featureClass,
        featureExtractor,
        ngramOrientation,
        ngramDirection,
        ngramSeparator,
        ngramStart,
        ngramEnd);
    this.name = name;
  }

  public Feature extract(JCas jCas, Annotation focusAnnotation, Class<? extends Annotation> cls)
      throws CleartkException {
    Annotation ngramAnnotation = AnnotationRetrieval.getContainingAnnotation(
        jCas,
        focusAnnotation,
        cls);
    return extract(jCas, focusAnnotation, ngramAnnotation);
  }

  public Feature extract(JCas jCas, Annotation focusAnnotation, Annotation ngramAnnotation)
      throws CleartkException {
    if (this.featureType == null)
      this.featureType = UIMAUtil.getCasType(jCas, this.featureClass);

    List<String> ngramValues = new ArrayList<String>();

    Annotation startAnnotation = getStartAnnotation(jCas, focusAnnotation);

    FSIterator<Annotation> featureAnnotationIterator = jCas
        .getAnnotationIndex(featureType)
        .iterator();

    if (startAnnotation != null) {
      featureAnnotationIterator.moveTo(startAnnotation);
    }

    int outOfBoundsDistance = 0;

    List<Feature> windowedFeatures = new ArrayList<Feature>();
    for (int i = 0; i < end; i++) {
      // if start annotation is null, then we will mistakenly create a
      // featureAnnotation for whatever
      // annotation the iterator starts at. Therefore, we check for null
      // here.
      if (outOfBoundsDistance == 0 && startAnnotation != null
          && featureAnnotationIterator.isValid()) {
        Annotation featureAnnotation = featureAnnotationIterator.get();
        if (isWithinBoundaries(featureAnnotation, focusAnnotation, ngramAnnotation)) {
          if (i >= start) {
            Feature ngrammedFeature = extactNGrammedFeature(jCas, i, featureAnnotation);
            if (ngrammedFeature != null) {
              ngramValues.add(ngrammedFeature.getValue().toString());
              windowedFeatures.add(ngrammedFeature);
            } else
              ngramValues.add("NULL");

          }
          moveIterator(featureAnnotationIterator);
          continue;
        }
      }
      outOfBoundsDistance++;
      if (i >= start) {
        ngramValues.add("OOB" + outOfBoundsDistance);
      }
    }

    StringBuffer featureValue = new StringBuffer();
    if ((direction.equals(WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT) && orientation
        .equals(WindowNGramFeature.ORIENTATION_RIGHT))
        || (direction.equals(WindowNGramFeature.DIRECTION_RIGHT_TO_LEFT) && orientation
            .equals(WindowNGramFeature.ORIENTATION_LEFT))) {
      for (int i = 0; i < ngramValues.size(); i++) {
        featureValue.append(ngramValues.get(i));
        if (i < ngramValues.size() - 1)
          featureValue.append(separator);
      }
    } else if ((direction.equals(WindowNGramFeature.DIRECTION_RIGHT_TO_LEFT) && orientation
        .equals(WindowNGramFeature.ORIENTATION_RIGHT))
        || (direction.equals(WindowNGramFeature.DIRECTION_LEFT_TO_RIGHT) && orientation
            .equals(WindowNGramFeature.ORIENTATION_LEFT))) {
      for (int i = ngramValues.size() - 1; i >= 0; i--) {
        featureValue.append(ngramValues.get(i));
        if (i > 0)
          featureValue.append(separator);
      }
    }
    WindowNGramFeature windowNGramFeature = new WindowNGramFeature(
        null,
        featureValue.toString(),
        orientation,
        direction,
        separator,
        end - start,
        start,
        windowedFeatures);
    return windowNGramFeature;
  }

  private boolean isWithinBoundaries(
      Annotation featureAnnotation,
      Annotation focusAnnotation,
      Annotation ngramAnnotation) {
    if (orientation.equals(WindowNGramFeature.ORIENTATION_LEFT)
        || orientation.equals(WindowNGramFeature.ORIENTATION_RIGHT)) {
      return AnnotationUtil.contains(ngramAnnotation, featureAnnotation);
    } else
      return AnnotationUtil.contains(ngramAnnotation, featureAnnotation)
          && AnnotationUtil.contains(focusAnnotation, featureAnnotation);
  }

  private void moveIterator(FSIterator<Annotation> ngramIterator) {
    if (orientation.equals(WindowNGramFeature.ORIENTATION_LEFT)
        || orientation.equals(WindowNGramFeature.ORIENTATION_MIDDLE_REVERSE))
      ngramIterator.moveToPrevious();
    else if (orientation.equals(WindowNGramFeature.ORIENTATION_RIGHT)
        || orientation.equals(WindowNGramFeature.ORIENTATION_MIDDLE))
      ngramIterator.moveToNext();
  }

  private Feature extactNGrammedFeature(JCas jCas, int i, Annotation annotation)
      throws CleartkException {
    List<Feature> ngramedFeatures = featureExtractor.extract(jCas, annotation);

    if (ngramedFeatures != null && ngramedFeatures.size() > 0) {
      return ngramedFeatures.get(0);
    }
    return null;
  }

  /**
   * This method returns the annotation at index 0 w.r.t. the orientation of the extractor, the
   * annotation we are extracting features for and the ngram the provides the boundaries for feature
   * extraction.
   * 
   * @param annotation
   *          the annotation of focus for which we are extracting features for
   * @return the annotation at index 0:
   *         <ul>
   *         <li>for ORIENTATION_LEFT return the annotation adjacent and to the left of annotation
   *         <li>for ORIENTATION_RIGHT return the annotation adjacent and to the right of annotation
   *         <li>for ORIENTATION_MIDDLE return the left most annotation completely contained in
   *         annotation, null if does not exist
   *         <li>for ORIENTATION_MIDDLE_REVERSE return the right most annotation completely
   *         contained in annotation, null if does not exist
   *         </ul>
   */
  public Annotation getStartAnnotation(JCas jCas, Annotation annotation) {
    if (orientation.equals(WindowNGramFeature.ORIENTATION_LEFT)) {
      if (annotation.getClass().equals(featureClass))
        return annotation;
      else
        return AnnotationRetrieval.getAdjacentAnnotation(jCas, annotation, featureClass, true);
    } else if (orientation.equals(WindowNGramFeature.ORIENTATION_RIGHT)) {
      if (annotation.getClass().equals(featureClass))
        return annotation;
      else
        return AnnotationRetrieval.getAdjacentAnnotation(jCas, annotation, featureClass, false);
    } else if (orientation.equals(WindowNGramFeature.ORIENTATION_MIDDLE)) {
      return AnnotationRetrieval.getFirstAnnotation(jCas, annotation, featureClass);
    } else if (orientation.equals(WindowNGramFeature.ORIENTATION_MIDDLE_REVERSE)) {
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

  public int getEnd() {
    return end;
  }

  public String getOrientation() {
    return orientation;
  }

  public int getStart() {
    return start;
  }

}
