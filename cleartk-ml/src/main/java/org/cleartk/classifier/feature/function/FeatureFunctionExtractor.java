/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.feature.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;

import com.google.common.base.Function;

/**
 * <br>
 * Copyright (c) 2007-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class FeatureFunctionExtractor<T extends Annotation> implements FeatureExtractor1<T> {

  public FeatureFunctionExtractor(
      FeatureExtractor1<T> extractor,
      FeatureFunction... featureFunctions) {
    this.extractor = extractor;
    this.featureFunctions = featureFunctions;
  }

  public List<Feature> extract(JCas jCas, T focusAnnotation) throws CleartkExtractorException {
    List<Feature> features = new ArrayList<Feature>();
    List<Feature> baseFeatures = this.extractor.extract(jCas, focusAnnotation);
    features.addAll(baseFeatures);
    for (Function<Feature, List<Feature>> featureFunction : this.featureFunctions) {
      features.addAll(apply(featureFunction, baseFeatures));
    }
    return features;
  }

  public static List<Feature> apply(
      Function<Feature, List<Feature>> featureFunction,
      List<Feature> features) {
    List<Feature> returnValues = new ArrayList<Feature>();
    for (Feature feature : features) {
      returnValues.addAll(featureFunction.apply(feature));
    }
    return returnValues;
  }

  private FeatureExtractor1<T> extractor;

  private FeatureFunction[] featureFunctions;
}
