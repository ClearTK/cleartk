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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.jar.DataWriter_ImplBase;

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
    this.setOutcomeEncoder(new StringToStringOutcomeEncoder());
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
    ArrayList<Attribute> attributes = ((WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder()).getWekaAttributes();
    Map<String, Attribute> attributeMap = ((WekaFeaturesEncoder) this.classifierBuilder.getFeaturesEncoder()).getWekaAttributeMap();
    
    // There is a known problem writing Weka SparseInstance objects from datasets that have string
    // attributes. Need to add a (hopefully unique for this dataset!) dummy string value at index 0
    // so that all the real values will have value > 0 and the SparseInstance will write them out.
    // (Note that a SparseInstance writes out the actual string values, not the indexes of those
    // values, so it shouldn't change the data if there's an extra dummy value in the Attribute.)
    // Read more:
    // http://weka.wikispaces.com/Why+am+I+missing+certain+nominal+or+string+values+from+sparse+instances%3F
    // http://weka.wikispaces.com/ARFF+%28stable+version%29#Sparse%20ARFF%20files
    for (Attribute attribute : attributeMap.values()) {
      if (attribute.isString() && attribute.numValues() == 0) {
        attribute.addStringValue(UUID.randomUUID().toString());
      }
    }

    Attribute outcomeAttribute = createOutcomeAttribute(attributes.size());
    attributes.add(outcomeAttribute);

    Instances instances = new Instances(relationTag, attributes, instanceFeatures.size());
    instances.setClass(outcomeAttribute);

    for (int i = 0; i < instanceFeatures.size(); i++) {
      SparseInstance instance = new SparseInstance(instances.numAttributes());

      Iterable<Feature> features = instanceFeatures.get(i);
      for (Feature feature : features) {
        Attribute attribute = attributeMap.get(feature.getName());
        Object featureValue = feature.getValue();

        if (featureValue instanceof Number) {
          double attributeValue = ((Number) feature.getValue()).doubleValue();
          instance.setValue(attribute, attributeValue);
        } else if (featureValue instanceof Boolean) {
          double attributeValue = (Boolean) featureValue ? 1.0d : -1.0d;
          instance.setValue(attribute, attributeValue);
        } else {
          instance.setValue(attribute, featureValue.toString());
        }
      }

      instance.setValue(outcomeAttribute, instanceOutcomes.get(i));
      instances.add(instance);
    }

    trainingDataWriter.write(instances.toString());
    super.finish();
  }

  private Attribute createOutcomeAttribute(int attributeIndex) {
    // TODO make sure that "outcome" is not the name of an existing feature.
    return new Attribute("outcome", new ArrayList<String>(this.outcomeValues));
  }

  @Override
  protected WekaStringOutcomeClassifierBuilder newClassifierBuilder() {
    return new WekaStringOutcomeClassifierBuilder();
  }

}
