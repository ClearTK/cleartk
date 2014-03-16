/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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

package org.cleartk.ml.tksvmlight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.cleartk.ml.Feature;
import org.cleartk.ml.TreeFeature;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.FeatureEncoder;
import org.cleartk.ml.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.ml.encoder.features.FeaturesEncoder;
import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.ml.encoder.features.normalizer.NoOpNormalizer;
import org.cleartk.ml.util.featurevector.FeatureVector;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @version 0.2.1
 * 
 *          An econder that is used for TreeFeatureVectors.
 * 
 */
@Beta
public class TreeFeatureVectorFeaturesEncoder implements FeaturesEncoder<TreeFeatureVector> {

  /**
   * 
   */
  private static final long serialVersionUID = -3144134287152993099L;

  private FeatureVectorFeaturesEncoder nameNumberEncoder;

  public TreeFeatureVectorFeaturesEncoder(int cutoff, NameNumberNormalizer normalizer) {
    nameNumberEncoder = new FeatureVectorFeaturesEncoder(cutoff, normalizer);
  }

  public TreeFeatureVectorFeaturesEncoder(int cutoff) {
    this(cutoff, new NoOpNormalizer());
  }

  public TreeFeatureVectorFeaturesEncoder() {
    this(0, new NoOpNormalizer());
  }

  @Override
  public TreeFeatureVector encodeAll(Iterable<Feature> features) throws CleartkEncoderException {
    List<Feature> fves = new ArrayList<Feature>();
    LinkedHashMap<String, String> trs = new LinkedHashMap<String, String>();
    for (Feature feature : features) {
      if (feature instanceof TreeFeature || (feature.getName() != null && feature.getName().matches("^TK.*"))) {
        trs.put(feature.getName(), feature.getValue().toString());
      } else {
        fves.add(feature);
      }
    }
    FeatureVector f = nameNumberEncoder.encodeAll(fves);
    TreeFeatureVector tfv = new TreeFeatureVector();
    tfv.setFeatures(f);
    tfv.setTrees(trs);
    return tfv;
  }

  @Override
  public void finalizeFeatureSet(File outputDirectory) throws IOException {
    nameNumberEncoder.finalizeFeatureSet(outputDirectory);

  }

  public void addEncoder(FeatureEncoder<NameNumber> numberEncoder) {
    nameNumberEncoder.addEncoder(numberEncoder);
  }

  public void setNormalizer(NameNumberNormalizer normalizer) {
    nameNumberEncoder.setNormalizer(normalizer);
  }
}
