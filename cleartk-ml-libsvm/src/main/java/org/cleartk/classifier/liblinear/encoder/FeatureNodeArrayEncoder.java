/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.liblinear.encoder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;

import com.google.common.collect.Maps;

import de.bwaldvogel.liblinear.FeatureNode;

/**
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class FeatureNodeArrayEncoder implements FeaturesEncoder<FeatureNode[]> {
  private static final long serialVersionUID = 1L;

  private Map<String, Integer> stringToInt = Maps.newHashMap();
  
  private boolean isFinalized = false;

  @Override
  public FeatureNode[] encodeAll(Iterable<Feature> features) throws CleartkEncoderException {
    // map feature indexes to feature nodes, sorting by index
    Map<Integer, FeatureNode> featureNodes = Maps.newTreeMap();
    for (Feature feature : features) {

      // convert features to a String name and a double value
      String name;
      double value;
      if (feature.getValue() instanceof Number) {
        name = feature.getName();
        value = ((Number) feature.getValue()).doubleValue();
      } else {
        name = Feature.createName(feature.getName(), feature.getValue().toString());
        value = 1.0;
      }

      // convert the name String to an index
      if (!this.stringToInt.containsKey(name)) {
        if (!this.isFinalized) {
          this.stringToInt.put(name, this.stringToInt.size() + 1);
        }
        
        // don't create feature nodes for features not seen before finalization
        else {
          continue;
        }
      }
      int index = this.stringToInt.get(name);

      // create a feature node for the given index
      // NOTE: if there are duplicate features, only the last will be kept
      featureNodes.put(index, new FeatureNode(index, value));
    }
    
    // put the feature nodes into an array, sorted by feature index
    FeatureNode[] featureNodeArray = new FeatureNode[featureNodes.size()];
    int i = 0;
    for (Integer index : featureNodes.keySet()) {
      featureNodeArray[i] = featureNodes.get(index);
      ++i;
    }
    return featureNodeArray;
  }

  @Override
  public void finalizeFeatureSet(File outputDirectory) throws IOException {
    this.isFinalized = true;
  }
}
