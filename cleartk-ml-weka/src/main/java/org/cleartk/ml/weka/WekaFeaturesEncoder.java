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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.features.FeaturesEncoder;

import com.google.common.annotations.Beta;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Majid Laali
 * 
 */
@Beta
public class WekaFeaturesEncoder implements FeaturesEncoder<Iterable<Feature>> {
  private static final long serialVersionUID = -1576549332348888211L;
  private Map<String, WekaNominalFeatureEncoder> nominalFeatures = new HashMap<>();
  private Map<String, Attribute> numberAttributes = new HashMap<>();

  public WekaFeaturesEncoder() {
  }

  public Iterable<Feature> encodeAll(Iterable<Feature> features) {
    for (Feature feature : features) {
      checkForNominalFeatures(feature);
    }
    return features;
  }

  private void checkForNominalFeatures(Feature feature) {
    Object value = feature.getValue();
    String name = feature.getName();
    
    if (value instanceof Number){
      if (nominalFeatures.containsKey(name))
        throw new RuntimeException(String.format("The feature <%s> cannon hold number values, %s is a number.", name, value.toString()));
      numberAttributes.put(name, new Attribute(name));
    } else {
      
      if (numberAttributes.containsKey(name))
        throw new RuntimeException(String.format("The feature <%s> can only hold number values, %s is not a number.", name, value.toString()));
      
      WekaNominalFeatureEncoder wekaNominalFeatureEncoder = nominalFeatures.get(name);
      if (wekaNominalFeatureEncoder == null){
        wekaNominalFeatureEncoder = new WekaNominalFeatureEncoder(name, true);
        nominalFeatures.put(name, wekaNominalFeatureEncoder);
      }
      wekaNominalFeatureEncoder.save(value == null ? null : value.toString());
    }
    
  }

  public void finalizeFeatureSet(File outputDirectory) {
  }

  public Instances makeInstances(int size, Attribute outcomeAttribute, String relationTag) {
    List<String> allAttributes = new ArrayList<>();
    allAttributes.addAll(numberAttributes.keySet());
    allAttributes.addAll(nominalFeatures.keySet());
    Collections.sort(allAttributes);
    
    ArrayList<Attribute> attributes = new ArrayList<>(); 
    for (String attributeName: allAttributes){
      if (numberAttributes.containsKey(attributeName)){
        attributes.add(numberAttributes.get(attributeName));
      } else
        attributes.add(nominalFeatures.get(attributeName).getAttribute());
    }
    
    attributes.add(outcomeAttribute);
    Instances instances = new Instances(relationTag, attributes, size);
    instances.setClass(outcomeAttribute);
    return instances;
  }

  public SparseInstance createInstance(Iterable<Feature> features) {
    
    SparseInstance instance = new SparseInstance(numberAttributes.size() + nominalFeatures.size() + 1); //add one for the outcome attribute 
    
    for (Feature feature: features){
      String attributeName = feature.getName();
      if (numberAttributes.containsKey(attributeName)){
        instance.setValue(numberAttributes.get(attributeName), ((Number)feature.getValue()).doubleValue());
      } else
        nominalFeatures.get(attributeName).setAttributeValue(instance, feature.getValue());  
    }
    return instance;
  }

}
