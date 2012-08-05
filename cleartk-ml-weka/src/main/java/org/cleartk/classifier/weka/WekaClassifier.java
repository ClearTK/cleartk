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
package org.cleartk.classifier.weka;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;

import weka.core.Instance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @deprecated Use {@link WekaStringOutcomeClassifier} instead.
 */
@Deprecated
public abstract class WekaClassifier extends Classifier_ImplBase<Instance, String, String> {

  // TODO need to add the Weka model as a parameter
  public WekaClassifier(
      FeaturesEncoder<Instance> featuresEncoder,
      OutcomeEncoder<String, String> outcomeEncoder) throws Exception {
    super(featuresEncoder, outcomeEncoder);
  }

  // TODO no implementation of classify method
  public String classify(List<Feature> features) throws UnsupportedOperationException {
    throw new NotImplementedException();
  }

  // TODO no implementation of the score method
  @Override
  public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults) {
    throw new NotImplementedException();
  }

}
