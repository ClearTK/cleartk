/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.svmlight;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.jar.JarClassifier;
import org.cleartk.classifier.sigmoid.Sigmoid;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class SVMlightClassifier extends JarClassifier<Boolean, Boolean, FeatureVector> {

  public static final String ATTRIBUTES_NAME = "SVMlight";

  public static final String SCALE_FEATURES_KEY = "scaleFeatures";

  public static final String SCALE_FEATURES_VALUE_NORMALIZEL2 = "normalizeL2";

  SVMlightModel model;

  Sigmoid sigmoid;

  public SVMlightClassifier(JarFile modelFile) throws IOException, CleartkException {
    super(modelFile);

    try {
      ZipEntry modelEntry = modelFile.getEntry("model.svmlight");
      this.model = SVMlightModel.fromInputStream(modelFile.getInputStream(modelEntry));

      modelEntry = modelFile.getEntry("model.sigmoid");
      ObjectInput in = new ObjectInputStream(modelFile.getInputStream(modelEntry));
      this.sigmoid = (Sigmoid) in.readObject();
      in.close();
    } catch (ClassNotFoundException e) {
      throw new CleartkException(e);
    }
  }

  public Boolean classify(List<Feature> features) throws CleartkException {
    FeatureVector featureVector = featuresEncoder.encodeAll(features);

    double prediction = sigmoid.evaluate(model.evaluate(featureVector));
    boolean encodedResult = (prediction > 0.5);

    return outcomeEncoder.decode(encodedResult);
  }

  @Override
  public List<ScoredOutcome<Boolean>> score(List<Feature> features, int maxResults)
      throws CleartkException {

    List<ScoredOutcome<Boolean>> resultList = new ArrayList<ScoredOutcome<Boolean>>();
    if (maxResults > 0)
      resultList.add(this.score(features));
    if (maxResults > 1) {
      ScoredOutcome<Boolean> v1 = resultList.get(0);
      ScoredOutcome<Boolean> v2 = new ScoredOutcome<Boolean>(!v1.getOutcome(), 1 - v1.getScore());
      resultList.add(v2);
    }
    return resultList;
  }

  private ScoredOutcome<Boolean> score(List<Feature> features) throws CleartkException {
    FeatureVector featureVector = featuresEncoder.encodeAll(features);

    double prediction = sigmoid.evaluate(model.evaluate(featureVector));
    boolean encodedResult = (prediction > 0.5);

    if (encodedResult) {
      return new ScoredOutcome<Boolean>(true, prediction);
    } else {
      return new ScoredOutcome<Boolean>(false, 1 - prediction);
    }
  }
}
