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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.DataWriter_ImplBase;

import com.google.common.annotations.Beta;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         http://weka.wikispaces.com/Creating+an+ARFF+file
 * 
 */
@Beta
public class WekaStringOutcomeDataWriter extends
DataWriter_ImplBase<WekaStringOutcomeClassifierBuilder, Iterable<Feature>, String, String> {

  private final String relationTag;

  List<Iterable<Feature>> instanceFeatures;

  List<String> instanceOutcomes;

  Set<String> outcomeValues;

  public WekaStringOutcomeDataWriter(File outputDirectory, String relationTag) throws IOException {
    super(outputDirectory);
    this.setFeaturesEncoder(new WekaFeaturesEncoder());
    this.setOutcomeEncoder(new WekaNominalFeatureEncoder("outcome", false));


    this.relationTag = relationTag;
    instanceFeatures = new ArrayList<Iterable<Feature>>();
    instanceOutcomes = new ArrayList<String>();
    outcomeValues = new HashSet<String>();
  }

  public WekaStringOutcomeDataWriter(File outputDirectory) throws IOException {
    this(outputDirectory, "cleartk-generated");
  }

  @Override
  public void writeEncoded(Iterable<Feature> features, String outcome) {
    this.instanceFeatures.add(features);
    instanceOutcomes.add(outcome);
    outcomeValues.add(outcome);
  }

  @Override
  public void finish() throws CleartkProcessingException {
    WekaFeaturesEncoder wekaFeatureEncoder = (WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder();
    WekaNominalFeatureEncoder wekaOutcomeEncoder= (WekaNominalFeatureEncoder) this.classifierBuilder.getOutcomeEncoder();

    Attribute outcomeAttribute = wekaOutcomeEncoder.getAttribute();
    Instances instances = wekaFeatureEncoder.makeInstances(instanceFeatures.size(), outcomeAttribute, relationTag);

    for (int i = 0; i < instanceFeatures.size(); i++) {
      Iterable<Feature> features = instanceFeatures.get(i);
      String outcome = instanceOutcomes.get(i);

      SparseInstance instance = wekaFeatureEncoder.createInstance(features);
      wekaOutcomeEncoder.setAttributeValue(instance, outcome);

      instances.add(instance);
    }

    trainingDataWriter.write(instances.toString());
    super.finish();
  }

  @Override
  protected WekaStringOutcomeClassifierBuilder newClassifierBuilder() {
    return new WekaStringOutcomeClassifierBuilder();
  }

}
