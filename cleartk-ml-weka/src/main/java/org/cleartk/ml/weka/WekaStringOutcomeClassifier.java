/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;
import org.cleartk.ml.jar.Classifier_ImplBase;

import com.google.common.annotations.Beta;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */
@Beta
public class WekaStringOutcomeClassifier extends Classifier_ImplBase<Iterable<Feature>, String, String> {
  private WekaFeaturesEncoder featuresEncoder;
  private WekaNominalFeatureEncoder outcomeEncoder;
  private Attribute outcomeAttribute;
  private Classifier classifier;

  public WekaStringOutcomeClassifier(FeaturesEncoder<Iterable<Feature>> featuresEncoder,
      OutcomeEncoder<String, String> outcomeEncoder, Classifier classifier) throws Exception {
    super(featuresEncoder, outcomeEncoder);
    this.featuresEncoder =  (WekaFeaturesEncoder) featuresEncoder;
    this.outcomeEncoder = (WekaNominalFeatureEncoder) outcomeEncoder;
    this.outcomeAttribute = this.outcomeEncoder.getAttribute();
    this.classifier = classifier;
  }

  public String classify(List<Feature> features) throws UnsupportedOperationException {
    Instances data = featuresEncoder.makeInstances(1, outcomeAttribute, "classify");

    data.add(featuresEncoder.createInstance(features));
    Attribute classAttribute = data.classAttribute();

    //classify the extracted instance.
    Instance wekaInstance = data.instance(0);
    double classifyInstance;
    try {
      classifyInstance = classifier.classifyInstance(wekaInstance);
      wekaInstance.setClassValue(classifyInstance);
      String label = wekaInstance.toString(classAttribute);
      return label;
    } catch (Exception e1) {
      throw new UnsupportedOperationException(e1);
    }
  }

  @Override
  public Map<String, Double> score(List<Feature> features) throws CleartkProcessingException {
    Instances data = featuresEncoder.makeInstances(1, outcomeAttribute, "classify");

    data.add(featuresEncoder.createInstance(features));
    Attribute classAttribute = data.classAttribute();

    //classify the extracted instance.
    Instance wekaInstance = data.instance(0);
    double[] scores;
    try {
      scores = classifier.distributionForInstance(wekaInstance);
      Map<String, Double> results = new HashMap<>();
      for (int i = 0; i < scores.length; i++){
        wekaInstance.setClassValue(i);
        String label = wekaInstance.toString(classAttribute);
        results.put(label, scores[i]);
      }
      return results;
    } catch (Exception e1) {
      throw new UnsupportedOperationException(e1);
    }
  }


}
