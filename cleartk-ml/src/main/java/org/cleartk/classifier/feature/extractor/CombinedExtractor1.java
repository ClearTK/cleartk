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

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philipp Wetzler
 * 
 */
public class CombinedExtractor1<T extends Annotation> implements FeatureExtractor1<T> {

  // public SimpleFeatureExtractor[] getExtractors() {
  // return extractors;
  // }
  //
  public List<FeatureExtractor1<T>> getExtractors() {
    return this.extractors;

  }

  /**
   * @param extractors
   *          the array of sub-extractors to be called by this one.
   */
  /*
   * public CombinedExtractor(SimpleFeatureExtractor... extractors) { this.extractors = extractors;
   * }
   */

  private CombinedExtractor1() {
    this.extractors = Lists.newArrayList();
  }

  public CombinedExtractor1(List<FeatureExtractor1<T>> extractors) {
    this.extractors = extractors;
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4,
      FeatureExtractor1<T> extractor5) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
    this.extractors.add(extractor5);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4,
      FeatureExtractor1<T> extractor5,
      FeatureExtractor1<T> extractor6) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
    this.extractors.add(extractor5);
    this.extractors.add(extractor6);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4,
      FeatureExtractor1<T> extractor5,
      FeatureExtractor1<T> extractor6,
      FeatureExtractor1<T> extractor7) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
    this.extractors.add(extractor5);
    this.extractors.add(extractor6);
    this.extractors.add(extractor7);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4,
      FeatureExtractor1<T> extractor5,
      FeatureExtractor1<T> extractor6,
      FeatureExtractor1<T> extractor7,
      FeatureExtractor1<T> extractor8) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
    this.extractors.add(extractor5);
    this.extractors.add(extractor6);
    this.extractors.add(extractor7);
    this.extractors.add(extractor8);
  }

  public CombinedExtractor1(
      FeatureExtractor1<T> extractor1,
      FeatureExtractor1<T> extractor2,
      FeatureExtractor1<T> extractor3,
      FeatureExtractor1<T> extractor4,
      FeatureExtractor1<T> extractor5,
      FeatureExtractor1<T> extractor6,
      FeatureExtractor1<T> extractor7,
      FeatureExtractor1<T> extractor8,
      FeatureExtractor1<T> extractor9) {
    this();
    this.extractors.add(extractor1);
    this.extractors.add(extractor2);
    this.extractors.add(extractor3);
    this.extractors.add(extractor4);
    this.extractors.add(extractor5);
    this.extractors.add(extractor6);
    this.extractors.add(extractor7);
    this.extractors.add(extractor8);
    this.extractors.add(extractor9);
  }

  /**
   * Extract features from the <tt>Annotation</tt> using the sub-extractors. The parameters are
   * passed on as they are.
   * 
   * @return the combined list of features generated by the sub-extractors. If <tt>name</tt> was set
   *         in the constructor, the top-level context of all features will have that as their name.
   */
  public List<Feature> extract(JCas view, T focusAnnotation) throws CleartkExtractorException {
    List<Feature> result = new ArrayList<Feature>();
    for (FeatureExtractor1<T> extractor : this.extractors) {
      result.addAll(extractor.extract(view, focusAnnotation));
    }

    return result;
  }

  private List<FeatureExtractor1<T>> extractors;

}
