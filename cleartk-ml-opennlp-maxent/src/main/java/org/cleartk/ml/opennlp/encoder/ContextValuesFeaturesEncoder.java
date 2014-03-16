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
package org.cleartk.ml.opennlp.encoder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.cleartk.ml.Feature;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.FeaturesEncoder;

/**
 * A class that converts ClearTK Features to the String[] and float[] needed by OpenNLP Maxent.
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class ContextValuesFeaturesEncoder implements FeaturesEncoder<ContextValues> {

  private static final long serialVersionUID = -1709923235891820441L;

  @Override
  public ContextValues encodeAll(Iterable<Feature> featuresIterable) throws CleartkEncoderException {
    // this is not guaranteed to work, however with normal use of ClearTK, it will
    Collection<Feature> features = (Collection<Feature>) featuresIterable;
    
    // populate context array and values array from the features
    String[] context = new String[features.size()];
    float[] values = new float[features.size()];
    int i = -1;
    for (Feature feature : features) {
      ++i;

      // convert features to a String name and a float value
      if (feature.getValue() instanceof Number) {
        context[i] = feature.getName();
        values[i] = ((Number) feature.getValue()).floatValue();
      } else if (feature.getValue() instanceof Boolean) {
        context[i] = feature.getName();
        values[i] = (Boolean) feature.getValue() ? 1.0f : 0.0f;
      } else {
        Object value = feature.getValue();
        context[i] = Feature.createName(feature.getName(), value == null ? null : value.toString());
        values[i] = 1.0f;
      }
    }
    
    return new ContextValues(context, values);
  }

  @Override
  public void finalizeFeatureSet(File outputDirectory) throws IOException {
    // nothing to do here
  }
}
