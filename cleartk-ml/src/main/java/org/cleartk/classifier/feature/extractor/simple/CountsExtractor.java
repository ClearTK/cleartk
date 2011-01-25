/** 
 * Copyright (c) 2009-2010, Regents of the University of Colorado 
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.CleartkRuntimeException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.FeatureCollection;

/**
 * <br>
 * Copyright (c) 2009-2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Wetzler
 */
public class CountsExtractor implements SimpleFeatureExtractor {

  public CountsExtractor(String identifier, SimpleFeatureExtractor subExtractor) {
    this.subExtractor = subExtractor;
    this.identifier = identifier;
  }

  public CountsExtractor(SimpleFeatureExtractor subExtractor) {
    this((String) null, subExtractor);
  }

  public CountsExtractor(
      String identifier,
      Class<? extends Annotation> annotationType,
      SimpleFeatureExtractor subExtractor) {
    this(identifier, new BagExtractor(annotationType, subExtractor));
  }

  public CountsExtractor(
      Class<? extends Annotation> annotationType,
      SimpleFeatureExtractor subExtractor) {
    this(null, annotationType, subExtractor);
  }

  public List<Feature> extract(JCas view, Annotation annotation) throws CleartkException {
    Map<Object, Integer> countsMap = new HashMap<Object, Integer>();
    FeatureName featureName = new FeatureName();

    count(subExtractor.extract(view, annotation), countsMap, featureName);

    Counts frequencies = new Counts(featureName.getFeatureName(), identifier, countsMap);
    Feature feature = new Feature(String.format("Count(%s)", featureName), frequencies);

    return Collections.singletonList(feature);
  }

  private void count(
      Collection<Feature> features,
      Map<Object, Integer> countsMap,
      FeatureName featureName) {
    for (Feature feature : features) {
      if (feature.getValue() instanceof FeatureCollection) {
        FeatureCollection fc = (FeatureCollection) feature.getValue();

        Collection<Feature> subFeatures = fc.getFeatures();
        for (Feature subFeature : subFeatures) {
          subFeature.setName(Feature.createName(feature.getName(), subFeature.getName()));
        }

        count(subFeatures, countsMap, featureName);

        continue;
      }

      featureName.setFeatureName(feature.getName());

      Object o = feature.getValue();
      if (countsMap.containsKey(o))
        countsMap.put(o, countsMap.get(o) + 1);
      else
        countsMap.put(o, 1);
    }
  }

  private static class FeatureName {
    public void setFeatureName(String f) {
      if (featureName != null && !featureName.equals(f))
        throw new CleartkRuntimeException(
            "sub-extractor of CountsExtractor must only extract features of one name");

      featureName = f;
    }

    public String getFeatureName() {
      return featureName;
    }

    private String featureName = null;
  }

  private SimpleFeatureExtractor subExtractor;

  private String identifier;

}
